package com.carrotsearch.rsstoolkit.dao;

/**
 * A category of feeds.
 */
public final class Category
{
    public final Category parent;
    public final int id;
    public final String name;

    public Category(int id, String name, Category parent)
    {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }
}