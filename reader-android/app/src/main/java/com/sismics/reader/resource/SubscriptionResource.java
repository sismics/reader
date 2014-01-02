package com.sismics.reader.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Access to /subscription API.
 * 
 * @author bgamard
 */
public class SubscriptionResource extends BaseResource {

    /**
     * PUT /subscription.
     * @param context
     * @param responseHandler
     */
    public static void add(Context context, String url, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        params.put("url", url);
        client.put(getApiUrl(context) + "/subscription", params, responseHandler);
    }

    /**
     * GET /subscription.
     * @param context
     * @param unread
     * @param responseHandler
     */
    public static void list(Context context, boolean unread, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        params.put("unread", Boolean.toString(unread));
        client.get(getApiUrl(context) + "/subscription", params, responseHandler);
    }
    
    /**
     * GET articles feed.
     * @param context
     * @param url
     * @param unread
     * @param limit
     * @param afterArticleId
     * @param responseHandler
     */
    public static void feed(Context context, String url, boolean unread, int limit, String afterArticleId, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        params.put("unread", Boolean.toString(unread));
        params.put("limit", Integer.toString(limit));
        params.put("after_article", afterArticleId);
        client.get(getApiUrl(context) + url, params, responseHandler);
    }
    
    /**
     * Mark all articles as read.
     * @param context Context
     * @param url URL
     * @param responseHandler Callback
     */
    public static void read(Context context, String url, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.post(getApiUrl(context) + url + "/read", params, responseHandler);
    }
    
    /**
     * Cancel pending requests.
     * @param context Context
     */
    public static void cancel(Context context) {
        client.cancelRequests(context, true);
    }
}
