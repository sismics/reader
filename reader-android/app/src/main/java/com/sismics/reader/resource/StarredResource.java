package com.sismics.reader.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Access to /starred API.
 * 
 * @author bgamard
 */
public class StarredResource extends BaseResource {

    /**
     * PUT/DELETE /starred/id.
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
}
