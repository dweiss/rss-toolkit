package com.carrotsearch.rsstoolkit.aggregator.queue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Feed metadata.
 */
public final class FeedMetadata implements Serializable
{
    private final static long serialVersionUID = 0x200712131813l;

    final ArrayList<Long> fetch_times = new ArrayList<Long>();

    /*
     * 
     */
    public void addFetchTime(long fetch_time)
    {
        fetch_times.add(fetch_time);
        if (fetch_times.size() > 10)
        {
            fetch_times.remove(0);
        }
    }

    /*
     * 
     */
    public long getAverageUpdateInterval()
    {
        if (fetch_times.size() < 2)
        {
            return 0;
        }

        // Calculate differences between updates.
        final ArrayList<Long> updates = new ArrayList<Long>(fetch_times.size() - 1);
        for (int i = 0; i < fetch_times.size() - 1; i++)
        {
            updates.add(fetch_times.get(i + 1) - fetch_times.get(i));
        }

        // Sort and calculate average. If possible, avoid using min/max cases.
        Collections.sort(updates);
        if (updates.size() > 4)
        {
            updates.remove(0);
            updates.remove(updates.size() - 1);
        }

        long avg = 0;
        for (long v : updates)
            avg += v;

        return avg / updates.size();
    }
}
