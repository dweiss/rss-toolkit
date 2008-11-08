package com.carrotsearch.rsstoolkit.aggregator.feeds.sources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedParsersFactory;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedSource;

/**
 * <p>
 * An {@link IFeedSource} reading feeds from an XML file. All elements matching an XPath
 * expression are returned. Namespaces can be added to an internal static field
 * {@link #namespaces}, if needed.
 */
public final class XMLFeedSource implements IFeedSource
{
    /** Logger for this class. */
    private final static Logger logger = Logger.getLogger(XMLFeedSource.class);

    /**
     * Default feed extraction XPath.
     */
    public final static String DEFAULT_XPATH = "/meadanrss:feeds/meadanrss:feed/attribute::url";

    /**
     * A map of all recognized namespaces.
     */
    public final static Map<String, String> namespaces;
    static
    {
        final HashMap<String, String> localMap = new HashMap<String, String>();
        localMap.put("meadanrss", "http://www.meadan.org/doctypes/feeds");
        namespaces = Collections.unmodifiableMap(localMap);
    }

    /**
     * A collection of feeds read from the input.
     */
    private final Collection<IFeed> feeds;

    /**
     * Provides parsers for the collected feeds.
     */
    private final IFeedParsersFactory feedParsersFactory;

    /**
     * @param xmlStream An XML stream with RDF data.
     * @param XPath An XPath expression extracting URLs to RSS/Atom feed sources.
     */
    public XMLFeedSource(final InputStream xmlStream, final String XPath,
        final IFeedParsersFactory feedParsersFactory) throws IOException
    {
        assert XPath != null;
        assert xmlStream != null;
        assert feedParsersFactory != null;

        this.feedParsersFactory = feedParsersFactory;
        this.feeds = parse(xmlStream, XPath);
    }

    /**
     * Creates an instance of this class with the default XPath expression
     * {@link #DEFAULT_XPATH}.
     * 
     * @param xmlStream An XML stream with RDF data.
     */
    public XMLFeedSource(final InputStream xmlStream,
        final IFeedParsersFactory feedParsersFactory) throws IOException
    {
        this(xmlStream, DEFAULT_XPATH, feedParsersFactory);
    }

    /**
     * Extracts URLs from the input XML and creates {@link #feeds}.
     */
    @SuppressWarnings("unchecked")
    private Collection<IFeed> parse(InputStream xmlStream, String xpath)
        throws IOException
    {
        final SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);

        try
        {
            final XPath compiledXPath = XPath.newInstance(xpath);
            for (Map.Entry<String, String> entry : namespaces.entrySet())
            {
                compiledXPath.addNamespace(entry.getKey(), entry.getValue());
            }

            final ArrayList<IFeed> feedList = new ArrayList<IFeed>();
            final Document doc = builder.build(xmlStream);
            for (final Iterator i = compiledXPath.selectNodes(doc).iterator(); i
                .hasNext();)
            {
                final Object ob = i.next();
                final String uri;
                if (ob instanceof Attribute)
                {
                    uri = ((Attribute) ob).getValue();
                }
                else if (ob instanceof Element)
                {
                    uri = ((Element) ob).getTextTrim();
                }
                else
                {
                    throw new IOException("Only attribute and element nodes allowed: "
                        + ob);
                }

                try
                {
                    feedList.add(feedParsersFactory.getFeedParser(new URI(uri), uri));
                }
                catch (URISyntaxException e)
                {
                    logger.warn("Malformed feed URI: " + uri);
                }
            }
            return Collections.unmodifiableCollection(feedList);
        }
        catch (JDOMException e)
        {
            throw new IOException("Malformed XML: " + e.getMessage());
        }
    }

    /**
     * Implementation of {@link IFeedSource#getFeeds()}.
     */
    public Collection<IFeed> getFeeds()
    {
        return this.feeds;
    }
}