package com.carrotsearch.rsstoolkit.aggregator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.carrotsearch.rsstoolkit.aggregator.collectors.INewsPostsCollector;
import com.carrotsearch.rsstoolkit.aggregator.collectors.UpdateInfo;
import com.carrotsearch.rsstoolkit.aggregator.feeds.FeedFetchingException;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedFetchResults;
import com.carrotsearch.rsstoolkit.aggregator.feeds.FeedFetchingException.REASON;
import com.carrotsearch.rsstoolkit.aggregator.queue.FeedStatus;
import com.carrotsearch.rsstoolkit.aggregator.queue.IFeedQueue;
import com.carrotsearch.util.time.Utils;

/**
 * <p>
 * Article aggregator. Periodically pulls data from {@link IFeed}s and stores the fetched
 * content in a {@link Collector}.
 */
public final class Aggregator
{
    /** Logger. */
    private final static Logger logger = Logger.getLogger(Aggregator.class);

    /**
     * Minimum delay between feed polls. Package scope for tests.
     */
    long MINIMUM_FETCH_INTERVAL = 30 * Utils.SECOND;

    /**
     * Initial delay before the first fetch round starts. Package scope for tests.
     */
    long INITIAL_DELAY = 5 * Utils.SECOND;

    /**
     * Maximum number of concurrent fetcher threads.
     */
    private int fetcherThreads = 20;

    /**
     * A (forced) timeout for a single connection to an RSS feed.
     */
    private final static long CONNECTION_TIMEOUT = 1 * Utils.MINUTE;

    /**
     * A maximum time for a single round of fetches. If exceeded, a warning is issued
     * (nothing else happens).
     */
    private final static long MAX_FETCH_ROUND_TIME = 10 * Utils.MINUTE;

    /**
     * Feed queue ordering feeds to be fetched.
     */
    private final IFeedQueue feedQueue;

    /**
     * Queue processor thread.
     */
    private volatile Thread processor;

    /**
     * Collector for posts fetched from {@link #feedQueue}'s sources.
     */
    private final INewsPostsCollector collector;

    /**
     * Listeners interested to hear about the aggregator's lifecycle events.
     */
    private final ArrayList<AggregatorListener> listeners = new ArrayList<AggregatorListener>();

    /**
     * Executor executor used to fetch content from {@link IFeed}s.
     */
    private ExecutorService executor;

    /**
     * Creates a new aggregator executor.
     * 
     * @param An initialized {@link MemFeedQueue} object.
     */
    public Aggregator(IFeedQueue feedQueue, INewsPostsCollector collector)
    {
        this.feedQueue = feedQueue;
        this.collector = collector;
    }

    /**
     * Initializes and starts the aggregator executor.
     */
    public synchronized void start()
    {
        if (processor != null)
        {
            throw new IllegalStateException("Already started.");
        }

        if (executor != null && executor.isShutdown())
        {
            throw new IllegalStateException("Already terminated.");
        }

        logger.info("Initializing [fetcherThreads: " + fetcherThreads + "]");
        executor = Executors.newFixedThreadPool(fetcherThreads);

        this.processor = new Thread("Aggregator")
        {
            public void run()
            {
                Aggregator.this.run();
            }
        };
        processor.start();

        // Notify listeners.
        for (final AggregatorListener l : listeners)
        {
            try
            {
                l.started(this);
            }
            catch (Throwable t)
            {
                logger.warn("Listener threw an exception.", t);
            }
        }
    }

    /**
     * Destroys the aggregator and releases any used resources. This method may block for
     * certain amount of time.
     */
    public void stop()
    {
        final Thread tempRef;
        synchronized (this)
        {
            if (processor == null)
            {
                throw new IllegalStateException("Not started.");
            }

            // Interrupt the processor and wait for it to finish.
            tempRef = this.processor;
            this.processor = null;
            tempRef.interrupt();
        }

        try
        {
            logger.debug("Waiting for the processor to finish.");
            tempRef.join();
        }
        catch (InterruptedException e)
        {
            logger.warn("Waiting for the processor interrupted.");
        }

        synchronized (this)
        {
            // Notify listeners.
            for (final AggregatorListener l : listeners)
            {
                try
                {
                    l.stopped(this);
                }
                catch (Throwable t)
                {
                    logger.warn("Listener threw an exception.", t);
                }
            }

            logger.info("Aggregator stopped.");
        }
    }

    /**
     *
     */
    public synchronized void addListener(AggregatorListener l)
    {
        if (!listeners.contains(l))
        {
            listeners.add(l);
        }
    }

    /**
     * 
     */
    public synchronized void removeListener(AggregatorListener l)
    {
        listeners.remove(l);
    }

    /**
     * Queue processor thread main loop.
     */
    final void run()
    {
        final Logger fetchloop = Logger.getLogger(this.getClass().getName()
            + ".fetchloop");
        try
        {
            // Start with some delay to allow other processes to warm up.
            long nextFetch = System.currentTimeMillis() + INITIAL_DELAY;
            while (this.processor == Thread.currentThread())
            {
                // Wait until next fetch time.
                synchronized (Aggregator.this)
                {
                    do
                    {
                        final long pause = nextFetch - System.currentTimeMillis();

                        if (pause <= 0)
                        {
                            break;
                        }

                        if (fetchloop.isInfoEnabled())
                        {
                            final MessageFormat formatter = new MessageFormat(
                                "Next: {0,date,yyyy/MM/dd/HH:mm:ss}, pausing for {1}",
                                Locale.ENGLISH);
                            fetchloop.info(formatter.format(new Object []
                            {
                                new Date(nextFetch), Utils.getDurationAsString(pause)
                            }));
                        }
                        final int BORDERCASE_EXTRA_DELAY = 100;
                        Aggregator.this.wait(BORDERCASE_EXTRA_DELAY + pause);
                    }
                    while (true);
                }

                final long now = System.currentTimeMillis();
                try
                {
                    fetchRound(fetchloop);

                    // Notify listeners.
                    for (final AggregatorListener l : listeners)
                    {
                        try
                        {
                            l.roundFinished(this);
                        }
                        catch (Throwable t)
                        {
                            fetchloop.warn("Listener threw an exception.", t);
                        }
                    }
                }
                finally
                {
                    final long duration = System.currentTimeMillis() - now;
                    if (fetchloop.isInfoEnabled())
                    {
                        fetchloop.info("Fetch round duration: "
                            + Utils.getDurationAsString(duration));
                    }

                    if (duration > MAX_FETCH_ROUND_TIME)
                    {
                        fetchloop.warn("Fetch round too long by: "
                            + Utils.getDurationAsString(duration - MAX_FETCH_ROUND_TIME));
                    }
                }

                nextFetch = estimateNextFetch();
            }
        }
        catch (InterruptedException e)
        {
            if (this.processor == Thread.currentThread())
            {
                fetchloop.error("Interrupted without explicit call to stop().");
            }
            else
            {
                fetchloop.debug("Interrupted.");
            }

            fetchloop.debug("Stopping the thread pool.");
            if (!executor.isShutdown())
            {
                final List<Runnable> remaining = executor.shutdownNow();
                if (remaining.size() > 0)
                {
                    fetchloop.warn("Discarding fetch jobs: " + remaining.size());
                }
            }
            fetchloop.info("Finished.");
        }
        catch (Throwable t)
        {
            fetchloop.fatal("Uncaught exception in fetcher thread.", t);
        }
    }

    /**
     * A single fetch round. See if there are any feeds that need to be updated and
     * download their content/ update collector.
     */
    private void fetchRound(final Logger l) throws InterruptedException
    {
        final Collection<IFeed> feeds = feedQueue.getStaleFeeds(System
            .currentTimeMillis());

        l.info("Fetch round started [" + feedQueue.getFeedsCount() + " total feeds, "
            + feeds.size() + " stale].");

        final CountDownLatch countDownLatch = new CountDownLatch(feeds.size());

        /*
         * Keep track of the opened connections and force interrupt if a connection hangs.
         * WORKAROUND FOR: opening socket connections that timeout after a long time,
         * opened socket connections for which remote server does not return anything.
         */
        final class ConnectionEntry
        {
            public final Thread thread;
            public final long expiresAt;

            public ConnectionEntry(final long expiresAt)
            {
                this.thread = Thread.currentThread();
                this.expiresAt = expiresAt;
            }
        }

        final ArrayList<ConnectionEntry> openConnections = new ArrayList<ConnectionEntry>(
            feeds.size());

        for (final IFeed feed : feeds)
        {
            final Runnable fetcher = new Runnable()
            {
                public void run()
                {
                    final long ttl;
                    int openConnectionIndex = -1;
                    UpdateInfo updateInfo = null;
                    try
                    {
                        final IFeedFetchResults fetchResult;
                        try
                        {
                            l.debug("Fetching: " + feed.getId());
                            synchronized (openConnections)
                            {
                                openConnectionIndex = openConnections.size();
                                openConnections.add(new ConnectionEntry(
                                    CONNECTION_TIMEOUT + System.currentTimeMillis()));
                            }

                            fetchResult = feed.fetch();
                        }
                        finally
                        {
                            synchronized (openConnections)
                            {
                                if (openConnectionIndex >= 0)
                                {
                                    openConnections.set(openConnectionIndex, null);
                                }
                                if (Thread.interrupted())
                                {
                                    throw new InterruptedException("Timeouted.");
                                }
                            }
                        }

                        // Collect posts from this feed.
                        updateInfo = collector.add(fetchResult.getPosts(), feed);
                        ttl = fetchResult.getUpdateInterval();
                        l.debug("Fetch ok: " + feed.getId() + " [" + updateInfo + "]");

                        // Update the feed queue with new information about this feed.
                        feedQueue.updateSchedule(feed, ttl, updateInfo);
                    }
                    catch (InterruptedException e)
                    {
                        l.info("Fetch timeouted: " + feed.getId());
                        feedQueue.updateFeedStatus(feed, FeedStatus.CONNECTION_TIMEOUT,
                            "Connection timeout.");
                    }
                    catch (FeedFetchingException e)
                    {
                        if (e.reason == REASON.IO || e.reason == REASON.INVALID_XML
                            || e.reason == REASON.HTTP_OTHER)
                        {
                            l.warn("Recoverable I/O error: " + feed.getId() + " ("
                                + e.toString() + ")");
                            feedQueue.updateFeedStatus(feed,
                                FeedStatus.RECOVERABLE_ERROR, e.toString());
                        }
                        else
                        {
                            l.warn("Unrecoverable feed error: " + feed.getId() + " ("
                                + e.toString() + ")");
                            feedQueue.updateFeedStatus(feed,
                                FeedStatus.UNRECOVERABLE_ERROR, e.toString());
                        }
                    }
                    catch (Throwable e)
                    {
                        l.error("Unhandled fetch error: " + feed.getId() + " ("
                            + e.toString() + ")", e);
                        feedQueue.updateFeedStatus(feed, FeedStatus.UNRECOVERABLE_ERROR,
                            e.toString());
                    }
                    finally
                    {
                        countDownLatch.countDown();
                        l.debug("Fetchers left: " + countDownLatch.getCount());
                    }
                }
            };
            executor.submit(fetcher);
        }

        // Wait until all threads finish their job.
        while (!countDownLatch.await(2 * Utils.SECOND, TimeUnit.MILLISECONDS))
        {
            // Interrupt hung connections periodically.
            synchronized (openConnections)
            {
                final long now = System.currentTimeMillis();
                for (final ConnectionEntry entry : openConnections)
                {
                    if (entry != null && entry.expiresAt < now)
                    {
                        logger.debug("Interrupting thread: " + entry.thread.getName());
                        entry.thread.interrupt();
                    }
                }
            }
        }
    }

    /**
     * @return Estimate and return the timestamp of the earliest next fetch.
     */
    private long estimateNextFetch()
    {
        final long earliestStart = System.currentTimeMillis() + MINIMUM_FETCH_INTERVAL;
        final long earliestFeed = feedQueue.getClosestFetchTimestamp();
        return Math.max(earliestStart, earliestFeed);
    }

    /*
     * 
     */
    public void setFetcherThreads(int fetcherThreads)
    {
        this.fetcherThreads = fetcherThreads;
    }

    /*
     * 
     */
    public IFeedQueue getFeedQueue()
    {
        return feedQueue;
    }
}
