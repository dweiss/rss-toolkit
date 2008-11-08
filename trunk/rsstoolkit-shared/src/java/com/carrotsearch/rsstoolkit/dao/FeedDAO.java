package com.carrotsearch.rsstoolkit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.carrotsearch.util.CloseableUtils;

public final class FeedDAO
{
    private final DataSource dataSource;

    /**
     * 
     */
    public FeedDAO(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * Return current feeds and all categories assigned to them (including parent
     * categories).
     */
    public HashMap<Integer, Feed> fetchFeeds() throws SQLException
    {
        final Connection conn = dataSource.getConnection();
        try
        {
            final HashMap<Integer, Feed> feeds = new HashMap<Integer, Feed>();

            final PreparedStatement ps = conn
                .prepareStatement("SELECT feeds.id, flat_names, feeds.url "
                    + "FROM feeds, auto_categories_flat, categories_feeds "
                    + "WHERE feeds.id = categories_feeds.feed_id "
                    + "      AND auto_categories_flat.id = categories_feeds.category_id "
                    + "ORDER BY feeds.id ASC ");

            final Pattern pattern = Pattern.compile(";", Pattern.LITERAL);
            final ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                final int feedId = rs.getInt(1);
                final String [] categories = pattern.split(rs.getString(2));
                final String url = rs.getString(3);

                Feed feed = feeds.get(feedId);
                if (feed == null)
                {
                    feed = new Feed(feedId, url, Arrays.asList(categories));
                    feeds.put(feedId, feed);
                }
            }

            return feeds;
        }
        finally
        {
            CloseableUtils.close(conn);
        }
    }
}
