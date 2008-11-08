package com.carrotsearch.rsstoolkit.aggregator.feeds.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.carrotsearch.rsstoolkit.aggregator.feeds.FeedFetchingException;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedFetchResults;
import com.carrotsearch.rsstoolkit.aggregator.feeds.INewsPost;
import com.carrotsearch.rsstoolkit.aggregator.feeds.NewsPostImpl;
import com.carrotsearch.rsstoolkit.aggregator.feeds.FeedFetchingException.REASON;
import com.carrotsearch.util.httpclient.HttpClientFactory;
import com.carrotsearch.util.time.Utils;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.module.Extendable;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.ParsingFeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * <p>
 * An implementation of a {@link IFeed} which uses <a
 * href="https://rome.dev.java.net/">Rome</a> library to parse RSS and Atom feeds.
 */
@SuppressWarnings("unchecked")
final class RomeFeed implements IFeed
{
    /** Local logger for this class. */
    private final static Logger logger = Logger.getLogger(RomeFeed.class);

    /**
     * The feed's URI.
     */
    private final URI uri;

    /**
     * {@link #uri} converted to a string.
     */
    private final String urlString;

    /**
     * Feed identifier.
     */
    private final Object feedId;

    /**
     * Remember TTL status.
     */
    private boolean noTTL;

    /**
     * @param uri The feed's URI.
     */
    public RomeFeed(URI uri, Object feedId)
    {
        this.feedId = feedId;
        this.uri = uri;
        this.urlString = uri.toString();
    }

    /**
     * 
     */
    public RomeFeed(URI uri)
    {
        this(uri, uri);
    }

    /**
     * Implementation of {@link IFeed#getId()}.
     */
    public Object getId()
    {
        return feedId;
    }

    /**
     * Implementation of {@link IFeed#fetch()}
     */
    public IFeedFetchResults fetch() throws FeedFetchingException
    {
        final HttpClient httpClient = HttpClientFactory.getTimeoutingClient();

        final GetMethod getMethod = new GetMethod(uri.toString());
        getMethod.setFollowRedirects(true);

        try
        {
            final int responseCode = httpClient.executeMethod(getMethod);
            if (responseCode == HttpStatus.SC_OK)
            {
                InputStream inputStream = null;
                try
                {
                    inputStream = getMethod.getResponseBodyAsStream();
                    return parseFeedResponse(inputStream);
                }
                finally
                {
                    if (inputStream != null)
                    {
                        try
                        {
                            inputStream.close();
                        }
                        catch (IOException e)
                        {
                            logger.warn("Could not close socket stream.");
                            // ignore.
                        }
                    }
                }
            }
            else
            {
                handleHTTPException(getMethod, null);
            }
        }
        catch (ParsingFeedException e)
        {
            throw new FeedFetchingException(REASON.INVALID_XML, e);
        }
        catch (FeedException e)
        {
            throw new FeedFetchingException(REASON.UNSPECIFIED, e);
        }
        catch (HttpException e)
        {
            handleHTTPException(getMethod, e);
        }
        catch (IOException e)
        {
            throw new FeedFetchingException(REASON.IO, e);
        }
        finally
        {
            getMethod.releaseConnection();
        }

        throw new RuntimeException(/* Unreachable block */);
    }

    /**
     * 
     */
    private void handleHTTPException(GetMethod getMethod, HttpException e)
        throws FeedFetchingException
    {
        final String statusText = getMethod.getStatusLine().toString()
            + (e != null ? "(HTTP exception: " + e.toString() + ")" : "");

        switch (getMethod.getStatusCode())
        {
            case HttpStatus.SC_FORBIDDEN:
                throw new FeedFetchingException(REASON.HTTP_403_FORBIDDEN, statusText);
            case HttpStatus.SC_NOT_FOUND:
                throw new FeedFetchingException(REASON.HTTP_404_NOT_FOUND, statusText);
        }
        throw new FeedFetchingException(REASON.HTTP_OTHER, statusText);
    }

    /**
     * Parses raw response from the feed's source. Package scope for tests.
     */
    final IFeedFetchResults parseFeedResponse(InputStream inputStream)
        throws IllegalArgumentException, FeedException, IOException
    {
        final WireFeedInput input = new WireFeedInput();
        final WireFeed feed = input.build(new XmlReader(inputStream));

        if (feed instanceof Channel)
        {
            return createRSSFeedResult((Channel) feed);
        }
        else if (feed instanceof Feed)
        {
            return createAtomFeedResult((Feed) feed);
        }
        else
        {
            throw new FeedException("Unsupported feed type: " + feed.getFeedType());
        }
    }

    /**
     * Creates and returns a {@link IFeedFetchResults} for an RSS feed.
     */
    private IFeedFetchResults createRSSFeedResult(Channel channel)
    {
        final Date now = new Date();

        // Determine next update time for this channel.
        long nextUpdate = channel.getTtl();
        if (nextUpdate <= 0)
        {
            if (noTTL == false)
            {
                noTTL = true; // Don't log again.
                logger.info("The RSS feed does not contain TTL field: " + getId());
            }
            nextUpdate = 0;
        }
        else
        {
            nextUpdate = nextUpdate * Utils.MINUTE; // Conform to the TTL.
        }

        // TODO: Possibly add support for daySkip and hourSkip. This will require
        // converting
        // next update time to a Calendar and making a while loop incrementing by one hour
        // until the conditions are satisfied.

        // Proceed to the items in this fetch.
        final ArrayList<INewsPost> posts = new ArrayList<INewsPost>();
        final List<Item> items = channel.getItems();
        for (final Item item : items)
        {
            final Date expirationDate = item.getExpirationDate();
            if (expirationDate != null && now.after(expirationDate))
            {
                // Skip expired posts.
                continue;
            }

            // Get publication date directly or from DC (Dublin core) module.
            final Date publicationDate = resolveUpdateDate(item.getPubDate(), item);

            final String title = trimAndNormalize(item.getTitle());
            // TODO: Should we parse different content formats here?
            final Description description = item.getDescription();
            final String textDescription = (description == null ? null
                : trimAndNormalize(description.getValue()));

            if (title == null)
            {
                logger.info("Skipping post without a title, feed: " + this.getId());
                continue;
            }

            final NewsPostImpl post = new NewsPostImpl(createId(urlString, title,
                textDescription), this.getId(), publicationDate);

            post.setTitle(title);

            if (textDescription != null)
            {
                post.setDescription(textDescription);
            }

            if (item.getLink() != null)
            {
                final String resolved = resolveRelative(item.getLink().trim());
                if (resolved != null)
                {
                    post.setLinks(new String []
                    {
                        resolved
                    });
                }
            }

            if (item.getAuthor() != null)
            {
                post.setAuthors(new String []
                {
                    item.getAuthor().trim()
                });
            }
            posts.add(post);
        }

        return new RomeFeedFetchResult(posts, nextUpdate);
    }

    /**
     * Resolve relative link against the base URI.
     * 
     * @return A string with resolved link or <code>null</code> if an error occurred.
     */
    final String resolveRelative(String link)
    {
        URI itemURI = null;

        // Try appending directly (assuming the link is URI-escaped properly).
        try
        {
            itemURI = new URI(link);
        }
        catch (URISyntaxException e)
        {
            logger.debug("First attempt to resolve link failed: " + this.getId()
                + ", link: " + link + ", base: " + urlString);

            final char [] chars = link.toCharArray();
            final StringBuilder linkBuf = new StringBuilder();
            for (int i = 0; i < chars.length; i++)
            {
                final char chr = chars[i];

                if (chr == '/' || chr == '~' || chr == '_' || chr == '.' || chr == '*'
                    || chr == '-' || chr == ',' || chr == '?' || chr == '='
                    || (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z')
                    || (chr >= '0' && chr <= '9'))
                {
                    linkBuf.append(chr);
                }
                else if (chr == ' ')
                {
                    linkBuf.append('+');
                }
                else
                {
                    if (chr == '%' && i + 2 < chars.length
                        && (chars[i + 1] > '0' && chars[i + 1] < '9')
                        && (chars[i + 2] > '0' && chars[i + 2] < '9'))
                    {
                        linkBuf.append(chars[i]);
                        i++;
                        linkBuf.append(chars[i]);
                        i++;
                        linkBuf.append(chars[i]);
                    }
                    else
                    {
                        try
                        {
                            linkBuf.append(URLEncoder.encode("" + chr, "UTF-8"));
                        }
                        catch (UnsupportedEncodingException x)
                        {
                            throw new RuntimeException(x);
                        }
                    }
                }
            }

            link = linkBuf.toString();
        }

        try
        {
            if (itemURI == null)
            {
                itemURI = new URI(link);
            }

            if (!itemURI.isAbsolute())
            {
                final URI baseURI = new URI(urlString);
                // Relative link, but must be absolute
                itemURI = baseURI.resolve(itemURI);
            }
            return itemURI.toString();
        }
        catch (URISyntaxException e)
        {
            logger.warn("Error while processing link for feed: " + this.getId()
                + ", link: " + link + ", base: " + urlString, e);
            return null;
        }
    }

    /**
     * Resolve the update date from the first argument or, if <code>null</code> from the
     * <code>item</code> using {@link DCModule} (if available). Default fallback is to
     * return the current date.
     */
    private Date resolveUpdateDate(Date pubDate, Extendable item)
    {
        if (null != pubDate)
        {
            return pubDate;
        }

        // Try DC module.
        final DCModule dcModule = (DCModule) item.getModule(DCModule.URI);
        if (dcModule != null && dcModule.getDate() != null)
        {
            return dcModule.getDate();
        }

        // Fallback to 'now'.
        return new Date();
    }

    /**
     * Normalize empty strings to null, chop surrounding spaces.
     */
    private String trimAndNormalize(String value)
    {
        if (value != null)
        {
            value = value.trim();
            if ("".equals(value))
            {
                value = null;
            }
        }
        return value;
    }

    /**
     * Creates and returns a {@link IFeedFetchResults} for an Atom feed.
     */
    private IFeedFetchResults createAtomFeedResult(Feed atomFeed)
    {
        // TODO: Atom feeds should (theoretically) have HTTP cache headers
        // set to proper values. I have not checked this possibility and how
        // this specification is actually implemented in real life.
        final long nextUpdate = 0 /* UNKNOWN, estimate feed update cycle. */;

        // Determine author(s).
        final String [] feedAuthors = extractFeedAuthors(atomFeed.getAuthors());

        // Proceed to the items in this fetch.
        final ArrayList<INewsPost> posts = new ArrayList<INewsPost>();
        for (final Entry entry : (List<Entry>) atomFeed.getEntries())
        {
            final String id = entry.getId();
            if (id == null)
            {
                logger.warn("Skipping post without a valid identifier, feed: "
                    + this.getId());
                continue;
            }

            final String title = entry.getTitle();
            final Date updated = resolveUpdateDate(entry.getUpdated(), entry);
            String [] authors = extractFeedAuthors(entry.getAuthors());
            if (authors.length == 0)
            {
                authors = feedAuthors;
            }

            // There is most likely just one content element per entry,
            // but Rome wants it different...
            //
            // TODO: Should we parse different content formats here?
            final StringBuilder contentBuilder = new StringBuilder();
            for (final Content content : (List<Content>) entry.getContents())
            {
                if (contentBuilder.length() > 0)
                {
                    contentBuilder.append("\n");
                }
                contentBuilder.append(content.getValue());
            }

            final NewsPostImpl post = new NewsPostImpl(createId(urlString, title,
                contentBuilder.toString()), this.getId(), updated);
            post.setTitle(title);
            post.setDescription(contentBuilder.toString());
            post.setAuthors(authors);

            final List altLinks = entry.getAlternateLinks();
            if (altLinks != null && altLinks.size() > 0)
            {
                final ArrayList<String> links = new ArrayList<String>(altLinks.size());
                for (final Object ob : entry.getAlternateLinks())
                {
                    final String href = ((Link) ob).getHref();
                    if (href == null)
                    {
                        continue;
                    }

                    final String resolved = resolveRelative(href);
                    if (resolved != null)
                    {
                        links.add(resolved);
                    }
                }
                post.setLinks(links.toArray(new String [links.size()]));
            }
            else
            {
                post.setLinks(new String [0]);
            }

            posts.add(post);
        }

        return new RomeFeedFetchResult(posts, nextUpdate);
    }

    /**
     * Extracts names of persons from a list and returns it as an array of strings.
     */
    private static String [] extractFeedAuthors(List authorList)
    {
        final ArrayList<String> authors = new ArrayList<String>();
        if (authorList != null)
        {
            for (Iterator i = authorList.iterator(); i.hasNext();)
            {
                final Person person = (Person) i.next();
                if (person != null && person.getName() != null)
                {
                    authors.add(person.getName());
                }
            }
        }
        return authors.toArray(new String [authors.size()]);
    }

    /**
     * <p>
     * Creates a unique (?) identifier for a news post described with the title and/or
     * description.
     * <p>
     * We currently use an MD5 checksum.
     */
    private static String createId(String feedId, String title, String description)
    {
        if (title == null && description == null)
        {
            throw new IllegalArgumentException(
                "Either title or description must not be null.");
        }

        final String encoding = "UTF-8";
        try
        {
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            if (feedId == null)
            {
                throw new IllegalArgumentException("FeedID must not be null.");
            }
            md5.update(feedId.getBytes(encoding));

            if (title != null) md5.update(title.getBytes(encoding));
            if (description != null) md5.update(description.getBytes(encoding));

            final byte [] checksum = md5.digest();
            return new String(org.apache.commons.codec.binary.Hex.encodeHex(checksum));
        }
        catch (NoSuchAlgorithmException e)
        {
            final String message = "MD5 digest not available?";
            logger.fatal(message, e);
            throw new RuntimeException(message, e);
        }
        catch (UnsupportedEncodingException e)
        {
            final String message = "Encoding not available: " + encoding;
            logger.fatal(message, e);
            throw new RuntimeException(message, e);
        }
    }
}
