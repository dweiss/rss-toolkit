package com.carrotsearch.rsstoolkit.aggregator.feeds.parsers;

import java.net.URI;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedParsersFactory;

/**
 * Parser factory based on Rome library.
 */
public class RomeFeedFactory implements IFeedParsersFactory
{
    /**
     * 
     */
    public IFeed getFeedParser(URI uri, Object feedId)
    {
        return new RomeFeed(uri, feedId);
    }
}
