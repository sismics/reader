package com.sismics.reader.resource;

import android.content.Context;

import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.sismics.android.SismicsHttpResponseHandler;

/**
 * Access to /subscription API.
 * 
 * @author bgamard
 */
public class SubscriptionResource extends BaseResource {

    /**
     * Resource initialization.
     * @param context
     */
    private static void init(Context context) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        client.setCookieStore(cookieStore);
    }

    /**
     * GET /subscription.
     * @param context
     * @param responseHandler
     */
    public static void list(Context context, boolean unread, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        params.put("unread", Boolean.toString(unread));
        client.get(getApiUrl(context) + "/subscription", params, responseHandler);
    }
    
    /**
     * GET articles feed.
     * @param context
     * @param responseHandler
     */
    public static void feed(Context context, String url, boolean unread, int limit, String afterArticleId, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        params.put("unread", Boolean.toString(unread));
        params.put("limit", Integer.toString(limit));
        params.put("after_article", afterArticleId);
        client.get(getApiUrl(context) + url, params, responseHandler);
    }
    
    /**
     * Mark all articles as read.
     * @param context
     * @param responseHandler
     */
    public static void read(Context context, String url, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.post(getApiUrl(context) + url + "/read", params, responseHandler);
    }
    
    /**
     * Cancel pending requests.
     * @param context
     */
    public static void cancel(Context context) {
        client.cancelRequests(context, true);
    }
}
