package com.carrotsearch.rsstoolkit.aggregator.collectors;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A fixed-size cache.
 */
public final class FixedSizeCache implements ICache<String>
{
    private final LinkedHashMap<String, Boolean> cache;

    /*
     * 
     */
    @SuppressWarnings("serial")
    public FixedSizeCache(final int maxSize)
    {
        cache = new LinkedHashMap<String, Boolean>()
        {
            @SuppressWarnings("unused")
            protected boolean removeEldestEntry(Entry<String, Boolean> eldest)
            {
                return size() > maxSize;
            }
        };
    }

    public synchronized boolean contains(String key)
    {
        return cache.containsKey(key);
    }

    public synchronized void put(String key)
    {
        cache.put(key, Boolean.TRUE);
    }
}
