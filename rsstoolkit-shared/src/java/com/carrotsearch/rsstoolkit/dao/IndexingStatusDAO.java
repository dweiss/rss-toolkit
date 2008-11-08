package com.carrotsearch.rsstoolkit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.carrotsearch.util.CloseableUtils;

/**
 * Access to indexing status.
 */
public final class IndexingStatusDAO
{
    private final DataSource dataSource;

    /**
     * 
     */
    public IndexingStatusDAO(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * Returns a hash map of all categories.
     */
    public String getField(String field) throws SQLException
    {
        final Connection conn = dataSource.getConnection();
        try
        {
            final PreparedStatement ps = conn.prepareStatement("SELECT value "
                + "FROM indexing_status " + "WHERE field = ?");

            ps.setString(1, field);

            final ResultSet rs = ps.executeQuery();
            final String value;
            if (rs.next())
            {
                value = rs.getString(1);
            }
            else
            {
                value = null;
            }

            return value;
        }
        finally
        {
            CloseableUtils.close(conn);
        }
    }

    /**
     * Updates a given field's value, adds it if it didn't exist.
     */
    public void updateField(String field, String value) throws SQLException
    {
        final Connection conn = dataSource.getConnection();
        try
        {
            final PreparedStatement ps = conn
                .prepareStatement("UPDATE indexing_status SET value = ? "
                    + "WHERE field = ?");

            ps.setString(1, value);
            ps.setString(2, field);

            if (1 != ps.executeUpdate())
            {
                // The row did not exist, add it.
                final PreparedStatement as = conn
                    .prepareStatement("INSERT INTO indexing_status (field, value) VALUES (?, ?)");
                as.setString(1, field);
                as.setString(2, value);
                as.executeUpdate();
            }
        }
        finally
        {
            CloseableUtils.close(conn);
        }
    }
}
