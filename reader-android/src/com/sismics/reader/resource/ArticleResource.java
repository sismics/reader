package com.sismics.reader.resource;

import java.util.ArrayList;

import android.content.Context;

import com.loopj.android.http.RequestParams;
import com.sismics.android.SismicsHttpResponseHandler;

/**
 * Access to /article API.
 * 
 * @author bgamard
 */
public class ArticleResource extends BaseResource {

    /**
     * POST /article/id/read.
     * @param context
     * @param id
     * @param responseHandler
     */
    public static void read(Context context, String id, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.post(getApiUrl(context) + "/article/" + id + "/read", params, responseHandler);
    }
    
    /**
     * POST /article/read.
     * @param context
     * @param id
     * @param responseHandler
     */
    public static void readMultiple(Context context, ArrayList<String> idList, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        params.put("id", idList);
        client.post(getApiUrl(context) + "/article/read", params, responseHandler);
    }
    
    /**
     * POST /article/id/unread.
     * @param context
     * @param id
     * @param responseHandler
     */
    public static void unread(Context context, String id, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.post(getApiUrl(context) + "/article/" + id + "/unread", params, responseHandler);
    }
}
