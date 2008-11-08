package com.carrotsearch.rsstoolkit.aggregator.feeds;

import java.util.Collection;

/**
 * A result returned from a {@link IFeed} containing a series of {@link INewsPost}s and
 * updated next fetch plan.
 */
public interface IFeedFetchResults
{
    /**
     * @return Returns the number of milliseconds to the next update or <code>-1</code>
     *         if this time is not known.
     */
    public long getUpdateInterval();

    /**
     * @return Returns the posts fetched as part of this request.
     */
    public Collection<INewsPost> getPosts();
}
