package com.carrotsearch.rsstoolkit.aggregator.collectors;

import java.util.ArrayList;
import java.util.Collection;

import com.carrotsearch.rsstoolkit.aggregator.collectors.INewsPostsCollector;
import com.carrotsearch.rsstoolkit.aggregator.collectors.UpdateInfo;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.INewsPost;

/**
 * <p>
 * Implementation of {@link INewsPostsCollector} that collects all posts in an in-memory
 * {@link #posts} list.
 */
public final class ListCollector implements INewsPostsCollector
{
    public final ArrayList<INewsPost> posts = new ArrayList<INewsPost>();

    @SuppressWarnings("unused")
    public synchronized UpdateInfo add(Collection<INewsPost> posts, IFeed source)
    {
        this.posts.addAll(posts);
        return new UpdateInfo(posts.size(), posts.size(), 0);
    }
}
