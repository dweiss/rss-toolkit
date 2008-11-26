package com.carrotsearch.util;

import junit.framework.TestCase;

public class MarkupUtilsTest extends TestCase
{
    public void testEntityOAcute()
    {
        final String input = "por&oacute;wnanie";        
        assertEquals("porównanie", MarkupUtils.toPlainText(input));
    }

    public void testEntityAmp()
    {
        String input = "left&amp;right";        
        assertEquals("left&right", MarkupUtils.toPlainText(input));
    }

    public void testUpperCaseEntities()
    {
        String input = "left&AMP;&LT;right";        
        assertEquals("left&<right", MarkupUtils.toPlainText(input));        
    }

    public void testNumericEntity()
    {
        final String input = "&#xA0;&#160;";        
        assertEquals(2, MarkupUtils.toPlainText(input).length());
    }        
}
