package com.carrotsearch.rsstoolkit.aggregator.collectors;

/**
 * Information concerning an update of documents in
 * {@link INewsPostsCollector#add(java.util.Collection)}.
 */
public final class UpdateInfo
{
    /** Number of items passed to {@link INewsPostsCollector} in this round. */
    public final int fetched;

    /** Number of new items. */
    public final int added;

    /** Number of ignored items. */
    public final int ignored;

    UpdateInfo(int fetched, int added, int ignored)
    {
        this.fetched = fetched;
        this.added = added;
        this.ignored = ignored;
    }

    @Override
    public String toString()
    {
        return "fetched: " + fetched + ", added: " + added + ", ignored: " + ignored;
    }
}
