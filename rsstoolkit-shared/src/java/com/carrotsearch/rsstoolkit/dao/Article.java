package com.carrotsearch.rsstoolkit.dao;

/**
 * A model for an article in the database.
 */
public final class Article
{
    public final Integer id;
    public final String url, title, content;
    public final long posted_at, added_at;

    public final Integer fk_feed_id;

    public Article(Integer id, String url, String title, String content, long posted_at,
        long added_at, Integer fk_feed_id)
    {
        this.id = id;
        this.url = url;
        this.title = title;
        this.content = content;
        this.posted_at = posted_at;
        this.added_at = added_at;
        this.fk_feed_id = fk_feed_id;
    }
}
