package com.carrotsearch.rsstoolkit.dao;

/**
 * A group of documents in the database.
 */
public final class Group
{
    public final Integer id;

    public final String title;

    public final boolean expired;

    public final String article_ids;
    public final int article_count;

    public final String article_ids_full;

    public final float intra_similarity;

    public Group(Integer id, String title, boolean expired, String article_ids,
        String article_ids_full, int article_count, float intra_similarity)
    {
        this.id = id;
        this.title = title;
        this.expired = expired;
        this.article_ids = article_ids;
        this.article_ids_full = article_ids_full;
        this.article_count = article_count;
        this.intra_similarity = intra_similarity;
    }
}
