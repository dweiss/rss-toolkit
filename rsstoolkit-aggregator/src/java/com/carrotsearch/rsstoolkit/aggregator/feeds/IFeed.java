package com.carrotsearch.rsstoolkit.aggregator.feeds;

/**
 * <p>
 * Details describing an article feed.
 */
public interface IFeed
{
    /**
     * @return Returns something uniquely identifying this feed.
     */
    public Object getId();

    /**
     * Fetches current content of this feed.
     */
    public IFeedFetchResults fetch() throws FeedFetchingException;
}
