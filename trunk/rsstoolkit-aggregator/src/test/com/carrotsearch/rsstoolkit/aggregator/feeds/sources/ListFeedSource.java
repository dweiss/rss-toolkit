package com.carrotsearch.rsstoolkit.aggregator.feeds.sources;

import java.util.Collection;
import java.util.List;

import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeed;
import com.carrotsearch.rsstoolkit.aggregator.feeds.IFeedSource;

/**
 * 
 * 
 */
public class ListFeedSource implements IFeedSource
{

    private List<IFeed> list;

    public ListFeedSource(List<IFeed> list)
    {
        this.list = list;
    }

    public Collection<IFeed> getFeeds()
    {
        return list;
    }
}
