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
 * Full content of articles.
 */
public final class FullContentDAO
{
    private final DataSource dataSource;

    /*
     * 
     */
    public final static class FetchEntry
    {
        public final String uri;
        public final int articleId;

        FetchEntry(int articleId, String uri)
        {
            this.uri = uri;
            this.articleId = articleId;
        }
    }

    /**
     * 
     */
    public FullContentDAO(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * Fetch a batch of {@link Article}s to be fetched.
     */
    public List<FetchEntry> getFetchBatch(int batchSize)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            ps = conn
                .prepareStatement(
                    "SELECT article_id, articles.url "
                    + "FROM full_content, articles " 
                    + "WHERE fetched = FALSE "
                    + " AND article_id = articles.id "
                    + "ORDER BY article_id ASC "
                    + "LIMIT ?");

            ps.setInt(1, batchSize);

            final ResultSet rs = ps.executeQuery();
            final ArrayList<FetchEntry> docs = new ArrayList<FetchEntry>(rs.getFetchSize());
            while (rs.next())
            {
                docs.add(new FetchEntry(rs.getInt(1), rs.getString(2)));
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

    public void set(int articleId, boolean fetched, byte [] content, String contentType)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            ps = conn
                .prepareStatement(
                    "UPDATE full_content SET fetched = ?, content = ?, content_type = ? "
                    + " where article_id = ?"); 

            ps.setBoolean(1, fetched);
            ps.setBytes(2, content);
            ps.setString(3, contentType);
            ps.setInt(4, articleId);

            ps.executeUpdate();
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
