package com.carrotsearch.rsstoolkit.aggregator;

/**
 * <p>
 * Aggregator listeners receive events associated with the {@link Aggregator}'s
 * lifecycle:
 * <ul>
 * <li>starting feed aggregation,</li>
 * <li>finish of a single round of fetching,</li>
 * <li>service shutdown event.</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class AggregatorListener
{
    /**
     * Aggregator has been started.
     */
    public void started(Aggregator source)
    {
        // ignore by default
    }

    /**
     * Aggregator stopped.
     */
    public void stopped(Aggregator source)
    {
        // ignore by default
    }

    /**
     * A round of fetching finished.
     */
    public void roundFinished(Aggregator source)
    {
        // ignore by default
    }
}