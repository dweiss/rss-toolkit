package com.carrotsearch.rsstoolkit.aggregator.collectors;

import java.util.Collection;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.INewsPost;

/**
 * <p>
 * A collector of {@link INewsPost}s fetched from an {@link IFeed}.
 * <p>
 * A collector implementation should support multithreading (many threads adding new posts
 * at the same time).
 */
public interface INewsPostsCollector
{
    /**
     * Consumes a collection of posts fetched from a given {@link IFeed}.
     */
    public UpdateInfo add(final Collection<INewsPost> posts, final IFeed source);
}
