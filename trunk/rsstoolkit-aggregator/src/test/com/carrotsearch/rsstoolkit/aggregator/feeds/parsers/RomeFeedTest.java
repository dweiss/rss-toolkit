package com.carrotsearch.rsstoolkit.aggregator.feeds.parsers;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import junit.framework.TestCase;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedFetchResults;
import com.carrotsearch.rsstoolkit.aggregator.feeds.INewsPost;

/**
 * Tests {@link RomeFeed}.
 */
public final class RomeFeedTest extends TestCase
{
    private final URI FAKE_URI;

    /**
     * 
     */
    public RomeFeedTest(String t)
    {
        super(t);

        try
        {
            FAKE_URI = new URI("http://no-host.no-domain.xx");
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     */
    public void testRssFetching() throws Exception
    {
        final RomeFeed romeFeed = new RomeFeed(new URI(
            "http://rss.cnn.com/rss/cnn_allpolitics.rss"));
        final IFeedFetchResults results = romeFeed.fetch();

        assertNotNull(results);
        assertTrue(results.getUpdateInterval() > 0);
        assertTrue(results.getPosts().size() > 0);

        for (final INewsPost post : results.getPosts())
        {
            assertNotNull(post.getId());
        }
    }

    /**
     * Check DC dates on items.
     * <code>http://www.icrc.org/web/eng/siteeng0.nsf/newsfeed.rss</code>
     */
    public void testRssDcDates() throws Exception
    {
        final RomeFeed romeFeed = new RomeFeed(FAKE_URI);
        final InputStream is = this.getClass().getResourceAsStream("rss-dc.rss");
        final IFeedFetchResults results = romeFeed.parseFeedResponse(is);

        assertNotNull(results);
        assertTrue(results.getPosts().size() > 0);

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",
            Locale.US);
        final long end = sdf.parse("2007-11-07T11:50:18+0200").getTime();
        final long start = sdf.parse("2007-10-18T16:27:50+0200").getTime();

        for (final INewsPost post : results.getPosts())
        {
            assertNotNull(post.getId());
            assertTrue(post.getPublicationDate().getTime() >= start);
            assertTrue(post.getPublicationDate().getTime() <= end);
        }
    }

    // http://english.aljazeera.net/NR/exeres/4D6139CD-6BB5-438A-8F33-96A7F25F40AF.htm?ArticleGuid=736515E4-37CE-4242-8A5F-854381D9DFEE
    public void testRssPubDateDates() throws Exception
    {
        final RomeFeed romeFeed = new RomeFeed(FAKE_URI);
        final InputStream is = this.getClass().getResourceAsStream("rss-aljazeera.rss");
        final IFeedFetchResults results = romeFeed.parseFeedResponse(is);

        assertNotNull(results);
        assertTrue(results.getPosts().size() > 0);

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
            Locale.US);
        final long end = sdf.parse("2007-11-20T06:24:41").getTime();
        final long start = sdf.parse("2007-11-15T20:14:47").getTime();

        for (final INewsPost post : results.getPosts())
        {
            assertNotNull(post.getId());
            assertTrue(post.getPublicationDate() + " > " + start, post
                .getPublicationDate().getTime() >= start);
            assertTrue(post.getPublicationDate() + " < " + end, post.getPublicationDate()
                .getTime() <= end);
        }
    }

    /**
     * Check parsing of invalid URIs.
     */
    public void testURIResolve() throws Exception
    {
        RomeFeed romeFeed = new RomeFeed(new URI("http://www.film.pl"));
        assertEquals(
            "http://www.film.pl/aktualnosci/montazysci-dzwieku-doceniaja-bourne%60a.html",
            romeFeed
                .resolveRelative("/aktualnosci/montazysci-dzwieku-doceniaja-bourne`a.html"));

        romeFeed = new RomeFeed(new URI("http://mediafm.net"));
        assertEquals(
            "http://mediafm.net/reklama/14432,Nowy-copywriter-w-TEQUILA%5CPolska.html",
            romeFeed
                .resolveRelative("/reklama/14432,Nowy-copywriter-w-TEQUILA\\Polska.html"));

        romeFeed = new RomeFeed(new URI("http://www.fakty.co.pl"));
        assertEquals(
            "http://www.fakty.co.pl/story.php?title=29%25_polskich_dzieci_zyje_w_nedzy_Polska_pomaga_Gruzji",
            romeFeed
                .resolveRelative("/story.php?title=29%_polskich_dzieci_zyje_w_nedzy_Polska_pomaga_Gruzji"));
    }
}
