package com.carrotsearch.rsstoolkit.aggregator;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.carrotsearch.rsstoolkit.aggregator.queue.IFeedQueue;
import com.carrotsearch.util.time.Utils;

/**
 * <p>
 * This servlet starts an {@link Aggregator} service in the background of a running Web
 * application.
 */
@SuppressWarnings("serial")
public final class AggregatorServlet extends HttpServlet
{
    /**
     * Formatter for {@link #toString()}.
     */
    private final static SimpleDateFormat formatter = Utils.createCommonDateFormat();

    /**
     * Logger instance.
     */
    private final static Logger logger = Logger.getLogger(AggregatorServlet.class);

    /**
     * Aggregator spawned from this service.
     */
    private Aggregator aggregator;

    /**
     * Initialize the service.
     */
    @Override
    public void init() throws ServletException
    {
        super.init();

        initAggregator();
    }

    /*
     * 
     */
    private void initAggregator() throws ServletException
    {
        logger.info("Starting...");
        final Aggregator aggregator;
        try
        {
            final ApplicationContext context = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());

            aggregator = (Aggregator) context.getBean("aggregator");
        }
        catch (RuntimeException e)
        {
            logger.error("Failed to start: " + e.getMessage(), e);
            throw new ServletException("Failed to start: " + e.getMessage(), e);
        }

        aggregator.start();
        this.aggregator = aggregator;
    }

    /**
     * Return some basic statistics about the queue.
     */
    @Override
    protected synchronized void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        final IFeedQueue queue = aggregator.getFeedQueue();

        resp.setContentType("text/html; charset=UTF-8");
        final Writer out = resp.getWriter();
        out
            .write("<html><head><link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\""
                + req.getContextPath() + "/css/style.css" + "\" /></head><body>");

        out.write("<h1>Meadan RSS/Atom Aggregator</h1>");

        final long NOW = System.currentTimeMillis();
        final long NEXT = queue.getClosestFetchTimestamp();

        out.write("<p>Feeds: <b>" + queue.getFeedsCount() + "</b></p>");
        out.write("<p>Time now: <b>" + formatter.format(new Date(NOW)) + "</b></p>");
        out.write("<p>Next fetch: <b>" + formatter.format(new Date(NEXT)) + "</b>" + " ("
            + (NEXT - NOW > 0 ? "in " : "running for ")
            + Utils.getDurationAsString(Math.abs(NEXT - NOW)) + ")" + "</p>");

        out.write("</table>");

        out.write("</body></html>");
        out.flush();
    }

    /**
     * Stop the service.
     */
    @Override
    public void destroy()
    {
        super.destroy();

        // Stop the aggregator service.
        if (this.aggregator != null)
        {
            this.aggregator.stop();
        }

        // Flush current feed queue.
        this.aggregator = null;
    }
}
