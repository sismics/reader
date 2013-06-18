package com.sismics.reader.resource;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.sismics.reader.util.PreferenceUtil;

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
    
    /**
     * Returns cleaned API URL.
     * @param context
     * @return
     */
    protected static String getApiUrl(Context context) {
        String serverUrl = PreferenceUtil.getServerUrl(context);
        
        if (serverUrl == null) {
            return null;
        }
        
        return serverUrl + "/api";
    }
}
