package com.carrotsearch.rsstoolkit.aggregator.feeds.sources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import junit.framework.TestCase;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.parsers.RomeFeedFactory;

/**
 * Tests {@link XMLFeedSource}.
 */
public final class XMLFeedSourceTest extends TestCase
{
    /**
     * 
     */
    public XMLFeedSourceTest(String t)
    {
        super(t);
    }

    /**
     * 
     */
    public void testDefaultParser() throws IOException
    {
        final InputStream is = this.getClass().getResourceAsStream("test-sources.xml");
        try
        {
            final XMLFeedSource source = new XMLFeedSource(is, new RomeFeedFactory());
            final Collection<IFeed> feeds = source.getFeeds();
            assertTrue(feeds.size() > 0);

            for (final IFeed feed : feeds)
            {
                assertNotNull(feed);
            }
        }
        finally
        {
            is.close();
        }
    }
}
