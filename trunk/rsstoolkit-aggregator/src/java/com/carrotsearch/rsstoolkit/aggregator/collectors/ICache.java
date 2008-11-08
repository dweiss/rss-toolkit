package com.carrotsearch.rsstoolkit.aggregator.collectors;

/**
 * 
 */
interface ICache<T>
{
    public void put(T key);

    public boolean contains(T key);
}
