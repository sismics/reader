package com.sismics.reader.resource;

import android.content.Context;

import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.sismics.android.SismicsHttpResponseHandler;
import com.sismics.reader.constant.Constants;

/**
 * Access to /article API.
 * 
 * @author bgamard
 */
public class ArticleResource extends BaseResource {

    /**
     * Resource initialization.
     * @param context
     */
    private static void init(Context context) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        client.setCookieStore(cookieStore);
    }

    /**
     * POST /article/id/read.
     * @param context
     * @param id
     * @param responseHandler
     */
    public static void read(Context context, String id, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.get(Constants.READER_API_URL + "/article/" + id + "/read", params, responseHandler);
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
        client.get(Constants.READER_API_URL + "/article/" + id + "/unread", params, responseHandler);
    }
}
