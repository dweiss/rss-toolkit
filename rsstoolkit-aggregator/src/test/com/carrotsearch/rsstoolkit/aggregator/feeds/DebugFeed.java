package com.carrotsearch.rsstoolkit.aggregator.feeds;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedFetchResults;
import com.carrotsearch.rsstoolkit.aggregator.feeds.INewsPost;
import com.carrotsearch.rsstoolkit.aggregator.feeds.NewsPostImpl;
import com.carrotsearch.util.time.Utils;

/**
 * An implementation of {@link IFeed} for testing purposes.
 */
public final class DebugFeed implements IFeed
{
    private final String id;
    private final long updateDelay;
    private int postsCount;

    public DebugFeed(String id, long updateDelay, int randomPostsCount)
    {
        this.id = id;
        this.updateDelay = updateDelay;
        this.postsCount = randomPostsCount;
    }

    public DebugFeed(String id)
    {
        this(id, 3 * Utils.SECOND, 0);
    }

    public String getId()
    {
        return id;
    }

    public String toString()
    {
        return id;
    }

    /**
     * Implementation of {@link IFeed#fetch()}
     */
    public IFeedFetchResults fetch()
    {
        return new IFeedFetchResults()
        {
            public long getUpdateInterval()
            {
                return updateDelay;
            }

            public Collection<INewsPost> getPosts()
            {
                final INewsPost [] posts = new INewsPost [postsCount];
                final Date now = new Date();
                for (int i = 0; i < posts.length; i++)
                {
                    final NewsPostImpl post = new NewsPostImpl(Integer.toString(i),
                        getId(), now);
                    post.setTitle("title " + i);
                    post.setDescription("description " + i);
                    posts[i] = post;
                }
                return Arrays.asList(posts);
            }
        };
    }
}
