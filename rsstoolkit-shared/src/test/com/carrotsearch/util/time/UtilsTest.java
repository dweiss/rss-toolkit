package com.carrotsearch.util.time;

import junit.framework.TestCase;

import com.carrotsearch.util.time.Utils;

/**
 * Tests {@link Utils} class.
 */
public final class UtilsTest extends TestCase
{
    /**
     * 
     */
    public UtilsTest(String t)
    {
        super(t);
    }

    /**
     * 
     */
    public void testDurationToString() throws Exception
    {
        assertEquals("1 second", Utils.getDurationAsString(1 * Utils.SECOND));
        assertEquals("2 seconds", Utils.getDurationAsString(2 * Utils.SECOND));
        assertEquals("0 seconds", Utils.getDurationAsString(0));
        assertEquals("1 minute 1 second", Utils.getDurationAsString(61 * Utils.SECOND));
        assertEquals("1 hour 59 minutes", Utils.getDurationAsString(1 * Utils.HOUR + 59
            * Utils.MINUTE + 0 * Utils.SECOND + 500));
    }
}
