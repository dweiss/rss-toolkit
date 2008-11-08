package com.carrotsearch.rsstoolkit.aggregator.collectors;

import java.util.Collection;
import java.util.HashMap;

import com.carrotsearch.rsstoolkit.aggregator.collectors.INewsPostsCollector;
import com.carrotsearch.rsstoolkit.aggregator.collectors.UpdateInfo;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.INewsPost;

/**
 * <p>
 * Implementation of {@link INewsPostsCollector} that collects all posts in an in-memory
 * {@link #posts} set.
 */
public final class SetCollector implements INewsPostsCollector
{
    public final HashMap<String, INewsPost> posts = new HashMap<String, INewsPost>();

    @SuppressWarnings("unused")
    public synchronized UpdateInfo add(Collection<INewsPost> posts, IFeed source)
    {
        int added = 0;
        int ignored = 0;
        for (INewsPost post : posts)
        {
            if (this.posts.containsKey(post.getId()))
            {
                ignored++;
                continue;
            }

            this.posts.put(post.getId(), post);
            added++;
        }
        return new UpdateInfo(posts.size(), added, ignored);
    }
}
