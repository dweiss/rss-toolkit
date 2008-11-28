package com.carrotsearch.rsstoolkit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.carrotsearch.util.CloseableUtils;

/**
 * Access to the articles.
 */
public final class ArticleDAO
{
    private final DataSource dataSource;

    /**
     * 
     */
    public ArticleDAO(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * Get all articles starting at the given identifier (inclusive), but not exceeding
     * the given limit of articles.
     */
    public List<Article> getFromId(int lastId, int limit)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            ps = conn
                .prepareStatement(
                    "SELECT id, url, title, content, posted_at, added_at, fk_feed_id "
                    + "FROM articles " + "WHERE id >= ?" + "ORDER BY id ASC LIMIT ?");

            ps.setInt(1, lastId);
            ps.setInt(2, limit);

            final ResultSet rs = ps.executeQuery();
            final ArrayList<Article> docs = new ArrayList<Article>(rs.getFetchSize());
            while (rs.next())
            {
                docs.add(new Article(rs.getInt(1), rs.getString(2), rs.getString(3), rs
                    .getString(4), rs.getTimestamp(5).getTime(), rs.getTimestamp(6)
                    .getTime(), rs.getInt(7)));
            }
            return docs;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Could not fetch articles: " + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(ps);
            CloseableUtils.close(conn);
        }
    }

    /**
     * Get all articles starting at the given identifier (inclusive). Articles should be
     * sorted in ascending order of their <code>posted_at</code> field.
     */
    public List<Article> getFromId(int lastId)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            ps = conn
                .prepareStatement("SELECT id, url, title, content, posted_at, added_at, fk_feed_id "
                    + "FROM articles " + "WHERE id >= ?" + "ORDER BY posted_at ASC");

            ps.setInt(1, lastId);

            final ResultSet rs = ps.executeQuery();
            final ArrayList<Article> docs = new ArrayList<Article>(rs.getFetchSize());
            while (rs.next())
            {
                docs.add(new Article(rs.getInt(1), rs.getString(2), rs.getString(3), rs
                    .getString(4), rs.getTimestamp(5).getTime(), rs.getTimestamp(6)
                    .getTime(), rs.getInt(7)));
            }
            return docs;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Could not fetch articles: " + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(ps);
            CloseableUtils.close(conn);
        }
    }

    /**
     * 
     */
    public int getLastId()
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            ps = conn.prepareStatement("SELECT max(id) " + "FROM articles");

            final ResultSet rs = ps.executeQuery();
            int lastId = 0;
            if (rs.next())
            {
                lastId = rs.getInt(1);
            }
            return lastId;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Could not fetch last ID: " + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(ps);
            CloseableUtils.close(conn);
        }
    }

    /**
     * 
     */
    public Article getById(Integer id)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            ps = conn
                .prepareStatement("SELECT id, url, title, content, posted_at, added_at, fk_feed_id "
                    + "FROM articles " + "WHERE id = ?");

            ps.setInt(1, id);

            final ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return new Article(rs.getInt(1), rs.getString(2), rs.getString(3), rs
                    .getString(4), rs.getTimestamp(5).getTime(), rs.getTimestamp(6)
                    .getTime(), rs.getInt(7));
            }

            return null;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Could not fetch article: " + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(ps);
            CloseableUtils.close(conn);
        }
    }

    /**
     * 
     */
    public Article [] getByIdList(int... ids)
    {
        if (ids.length == 0)
        {
            return new Article [0];
        }

        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            final ArrayList<Article> result = new ArrayList<Article>();
            int index = 0;

            while (index < ids.length)
            {
                final StringBuilder query = new StringBuilder();
                query
                    .append("SELECT id, url, title, content, posted_at, added_at, fk_feed_id "
                        + "FROM articles " + "WHERE id IN (");

                int i;
                final int maxInClauses = 127;
                for (i = 0; index + i < ids.length && i < maxInClauses; i++)
                {
                    if (i > 0) query.append(", ");
                    query.append(ids[index + i]);
                }
                index += i;

                query.append(") ORDER BY id ASC");

                ps = conn.prepareStatement(query.toString());

                final ResultSet rs = ps.executeQuery();
                while (rs.next())
                {
                    result.add(new Article(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getString(4), rs.getTimestamp(5).getTime(),
                        rs.getTimestamp(6).getTime(), rs.getInt(7)));
                }
            }

            return result.toArray(new Article [result.size()]);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Could not fetch articles: " + e.getMessage(), e);
        }
        finally
        {
            CloseableUtils.close(ps);
            CloseableUtils.close(conn);
        }
    }
}
