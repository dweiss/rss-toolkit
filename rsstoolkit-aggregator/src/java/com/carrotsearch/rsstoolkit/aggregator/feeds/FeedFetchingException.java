package com.carrotsearch.rsstoolkit.aggregator.feeds;

/**
 * An exception thrown when fetching a feed's posts failed.
 */
@SuppressWarnings("serial")
public class FeedFetchingException extends Exception
{
    public static enum REASON
    {
        HTTP_403_FORBIDDEN, HTTP_404_NOT_FOUND, HTTP_OTHER, INVALID_XML, IO, UNSPECIFIED,
    }

    public final REASON reason;

    public FeedFetchingException(REASON reason, String message, Throwable cause)
    {
        super(message, cause);
        this.reason = reason;
    }

    public FeedFetchingException(REASON reason, Throwable cause)
    {
        super(cause);
        this.reason = reason;
    }

    public FeedFetchingException(REASON reason, String message)
    {
        super(message);
        this.reason = reason;
    }

    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        b.append(reason.toString());
        if (getMessage() != null)
        {
            b.append(", ");
            b.append(getMessage());
        }
        if (getCause() != null)
        {
            b.append(" [cause: " + getCause() + "]");
        }
        return b.toString();
    }
}
