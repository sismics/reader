package com.sismics.reader.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Stream utilities.
 *
 * @author jtremeaux 
 */
public class StreamUtil {
    /**
     * User-Agent to use.
     * Note: some servers refuse to talk to the default user-agent and issue a 403 Bad Behavior.
     */
    private static final String USER_AGENT = "Mozilla/4.0 (compatible; SismicsReaderBot/1.0;+http://www.sismics.com/reader/)";

    /**
     * Open a stream from a URL.
     * 
     * @param url URL
     * @return Stream
     */
    public static InputStream openStream(URL url) {
        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            return connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
