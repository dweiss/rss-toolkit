package com.carrotsearch.rsstoolkit.aggregator.queue;

import java.util.Collection;

import com.carrotsearch.rsstoolkit.aggregator.collectors.UpdateInfo;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;

/**
 * A feed queue interface. Keeps the feeds in the order of subsequent fetch time.
 */
public interface IFeedQueue
{
    /**
     * @return Returns a collection of {@link IFeed}s that need to be updated. All feeds
     *         which have an expected fetch time lower than <code>now</code> will be
     *         returned.
     */
    public Collection<IFeed> getStaleFeeds(final long now);

    /**
     * @return Returns the time of the closest expected feed fetch or un undefined value
     *         in the future if there is no next element to fetch.
     */
    public long getClosestFetchTimestamp();

    /**
     * @return Returns a count of all feeds present in the queue.
     */
    public long getFeedsCount();

    /**
     * Acquire a new schedule for the feed, depending on the <code>ttl</code> value (if
     * greater than zero) and the history of updates.
     * 
     * @param ttl Expected number of milliseconds in which the next update should occur or
     *            zero.
     */
    public void updateSchedule(IFeed feed, long ttl, UpdateInfo updateInfo);

    /**
     * Update the status of this feed and update its schedule if possible.
     */
    public void updateFeedStatus(IFeed feed, FeedStatus status, String message);
}