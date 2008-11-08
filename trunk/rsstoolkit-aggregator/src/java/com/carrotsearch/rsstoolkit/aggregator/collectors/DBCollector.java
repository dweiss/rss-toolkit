package com.carrotsearch.rsstoolkit.aggregator.collectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.INewsPost;
import com.carrotsearch.util.CloseableUtils;

/**
 * A {@link INewsPostsCollector} saving the results to a database.
 */
public class DBCollector implements INewsPostsCollector
{
    /** Logger instance. */
    private final static Logger logger = Logger.getLogger(DBCollector.class);

    /**
     * Database to insert new nodes to.
     */
    private final DataSource dataSource;

    /**
     * Cache of recent entries so that we don't add duplicates.
     */
    private final ICache<String> cache;

    /**
     * Create a new database collector.
     */
    public DBCollector(DataSource dataSource, ICache<String> cache)
    {
        this.dataSource = dataSource;
        this.cache = (cache == null ? new FixedSizeCache(10000) : cache);
    }

    /**
     * Populate cache with recent posts on start.
     */
    @PostConstruct
    public void populateCache()
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            final PreparedStatement s = connection
                .prepareStatement("SELECT md5 FROM articles " + " WHERE added_at >= ? "
                    + " ORDER BY id DESC");
            try
            {
                final Calendar startFrom = Calendar.getInstance(TimeZone
                    .getTimeZone("GMT"));
                startFrom.add(Calendar.HOUR, -24);
                s.setTimestamp(1, new Timestamp(startFrom.getTimeInMillis()));

                final ResultSet rs = s.executeQuery();
                int fetched = 0;
                while (rs.next())
                {
                    final String md5 = rs.getString(1);
                    cache.put(md5);
                    fetched++;
                }

                logger.info("Fetched " + fetched + " recent posts to cache.");
            }
            finally
            {
                s.close();
            }
        }
        catch (SQLException e)
        {
            // Ignore.
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                }
            }
        }
    }

    /**
     * Add a batch of posts to the database.
     */
    public UpdateInfo add(Collection<INewsPost> posts, IFeed feed)
    {
        Connection connection = null;
        Statement s = null;
        int added = 0;
        int ignored = 0;
        try
        {
            connection = dataSource.getConnection();
            for (INewsPost post : posts)
            {
                if (!cache.contains(post.getId()) && !exists(connection, post))
                {
                    logger.info("Adding new post: " + post);
                    try
                    {
                        add(connection, post, feed);
                        cache.put(post.getId());
                    }
                    catch (SQLException e)
                    {
                        logger.warn("Adding post failed: " + post + " (" + e.toString()
                            + ")");
                    }
                    added++;
                }
                else
                {
                    logger.debug("Skipping existing post: " + post);
                    ignored++;
                }
            }
        }
        catch (SQLException e)
        {
            logger.error("Exception when inserting new feed nodes.", e);
        }
        finally
        {
            CloseableUtils.close(s);
            CloseableUtils.close(connection);
        }
        return new UpdateInfo(posts.size(), added, ignored);
    }

    /**
     * Adds a new post and its associated attributes.
     */
    private void add(Connection connection, INewsPost post, IFeed feed)
        throws SQLException
    {
        final String [] links = post.getLinks();

        if (links == null || links.length == 0)
        {
            // No links? Return immediately
            return;
        }

        // Now add properties of this post.
        final PreparedStatement s = connection
            .prepareStatement("INSERT INTO articles (url, title, content, posted_at, added_at, md5, fk_feed_id) "
                + "values (?, ?, ?, ?, ?, ?, ?)");

        try
        {
            s.setString(1, links[0]);
            s.setString(2, post.getTitle());
            s.setString(3, post.getDescription());
            s.setTimestamp(4, new Timestamp(post.getPublicationDate().getTime()));
            s.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            s.setString(6, post.getId());
            s.setLong(7, (Long) feed.getId());

            s.execute();
        }
        finally
        {
            s.close();
        }
    }

    /**
     * Checks if a given post already exists in the database.
     */
    private boolean exists(Connection connection, INewsPost post) throws SQLException
    {
        PreparedStatement s = connection
            .prepareStatement("SELECT count(*) FROM articles WHERE md5 = ?");
        try
        {
            s.setString(1, post.getId());
            final ResultSet rs = s.executeQuery();
            rs.next();

            final long count = rs.getLong(1);
            return count > 0;
        }
        finally
        {
            s.close();
        }
    }
}
