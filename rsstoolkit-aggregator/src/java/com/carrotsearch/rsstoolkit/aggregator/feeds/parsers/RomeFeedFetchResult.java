package com.carrotsearch.rsstoolkit.aggregator.feeds.parsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedFetchResults;
import com.carrotsearch.rsstoolkit.aggregator.feeds.INewsPost;

/**
 * A fetch result returned by the {@link RomeFeed}.
 */
final class RomeFeedFetchResult implements IFeedFetchResults
{
    /**
     * 
     */
    private final long ttl;

    /**
     * 
     */
    private ArrayList<INewsPost> posts;

    /**
     * 
     */
    public RomeFeedFetchResult(ArrayList<INewsPost> posts, long ttl)
    {
        this.ttl = ttl;
        this.posts = posts;
    }

    /**
     * Implements {@link IFeedFetchResults#getNextUpdateTimestamp()}.
     */
    public long getUpdateInterval()
    {
        return this.ttl;
    }

    /**
     * Implements {@link IFeedFetchResults#getPosts()}.
     */
    public Collection<INewsPost> getPosts()
    {
        return Collections.unmodifiableCollection(this.posts);
    }
}
