package com.sismics.reader.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.google.common.io.Closer;

/**
 * HTTP client.
 *
 * @author jtremeaux 
 */
public abstract class ReaderHttpClient {
    /**
     * User-Agent to use.
     * Note: some servers refuse to talk to the default user-agent and issue a 403 Bad Behavior.
     */
    private static final String USER_AGENT = "Mozilla/4.0 (compatible; SismicsReaderBot/1.0;+http://www.sismics.com/reader/)";

    /**
     * Open and process a stream from a URL.
     * 
     * @param url URL
     * @throws Exception
     */
    public void open(URL url) throws Exception {
        Closer closer = Closer.create();
        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            InputStream is = closer.register(connection.getInputStream());
            
            process(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                // NOP
            }
        }
    }
    
    public abstract void process(InputStream is) throws Exception;
}
