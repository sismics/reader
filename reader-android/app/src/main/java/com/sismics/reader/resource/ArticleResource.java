package com.sismics.reader.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Set;

/**
 * Access to /article API.
 * 
 * @author bgamard
 */
public class ArticleResource extends BaseResource {
    /**
     * POST /article/read.
     *
     * @param context Context
     * @param idList IDs of the articles to mark as read
     * @param responseHandler Callback
     */
    public static void readMultiple(Context context, Set<String> idList, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        params.put("id", idList);
        client.post(getApiUrl(context) + "/article/read", params, responseHandler);
    }

    /**
     * POST /article/unread.
     *
     * @param context Context
     * @param idList IDs of the articles to mark as unread
     * @param responseHandler Callback
     */
    public static void unreadMultiple(Context context, Set<String> idList, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        params.put("id", idList);
        client.post(getApiUrl(context) + "/article/unread", params, responseHandler);
    }

    /**
     * POST /article/id/unread.
     *
     * @param context Context
     * @param id ID of the article to mark as unread
     * @param responseHandler Callback
     */
    public static void unread(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.post(getApiUrl(context) + "/article/" + id + "/unread", params, responseHandler);
    }
}
