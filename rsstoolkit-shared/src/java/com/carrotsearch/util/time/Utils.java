package com.carrotsearch.util.time;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Various utilities related to time.
 */
public final class Utils
{

    /** A second is 1000 milliseconds. */
    public final static long SECOND = 1000;

    /** A minute is 60 seconds. */
    public final static long MINUTE = 60 * SECOND;

    /** An hour is 60 minutes. */
    public final static long HOUR = 60 * MINUTE;

    /** A day is 24 hours. */
    public final static long DAY = 24 * HOUR;

    /**
     *
     */
    private Utils()
    {
        // no instances.
    }

    /**
     * Converts the given number of milliseconds to a string containing a given number of
     * days, hours and minutes.
     */
    public final static String getDurationAsString(long durationMillis)
    {
        final long days = durationMillis / (24 * Utils.HOUR);
        durationMillis -= days * (24 * Utils.HOUR);

        final long hours = durationMillis / Utils.HOUR;
        durationMillis -= hours * Utils.HOUR;

        final long minutes = durationMillis / Utils.MINUTE;
        durationMillis -= minutes * Utils.MINUTE;

        final long seconds = durationMillis / Utils.SECOND;
        durationMillis -= seconds * Utils.SECOND;

        final StringBuilder builder = new StringBuilder();

        if (days > 0) builder.append(days).append(days == 1 ? " day " : " days ");
        if (hours > 0) builder.append(hours).append(hours == 1 ? " hour " : " hours ");
        if (minutes > 0) builder.append(minutes).append(
            minutes == 1 ? " minute " : " minutes ");
        if (seconds != 0 || (days == 0 && hours == 0 && minutes == 0))
        {
            builder.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }

        return builder.toString().trim();
    }

    /**
     * Returns a new instance of a (commonly-initialized) date formatter.
     */
    public static SimpleDateFormat createCommonDateFormat()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf;
    }

    /**
     * Returns ISO 8601 date format (compact).
     */
    public static SimpleDateFormat createISODateFormat()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf;
    }
}
