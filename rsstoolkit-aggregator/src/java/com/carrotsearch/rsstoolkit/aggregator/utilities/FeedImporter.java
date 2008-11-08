package com.carrotsearch.rsstoolkit.aggregator.utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * Read a list of URLs to RSS/Atom feeds, fetch their content and list the URL and title.
 */
public final class FeedImporter
{
    /*
     * 
     */
    public static void main(String [] args) throws Exception
    {
        final BufferedReader br = new BufferedReader(new InputStreamReader(
            new FileInputStream(args[0]), "UTF-8"));

        String line;
        while ((line = br.readLine()) != null)
        {
            line = line.trim();

            try
            {
                final URL url = new URL(line);
                final WireFeedInput input = new WireFeedInput();
                final WireFeed feed = input.build(new XmlReader(url.openStream()));

                String title = url.toExternalForm();
                if (feed instanceof Channel)
                {
                    title = ((Channel) feed).getTitle();
                }
                else if (feed instanceof Feed)
                {
                    title = ((Feed) feed).getTitle();
                }
                System.out.println("'" + url.toExternalForm() + "', '" + title + "'");
            }
            catch (Exception e)
            {
                System.err.println("Feed failed: " + line + ", " + e.toString());
            }
        }
    }
}
