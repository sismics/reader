package com.sismics.reader.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
    private static final String USER_AGENT = "Mozilla/4.0 (compatible; Like Firefox; SismicsReaderBot/1.0;+http://www.sismics.com/reader/)";

    /**
     * Open and process a stream from a URL.
     * 
     * @param url URL
     * @throws Exception
     */
    public void open(URL url) throws Exception {
        Closer closer = Closer.create();
        try {
            HttpURLConnection connection = buildHttpConnection(url);
            
            // Handle 3xx redirections
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                String newUrl = connection.getHeaderField("Location");
                if (newUrl != null) {
                    connection = buildHttpConnection(new URL(newUrl));
                }
            }
            
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
    
    /**
     * Build a connection to an URL.
     * 
     * @param url URL
     * @return Connection
     * @throws IOException
     */
    private HttpURLConnection buildHttpConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);
        return connection;
    }
    
    public abstract void process(InputStream is) throws Exception;
}
