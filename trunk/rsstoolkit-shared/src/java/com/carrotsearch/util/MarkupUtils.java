package com.carrotsearch.util;

import java.io.*;

import org.apache.solr.analysis.HTMLStripReader;

/**
 * Utilitities for stripping HTML markup.
 */
public class MarkupUtils
{

    /**
     * Converts a string with possible HTML codes to plain text.
     */
    public final static String toPlainText(String html)
    {
        if (html == null)
        {
            return null;
        }

        try
        {
            final HTMLStripReader reader = new HTMLStripReader(new StringReader(html));
            reader.setKeepAlignedPositions(false);
            final StringWriter writer = new StringWriter();
            final char [] buffer = new char [4096];
            int len;
            while ((len = reader.read(buffer)) > 0)
            {
                writer.write(buffer, 0, len);
            }
    
            return writer.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unreachable code.");
        }
    }
}
