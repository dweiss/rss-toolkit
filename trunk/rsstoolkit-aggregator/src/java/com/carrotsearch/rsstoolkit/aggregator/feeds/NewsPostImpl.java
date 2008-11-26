package com.carrotsearch.rsstoolkit.aggregator.feeds;

import java.util.Arrays;
import java.util.Date;

import com.carrotsearch.util.MarkupUtils;

/**
 * A base implementation of {@link INewsPost} interface.
 */
public class NewsPostImpl implements INewsPost
{
    /**
     * An identifier of this post. Should be something unique for any post ever published.
     * Obligatory.
     */
    private final String id;

    /**
     * Date of publication of this post. Obligatory.
     */
    private final Date publicationDate;

    /**
     * The identifier of a feed this news originated from.
     */
    private final Object feedId;

    private String title;
    private String description;

    private String [] authors;
    private String [] links;

    // Getters/ setters below.

    public NewsPostImpl(String id, Object feedId, Date publicationDate)
    {
        if (id == null) throw new IllegalArgumentException(
            "News identifier must not be null.");
        if (!id.matches("[a-zA-Z0-9]+")) throw new IllegalArgumentException(
            "News identifier must match [a-zA-Z0-9]+: " + id);
        if (feedId == null) throw new IllegalArgumentException(
            "Feed ID must not be null.");
        if (publicationDate == null) throw new IllegalArgumentException(
            "Publication date must not be null.");

        this.id = id;
        this.feedId = feedId;
        this.publicationDate = publicationDate;
    }

    /**
     * 
     */
    public String [] getAuthors()
    {
        return authors;
    }

    /**
     * 
     */
    public void setAuthors(String [] authors)
    {
        if (authors == null || Arrays.asList(authors).contains(null))
        {
            throw new IllegalArgumentException();
        }
        this.authors = authors;
    }

    /**
     * 
     */
    public String getDescription()
    {
        return description == null ? "" : description;
    }

    /**
     * 
     */
    public void setDescription(String description)
    {
        this.description = MarkupUtils.toPlainText(description);
    }

    /**
     * 
     */
    public Date getPublicationDate()
    {
        return publicationDate;
    }

    /**
     * 
     */
    public String getTitle()
    {
        return title == null ? "" : title;
    }

    /**
     * 
     */
    public void setTitle(String title)
    {
        this.title = MarkupUtils.toPlainText(title);
    }

    /**
     * 
     */
    public void setLinks(String [] links)
    {
        if (links == null || Arrays.asList(links).contains(null))
        {
            throw new IllegalArgumentException();
        }
        this.links = links;
    }

    /**
     * 
     */
    public String [] getLinks()
    {
        return this.links;
    }

    /**
     * 
     */
    public String getId()
    {
        return id;
    }

    /**
     * 
     */
    public Object getFeedId()
    {
        return feedId;
    }

    @Override
    public String toString()
    {
        return "[post url="
            + (getLinks() != null && getLinks().length > 0 ? getLinks()[0] : "")
            + ", md5=" + getId() + "]";
    }
}
