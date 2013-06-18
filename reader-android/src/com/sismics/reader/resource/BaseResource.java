package com.sismics.reader.resource;

import com.loopj.android.http.AsyncHttpClient;

/**
 * Base class for API access.
 * 
 * @author bgamard
 */
public class BaseResource {
    
    /**
     * User-Agent to use.
     */
    protected static String USER_AGENT = null;
    
    /**
     * HTTP client.
     */
    protected static AsyncHttpClient client = new AsyncHttpClient();
    
    static {
        // 20sec default timeout
        client.setTimeout(20000);
    }
}
