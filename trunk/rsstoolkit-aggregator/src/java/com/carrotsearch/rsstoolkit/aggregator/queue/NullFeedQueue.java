package com.carrotsearch.rsstoolkit.aggregator.queue;

import java.util.Collection;
import java.util.Collections;

import com.carrotsearch.rsstoolkit.aggregator.collectors.UpdateInfo;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.util.time.Utils;

/**
 * <p>
 * Empty feed queue. Does nothing.
 */
public final class NullFeedQueue implements IFeedQueue
{
    /**
     * @return Returns a collection of {@link IFeed}s that need to be updated. All feeds
     *         which have an expected fetch time lower than <code>now</code> will be
     *         returned.
     */
    @SuppressWarnings("unchecked")
    public Collection<IFeed> getStaleFeeds(final long now)
    {
        return Collections.emptyList();
    }

    /**
     * @return Returns the time of the closest expected feed fetch or
     *         <code>now + 5 seconds</code> if there is no next element to fetch.
     */
    public long getClosestFetchTimestamp()
    {
        return System.currentTimeMillis() + Utils.SECOND * 30;
    }

    /**
     * @return Returns a count of all feeds present in the queue.
     */
    public long getFeedsCount()
    {
        return 0;
    }

    /**
     * 
     */
    public void updateSchedule(IFeed feed, final long ttl, UpdateInfo updateInfo)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     */
    public void updateFeedStatus(IFeed feed, FeedStatus status, String message)
    {
        throw new UnsupportedOperationException();
    }
}
