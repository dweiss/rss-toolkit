package com.carrotsearch.rsstoolkit.aggregator.queue;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.carrotsearch.rsstoolkit.aggregator.collectors.UpdateInfo;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedParsersFactory;
import com.carrotsearch.util.CloseableUtils;
import com.carrotsearch.util.time.Utils;

/**
 * <p>
 * A feed queue on top of relational database.
 */
public final class DBFeedQueue implements IFeedQueue
{
    /**
     * Logger for this class.
     */
    private final static Logger logger = Logger.getLogger(DBFeedQueue.class);

    /**
     * Minimum update interval. Do not change, this field is public for tests only.
     */
    public long MINIMUM_UPDATE_INTERVAL = 5 * Utils.MINUTE;

    /**
     * Maximum update interval (for adaptive feeds). Do not change, this field is public
     * for tests only.
     */
    public long MAXIMUM_UPDATE_INTERVAL = 24 * Utils.HOUR;

    /**
     * Default update interval until some adaptive data is received. Do not change, this
     * field is public for tests only.
     */
    public long DEFAULT_UPDATE_INTERVAL = 1 * Utils.HOUR;

    /**
     * Data source for db access.
     */
    private final DataSource ds;

    /**
     * Feed parsers factory.
     */
    private final IFeedParsersFactory feedParsersFactory;

    /**
     * A public initializing constructor.
     */
    public DBFeedQueue(DataSource dataSource, IFeedParsersFactory factory)
    {
        this.ds = dataSource;
        this.feedParsersFactory = factory;
    }

    /**
     * @return Returns a collection of {@link IFeed}s that need to be updated. All feeds
     *         which have an expected fetch time lower than <code>now</code> will be
     *         returned.
     */
    public Collection<IFeed> getStaleFeeds(final long now)
    {
        final Timestamp ts = new Timestamp(now);
        final ArrayList<IFeed> stale = new ArrayList<IFeed>();

        class QueueEntry
        {
            public final String URL;
            public final long id;
            
            public QueueEntry(String URL, long id)
            {
                this.id = id;
                this.URL = URL;
            }
        };
        
        // Run query.
        Connection conn = null;
        PreparedStatement ps = null;
        final ArrayList<QueueEntry> queue = new ArrayList<QueueEntry>();
        try
        {
            final String STALE_FEEDS = " SELECT id, url" + " FROM feeds " + " WHERE "
                + "   NOT ignore " + "   AND status != " + FeedStatus.HTTP_NOT_FOUND.id()
                + "   AND status != " + FeedStatus.UNRECOVERABLE_ERROR.id()
                + "   AND (next_fetch IS NULL OR next_fetch < ?) "
                + " ORDER BY next_fetch ASC";

            conn = ds.getConnection();
            ps = conn.prepareStatement(STALE_FEEDS);
            ps.setTimestamp(1, ts);
            final ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                final long id = rs.getLong(1);
                final String url = rs.getString(2);
                queue.add(new QueueEntry(url, id));
            }
            rs.close();

            // Update incorrect feeds.
            for (QueueEntry q : queue)
            {
                try
                {
                    stale.add(getFeed(q.id, q.URL));
                }
                catch (URISyntaxException e)
                {
                    updateFeedStatus0(conn, q.id, FeedStatus.UNRECOVERABLE_ERROR,
                        "URI parsing error: " + q.URL);
                    continue;
                }
            }
        }
        catch (SQLException e)
        {
            logger.error("Unexpected SQL error: " + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(conn);
            CloseableUtils.close(ps);
        }
        return stale;
    }

    /**
     * @return Returns the time of the closest expected feed fetch or
     *         <code>now + 1 minute</code> if there is no next element to fetch.
     */
    public long getClosestFetchTimestamp()
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            final String NEXT_FETCH = " SELECT MIN(COALESCE(next_fetch, NOW()))"
                + " FROM feeds " + " WHERE " + "   NOT ignore " + "   AND status != "
                + FeedStatus.HTTP_NOT_FOUND.id() + "   AND status != "
                + FeedStatus.UNRECOVERABLE_ERROR.id();

            conn = ds.getConnection();
            ps = conn.prepareStatement(NEXT_FETCH);
            final ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getTimestamp(1).getTime();
            }
            else
            {
                // No next fetch, just set an arbitrary next timestamp.
                return System.currentTimeMillis() + Utils.MINUTE;
            }
        }
        catch (SQLException e)
        {
            logger.error("Unexpected SQL error: " + e.getMessage(), e);
            throw new RuntimeException("Unexpected SQL error: " + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(conn);
            CloseableUtils.close(ps);
        }
    }

    /**
     * @return Returns a count of all feeds present in the queue.
     */
    public long getFeedsCount()
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            final String FEED_COUNT = " SELECT COUNT(*)" + " FROM feeds " + " WHERE "
                + "   NOT ignore " + "   AND status != " + FeedStatus.HTTP_NOT_FOUND.id()
                + "   AND status != " + FeedStatus.UNRECOVERABLE_ERROR.id();

            conn = ds.getConnection();
            ps = conn.prepareStatement(FEED_COUNT);
            final ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getLong(1);
            }
            else
            {
                logger.warn("No count returned from getFeedsCount()?");
                return 0;
            }
        }
        catch (SQLException e)
        {
            logger.error("Unexpected SQL error: " + e.getMessage(), e);
            throw new RuntimeException("Unexpected SQL error: " + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(conn);
            CloseableUtils.close(ps);
        }
    }

    /**
     * Reschedule a feed adaptively, depending on the <code>ttl</code> value (if greater
     * than zero) and the history of updates.
     * 
     * @param ttl Expected number of milliseconds in which the next update should occur or
     *            zero.
     */
    public void updateSchedule(IFeed feed, final long ttl, UpdateInfo updateInfo)
    {
        final long NOW = System.currentTimeMillis();
        final long feedId = (Long) feed.getId();

        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = ds.getConnection();

            final FeedMetadata metadata;
            ps = conn.prepareStatement("SELECT id, metadata " + " FROM feeds "
                + " WHERE id = ?");
            ps.setLong(1, feedId);
            final ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                metadata = deserialize(rs.getBytes(2));
                logger.debug("Metadata deserialized: " + metadata.fetch_times.size());
            }
            else
            {
                metadata = new FeedMetadata();
            }

            if (updateInfo.added > 0)
            {
                metadata.addFetchTime(NOW);
            }

            final long avgUpdateInterval = metadata.getAverageUpdateInterval();
            final long nextUpdateOffset;
            if (ttl > 0)
            {
                nextUpdateOffset = ttl;
            }
            else
            {
                if (avgUpdateInterval > 0)
                {
                    nextUpdateOffset = avgUpdateInterval;
                }
                else
                {
                    nextUpdateOffset = DEFAULT_UPDATE_INTERVAL;
                }
            }

            final long nextUpdateTimestamp = NOW
                + Math.max(MINIMUM_UPDATE_INTERVAL, Math.min(MAXIMUM_UPDATE_INTERVAL,
                    nextUpdateOffset));

            ps = conn.prepareStatement("UPDATE feeds " + " SET next_fetch = ?,"
                + "     last_fetch = ?," + "     status = ?,"
                + "     status_text = NULL, " + "     errors = 0, "
                + "     adaptive_ttl = ?," + "     ttl = ?," + "     metadata = ?"
                + " WHERE id = ?");

            ps.setTimestamp(1, new Timestamp(nextUpdateTimestamp));
            ps.setTimestamp(2, new Timestamp(NOW));
            ps.setInt(3, FeedStatus.CRAWLED_OK.id());
            ps.setInt(4, (int) avgUpdateInterval);
            ps.setInt(5, (int) ttl);
            ps.setBytes(6, serialize(metadata));
            ps.setLong(7, feedId);

            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            logger.error("Could not update schedule of feed " + feedId + ": "
                + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(ps);
            CloseableUtils.close(conn);
        }
    }

    /**
     * Deserialize metadata from a byte stream.
     */
    private FeedMetadata deserialize(byte [] bytes)
    {
        if (bytes == null)
        {
            return new FeedMetadata();
        }

        try
        {
            final ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
            final ObjectInputStream os = new ObjectInputStream(baos);
            FeedMetadata fm = (FeedMetadata) os.readObject();
            os.close();

            return fm;
        }
        catch (Exception e)
        {
            logger.warn("Could not deserialize metadata: " + e.toString());
            return new FeedMetadata();
        }
    }

    /**
     * Serialize metadata to a byte stream.
     */
    private byte [] serialize(FeedMetadata ob)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream os = new ObjectOutputStream(baos);
            os.writeObject(ob);
            os.close();

            return baos.toByteArray();
        }
        catch (IOException e)
        {
            logger.warn("Could not serialize metadata: " + e.toString());
            return new byte [0];
        }
    }

    /**
     * Produce an {@link IFeed} for a given id, url pair.
     */
    private IFeed getFeed(Long id, String url) throws URISyntaxException
    {
        return this.feedParsersFactory.getFeedParser(new URI(url), id);
    }

    /**
     * Update feed status and change the next fetch time according to error type.
     */
    public void updateFeedStatus(IFeed feed, FeedStatus status, String message)
    {
        long feedid = (Long) feed.getId();
        long t;

        Connection conn = null;
        try
        {
            conn = ds.getConnection();

            switch (status)
            {
                case NEW:
                case CRAWLED_OK:
                    throw new RuntimeException("Status not expected here: " + status);

                case HTTP_NOT_FOUND:
                case CONNECTION_TIMEOUT:
                case RECOVERABLE_ERROR:
                    t = (3 * Utils.HOUR) / Utils.SECOND;
                    updateErrorFetchTime0(conn, feedid, t);
                    break;

                case UNRECOVERABLE_ERROR:
                    // Permanent error, just set the update to one year from now.
                    t = (Utils.DAY * 365) / Utils.SECOND;
                    updateErrorFetchTime0(conn, feedid, t);
                    break;

                default:
                    throw new RuntimeException("Status unknown: " + status);
            }

            updateFeedStatus0(conn, feedid, status, message);
        }
        catch (SQLException e)
        {
            logger.error("Could not update feed status: " + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(conn);
        }
    }

    /**
     * Update the next fetch time for the feed in case of an error. The delayPeriod is
     * multiplied by the number of times the feed received an error.
     */
    private void updateErrorFetchTime0(Connection conn, long feedId,
        long delayPeriodSeconds) throws SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = conn.prepareStatement("UPDATE feeds " + " SET next_fetch = "
                + "     NOW() + (COALESCE(errors, 0) + 1) * (INTERVAL '"
                + delayPeriodSeconds + " seconds')" + " WHERE id = ?");

            ps.setLong(1, feedId);

            ps.executeUpdate();
        }
        finally
        {
            CloseableUtils.close(ps);
        }
    }

    /**
     * Update feed status in the DB.
     */
    private void updateFeedStatus0(Connection conn, Long feedId, FeedStatus status,
        String message) throws SQLException
    {
        boolean incrementErrorCount = false;
        switch (status)
        {
            case UNRECOVERABLE_ERROR:
            case RECOVERABLE_ERROR:
            case HTTP_NOT_FOUND:
            case CONNECTION_TIMEOUT:
                incrementErrorCount = true;
        }

        PreparedStatement ps = null;
        try
        {
            ps = conn.prepareStatement("UPDATE feeds " + " SET " + "   status = ?,"
                + "   status_text = ?"
                + (incrementErrorCount ? ", errors = COALESCE(errors,0) + 1" : "")
                + " WHERE id = ?");

            ps.setInt(1, status.id());
            ps.setString(2, message);
            ps.setLong(3, feedId);

            ps.executeUpdate();
        }
        finally
        {
            CloseableUtils.close(ps);
        }
    }
}
