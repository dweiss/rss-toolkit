package com.carrotsearch.util.httpclient;

import org.apache.commons.httpclient.HttpClient;

import com.carrotsearch.util.time.Utils;

/**
 * Prepare instances of {@link HttpClient} with desired socket configuration settings.
 */
public final class HttpClientFactory
{
    private HttpClientFactory()
    {
        // no instances
    }

    /**
     * @return
     */
    public static HttpClient getTimeoutingClient()
    {
        final HttpClient httpClient = new HttpClient(new SingleHttpConnectionManager());

        // Setup default timeouts.
        httpClient.getParams().setSoTimeout((int) (20 * Utils.SECOND));
        httpClient.getParams().setIntParameter("http.connection.timeout",
            (int) (20 * Utils.SECOND));

        // Not important (single http connection manager), but anyway.
        httpClient.getParams().setConnectionManagerTimeout(30 * Utils.SECOND);

        return httpClient;
    }
}
