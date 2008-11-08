package com.carrotsearch.rsstoolkit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.sql.DataSource;

import com.carrotsearch.util.CloseableUtils;

public final class CategoryDAO
{
    private final DataSource dataSource;

    /**
     * 
     */
    public CategoryDAO(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * Returns a hash map of all categories.
     */
    public HashMap<Integer, Category> getCategories() throws SQLException
    {
        final Connection conn = dataSource.getConnection();
        try
        {
            final HashMap<Integer, Category> result = new HashMap<Integer, Category>();

            final PreparedStatement ps = conn
                .prepareStatement("SELECT id, name, category_id " + "FROM categories "
                    + "ORDER BY id ASC ");

            final ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                final int id = rs.getInt(1);
                final String name = rs.getString(2);
                final Integer parentId = (Integer) rs.getObject(3);

                Category parent = null;
                if (parentId != null)
                {
                    parent = result.get(parentId);
                    if (parent == null) throw new RuntimeException("Parent not found.");
                }

                result.put(id, new Category(id, name, parent));
            }

            return result;
        }
        finally
        {
            CloseableUtils.close(conn);
        }
    }

}
