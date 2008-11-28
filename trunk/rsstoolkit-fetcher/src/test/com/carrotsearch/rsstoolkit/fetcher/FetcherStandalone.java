package com.carrotsearch.rsstoolkit.fetcher;

import java.io.File;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.BlockingChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Launcher for built-in Jetty.
 */
public final class FetcherStandalone {
    /**
     * Command line entry point.
     */
    public static void main(String [] args)
        throws Exception
    {
        final Server server = new Server();
        server.setStopAtShutdown(true);
        
        final WebAppContext context = new WebAppContext(new File("web").getAbsolutePath(), "/");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        server.addHandler(context);
        
        final Connector c = new BlockingChannelConnector();
        c.setPort(8080);
        server.addConnector(c);

        // Start the HTTP server
        server.start();
    }
}
