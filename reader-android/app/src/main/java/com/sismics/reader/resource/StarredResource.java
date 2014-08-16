package com.sismics.reader.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Set;

/**
 * Access to /starred API.
 * 
 * @author bgamard
 */
public class StarredResource extends BaseResource {

    /**
     * PUT/DELETE /starred/id.
     *
     * @param context Context
     * @param id Article to star/unstar
     * @param star Star if true
     * @param responseHandler Callback
     */
    public static void star(Context context, String id, boolean star, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        if (star) {
            client.put(getApiUrl(context) + "/starred/" + id, responseHandler);
        } else {
            client.delete(getApiUrl(context) + "/starred/" + id, responseHandler);
        }
    }

    /**
     * POST /starred/star.
     *
     * @param context Context
     * @param idList IDs of the articles to mark as unread
     * @param responseHandler Callback
     */
    public static void starMultiple(Context context, Set<String> idList, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        params.put("id", idList);
        client.post(getApiUrl(context) + "/starred/star", params, responseHandler);
    }

    /**
     * POST /starred/unstar.
     *
     * @param context Context
     * @param idList IDs of the articles to mark as unread
     * @param responseHandler Callback
     */
    public static void unstarMultiple(Context context, Set<String> idList, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        params.put("id", idList);
        client.post(getApiUrl(context) + "/starred/unstar", params, responseHandler);
    }
}
