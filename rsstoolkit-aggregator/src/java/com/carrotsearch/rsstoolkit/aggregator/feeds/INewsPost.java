package com.carrotsearch.rsstoolkit.aggregator.feeds;

import java.util.Date;

/**
 * <p>
 * Aggregator's central object: a news post. This interface defines the properties of a
 * news post as a POJO interface.
 */
public interface INewsPost
{
    /**
     * @return Returns a unique identifier of this news item in the entire collection.
     *         Never <code>null</code>.
     */
    public String getId();

    /**
     * @return Return the feed identifier this post originated from.
     */
    public Object getFeedId();

    /**
     * @return Returns the publication date of this post. Never <code>null</code>.
     */
    public Date getPublicationDate();

    /**
     * @return Returns the authors of this post (possibly empty).
     */
    public String [] getAuthors();

    /**
     * @return Returns a string with a plain text summary attached to this post.
     */
    public String getDescription();

    /**
     * @return Returns a string with a plain text title of this post.
     */
    public String getTitle();

    /**
     * @return Returns the URLs associated with this post (possibly empty).
     */
    public String [] getLinks();
}
