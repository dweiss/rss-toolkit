package com.carrotsearch.rsstoolkit.aggregator.queue;

/**
 * Feed status information.
 */
public enum FeedStatus
{
    // New feed (not yet crawled).
    NEW
    {
        public int id()
        {
            return 0;
        }
    },

    // Normal ok status.
    CRAWLED_OK
    {
        public int id()
        {
            return 1;
        }
    },

    // The feed should not be recrawled at all (404).
    HTTP_NOT_FOUND
    {
        public int id()
        {
            return 2;
        }
    },

    // The feed should not be recrawled at all.
    UNRECOVERABLE_ERROR
    {
        public int id()
        {
            return 3;
        }
    },

    // The feed may be recrawled some time in near future.
    CONNECTION_TIMEOUT
    {
        public int id()
        {
            return 4;
        }
    },

    // Some other recoverable error (retry after a while).
    RECOVERABLE_ERROR
    {
        public int id()
        {
            return 5;
        }
    };

    public abstract int id();
}
