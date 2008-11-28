package com.carrotsearch.rsstoolkit.fetcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>
 * This servlet starts the content fetcher service.
 */
@SuppressWarnings("serial")
public final class FetcherServlet extends HttpServlet
{
    /**
     * Logger instance.
     */
    private final static Logger logger = Logger.getLogger(FetcherServlet.class);

    private Scheduler scheduler;

    /**
     * Initialize the service.
     */
    @Override
    public void init() throws ServletException
    {
        initPollingThread();
    }

    /*
     * 
     */
    private void initPollingThread() throws ServletException
    {
        logger.info("Starting...");
        try
        {
            WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        }
        catch (RuntimeException e)
        {
            logger.error("Failed to start: " + e.getMessage(), e);
            throw new ServletException("Failed to start: " + e.getMessage(), e);
        }
    }

    /**
     * Stop the service.
     */
    @Override
    public void destroy()
    {
        super.destroy();

        try
        {
            if (scheduler != null)
            {
                this.scheduler.shutdown(true);
            }
        }
        catch (SchedulerException e)
        {
            logger.fatal("Failure shutting down the scheduler.", e);
        }
    }
}
