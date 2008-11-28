package com.carrotsearch.rsstoolkit.fetcher;

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.carrotsearch.rsstoolkit.dao.FullContentDAO;
import com.carrotsearch.rsstoolkit.dao.FullContentDAO.FetchEntry;
import com.carrotsearch.util.httpclient.HttpUtils;
import com.carrotsearch.util.httpclient.HttpUtils.Response;

/**
 * A job for the Quartz scheduler, periodically invoking the fetcher.
 */
public final class FetcherJob
{
    private final Logger logger = Logger.getLogger(FetcherJob.class);

    /** */
    private final FullContentDAO fullContentDAO;

    /**
     * 
     */
    public FetcherJob(DataSource dataSource)
    {
        this.fullContentDAO = new FullContentDAO(dataSource);
    }

    /**
     * Called by the scheduler.
     */
    public void execRound()
    {
        /*
         * Retrieve batches of articles starting from the given ID.
         */
        final int batchSize = 10;
        List<FetchEntry> fetchEntries;
        do
        {
            if (Thread.currentThread().isInterrupted())
            {
                // Clear interrupted status.
                Thread.interrupted();
                break;
            }

            fetchEntries = fullContentDAO.getFetchBatch(batchSize);
            for (FetchEntry entry : fetchEntries)
            {
                byte [] content;
                String contentType = "";
                try
                {
                    Response response = HttpUtils.doGET(entry.uri, null, null);
                    content = response.payload;

                    for (String [] headers : response.headers)
                    {
                        if ("content-type".equals(headers[0].toLowerCase().trim()))
                        {
                            contentType = headers[1];
                        }
                    }
                    logger.debug("Fetched: " + entry.uri);                    
                }
                catch (Throwable e)
                {
                    logger.info("Fetching error: " + entry.uri + " (" + e.getMessage() + ")");

                    // Any exception means skip the URI.
                    content = null;
                    contentType = null;
                }

                fullContentDAO.set(entry.articleId, true, content, contentType);
            }
        } while (!fetchEntries.isEmpty());
    }
}
