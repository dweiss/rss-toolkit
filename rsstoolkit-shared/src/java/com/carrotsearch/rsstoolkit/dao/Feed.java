package com.carrotsearch.rsstoolkit.dao;

import java.util.List;

/**
 * A category of feeds.
 */
public final class Feed
{
    public final List<String> categories;
    public final int id;
    public final String url;

    public Feed(int id, String url, List<String> categories)
    {
        this.categories = categories;
        this.url = url;
        this.id = id;
    }
}