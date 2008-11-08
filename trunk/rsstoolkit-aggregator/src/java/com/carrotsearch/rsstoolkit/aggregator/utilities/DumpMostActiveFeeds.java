package com.carrotsearch.rsstoolkit.aggregator.utilities;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.carrotsearch.util.CloseableUtils;

/**
 * Dump most active feeds and examples of their articles.
 */
public final class DumpMostActiveFeeds
{
    private final static Logger logger = Logger.getLogger(DumpMostActiveFeeds.class);

    /**
     * 
     */
    public void dumpMostActiveFeeds(DataSource ds, PrintWriter out) throws SQLException
    {
        final Connection conn = ds.getConnection();
        Statement s = null;
        ResultSet rs = null;
        try
        {
            s = conn.createStatement();
            rs = s.executeQuery("select feeds.id, count(*), feeds.title"
                + "  from feeds, articles" + "  where articles.fk_feed_id = feeds.id "
                + "  group by feeds.id, feeds.title " + "  having count(*) > 100 "
                + "  order by 2 desc");

            final ResultSetMetaData meta = rs.getMetaData();
            final int columns = meta.getColumnCount();
            final ArrayList<Object []> feeds = fetchRows(rs, columns);

            out.println("# " + Arrays.toString(getColumnLabels(meta)));
            out.println("----");

            int counter = 0;
            for (Object [] row : feeds)
            {
                out.println("# -- FEED #" + counter);
                out.println(row[0] + "\t" + row[1] + "\t" + oneliner((String) row[2]));

                final ArrayList<Object []> examples = fetchExamples(((Number) row[0])
                    .intValue(), s);
                for (Object [] example : examples)
                {
                    out.println("\t t: " + oneliner((String) example[1]) + ", c: "
                        + oneliner((String) example[2]));
                }
                counter++;

                if ((counter % 10) == 0)
                {
                    logger.info("Dumped " + counter + " feeds.");
                }
            }
        }
        finally
        {
            CloseableUtils.close(s);
            CloseableUtils.close(conn);
        }

        logger.info("Finished.");
    }

    /**
     * 
     */
    private ArrayList<Object []> fetchExamples(int feed_pk, Statement s)
        throws SQLException
    {
        final ResultSet rs = s.executeQuery("select id, title, content from articles "
            + "  where fk_feed_id = " + feed_pk + "  order by id desc " + "  limit 20");

        return fetchRows(rs, rs.getMetaData().getColumnCount());
    }

    private final static Pattern CLEANUP = Pattern.compile(
        "[^\\p{L}&&\\p{Punct}&&\\p{Digit}]", Pattern.UNICODE_CASE);

    /**
     * Formats the string on one line (removing unnecessary spaces etc.).
     */
    private String oneliner(String s)
    {
        String p = CLEANUP.matcher(s.replaceAll("[\n\r]+", " ")).replaceAll("_").trim();
        if (p.length() > 160)
        {
            p = p.substring(0, 160) + "...";
        }
        return p;
    }

    /**
     * 
     */
    private String [] getColumnLabels(ResultSetMetaData meta) throws SQLException
    {
        final String [] labels = new String [meta.getColumnCount()];
        for (int i = 0; i < labels.length; i++)
        {
            labels[i] = meta.getColumnLabel(i + 1);
        }
        return labels;
    }

    /**
     * Fetch all rows from the result set.
     */
    private ArrayList<Object []> fetchRows(ResultSet rs, int columnCount)
        throws SQLException
    {
        final ArrayList<Object []> result = new ArrayList<Object []>(rs.getFetchSize());
        while (rs.next())
        {
            result.add(fetchRow(rs, columnCount));
        }
        return result;
    }

    /**
     * Fetch a single row from the result set.
     */
    private Object [] fetchRow(ResultSet rs, final int columns) throws SQLException
    {
        final Object [] results = new Object [columns];
        for (int i = 0; i < columns; i++)
        {
            results[i] = rs.getObject(i + 1);
        }
        return results;
    }

    /**
     * Command line entry point.
     */
    public static void main(String [] args) throws Exception
    {
        if (args.length != 1)
        {
            System.err.println("Args: [app-context]");
            return;
        }

        final ApplicationContext ctx = new FileSystemXmlApplicationContext(args[0]);
        final DataSource ds = (DataSource) ctx.getBean("dataSource");

        final PrintWriter pw = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream("feeds.log"), "UTF-8"));

        new DumpMostActiveFeeds().dumpMostActiveFeeds(ds, pw);

        pw.close();
    }
}
