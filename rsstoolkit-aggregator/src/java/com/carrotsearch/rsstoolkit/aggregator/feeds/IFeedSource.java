package com.carrotsearch.rsstoolkit.aggregator.feeds;

import java.util.Collection;

/**
 * <p>
 * A source of one or many {@link IFeed}s.
 */
public interface IFeedSource
{
    /**
     * @return Returns a collection of {@link IFeed} instances, ready for crawling.
     */
    public Collection<IFeed> getFeeds();
}
