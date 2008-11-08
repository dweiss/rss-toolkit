package com.carrotsearch.rsstoolkit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.carrotsearch.util.CloseableUtils;

/**
 * Access to groups.
 */
public class GroupDAO
{
    private final static Logger logger = Logger.getLogger(GroupDAO.class);
    private DataSource dataSource;

    public GroupDAO(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * 
     */
    public void insert(Group group)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            ps = conn
                .prepareStatement("INSERT INTO events (id, title, expired, article_ids, article_count, intra_similarity, article_ids_full) "
                    + "VALUES             (?,  ?,     ?,       ?,           ?,             ?,                ?) ");

            ps.setInt(1, group.id);
            ps.setString(2, group.title);
            ps.setBoolean(3, group.expired);
            ps.setString(4, group.article_ids);
            ps.setInt(5, group.article_count);
            ps.setFloat(6, group.intra_similarity);
            ps.setString(7, group.article_ids_full);

            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            logger.error("Could not add new group: " + group.id + ", " + group.title, e);
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
    public void update(Group group)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            ps = conn.prepareStatement("UPDATE events SET " + " title = ?,"
                + " expired = ?," + " article_ids = ?, " + " article_count = ?, "
                + " intra_similarity = ?, " + " article_ids_full = ?, "
                + " updated_at = NOW() " + "WHERE id = ?");

            ps.setString(1, group.title);
            ps.setBoolean(2, group.expired);
            ps.setString(3, group.article_ids);
            ps.setInt(4, group.article_count);
            ps.setFloat(5, group.intra_similarity);
            ps.setString(6, group.article_ids_full);

            ps.setInt(7, group.id);

            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            logger.error("Could not add new group: " + group.id + ", " + group.title, e);
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
    public void setExpired(Integer id, boolean expiredFlag)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();

            ps = conn.prepareStatement("UPDATE events SET " + " expired = ? "
                + "WHERE id = ?");

            ps.setBoolean(1, expiredFlag);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            logger.error("Could not set expire on group: " + id, e);
        }
        finally
        {
            CloseableUtils.close(ps);
            CloseableUtils.close(conn);
        }
    }
}
