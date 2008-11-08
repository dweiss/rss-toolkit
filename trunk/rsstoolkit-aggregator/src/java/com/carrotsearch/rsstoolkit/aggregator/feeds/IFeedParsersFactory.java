package com.carrotsearch.rsstoolkit.aggregator.feeds;

import java.net.URI;

/**
 * A factory of parsers for {@link URI}s pointing to sources of {@link IFeed}s.
 */
public interface IFeedParsersFactory
{
    /**
     * @return Returns a parser for a given <code>URI</code> and feed identifier.
     */
    public IFeed getFeedParser(URI url, Object id);
}
