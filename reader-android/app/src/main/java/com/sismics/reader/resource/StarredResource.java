package com.sismics.reader.resource;

import android.content.Context;

import com.sismics.android.SismicsHttpResponseHandler;

/**
 * Access to /starred API.
 * 
 * @author bgamard
 */
public class StarredResource extends BaseResource {

    /**
     * PUT/DELETE /starred/id.
     * @param context
     * @param id
     * @param star
     * @param responseHandler
     */
    public static void star(Context context, String id, boolean star, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        if (star) {
            client.put(getApiUrl(context) + "/starred/" + id, responseHandler);
        } else {
            client.delete(getApiUrl(context) + "/starred/" + id, responseHandler);
        }
    }
}
