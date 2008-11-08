package com.carrotsearch.rsstoolkit.aggregator.feeds;

import junit.framework.TestCase;

/**
 * Tests {@link NewsPostImpl}.
 */
public class NewsPostImplTest extends TestCase
{
    public NewsPostImplTest(String t)
    {
        super(t);
    }

    /**
     *
     */
    public void testNull()
    {
        assertEquals(null, NewsPostImpl.toPlainText(null));
    }

    /**
     *
     */
    public void testEmpty()
    {
        assertEquals("", NewsPostImpl.toPlainText(""));
    }

    /**
     *
     */
    public void testPlainText()
    {
        final String text = " \tDawid Weiss\n19037\r\n";
        assertEquals(text, NewsPostImpl.toPlainText(text));
    }

    /**
     *
     */
    public void testSimpleTags()
    {
        final String html = "This is <a href=\"buhu\">link </a>.";
        final String expected = "This is link .";
        final String result = NewsPostImpl.toPlainText(html);
        assertEquals(expected, normalizeWhitespace(result));
    }

    /**
     *
     */
    public void testEntity()
    {
        final String html = "An entity &lt; bad entity &gt .";
        final String expected = "An entity < bad entity &gt .";
        final String result = NewsPostImpl.toPlainText(html);
        assertEquals(expected, normalizeWhitespace(result));
    }

    private String normalizeWhitespace(String result)
    {
        return result.replaceAll("[\\n\\t\\ \\r]+", " ");
    }
}
