package com.carrotsearch.rsstoolkit.aggregator.collectors;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.INewsPost;

/**
 * <p>
 * Implementation of {@link INewsPostsCollector} that does nothing (discards the posts).
 */
public final class NullCollector implements INewsPostsCollector
{
    private final static Logger logger = Logger.getLogger(NullCollector.class);

    @SuppressWarnings("unused")
    public synchronized UpdateInfo add(Collection<INewsPost> posts, IFeed source)
    {
        for (final INewsPost post : posts)
        {
            logger.info("Collected post: " + post.getId());
        }
        return new UpdateInfo(posts.size(), 0, posts.size());
    }
}
