package com.carrotsearch.rsstoolkit.aggregator.collectors;

import java.io.Serializable;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * EHCache based cache for storing recent posts (their MD5's).
 */
final class EhCache<T extends Serializable> implements ICache<T>
{
    final Cache cache;

    /*
     * 
     */
    public EhCache(Cache cache)
    {
        this.cache = cache;
        this.cache.removeAll();
    }

    /*
     * 
     */
    public void put(T key)
    {
        cache.put(new Element(key, Boolean.TRUE));
    }

    /*
     * 
     */
    public boolean contains(T key)
    {
        return cache.isKeyInCache(key);
    }
}
