package com.sismics.reader.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Access to /category API.
 * 
 * @author bgamard
 */
public class CategoryResource extends BaseResource {

    /**
     * PUT /category.
     * @param context Context
     * @param name Category name
     * @param responseHandler Callback
     */
    public static void add(Context context, String name, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        params.put("name", name);
        client.put(getApiUrl(context) + "/category", params, responseHandler);
    }

    /**
     * GET /category.
     * @param context Context
     * @param responseHandler Callback
     */
    public static void list(Context context, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.get(getApiUrl(context) + "/category", params, responseHandler);
    }

    /**
     * DELETE /category.
     * @param context Context
     * @param id Category ID
     * @param responseHandler Callback
     */
    public static void delete(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.delete(getApiUrl(context) + "/category/" + id, responseHandler);
    }

    /**
     * POST /category.
     * @param context Context
     * @param id Category ID
     * @param name Category name
     * @param order Category order
     * @param responseHandler Callback
     */
    public static void update(Context context, String id, String name, Integer order, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        if (name != null) {
            params.put("name", name);
        }
        if (order != null) {
            params.put("order", Integer.toString(order));
        }
        client.post(getApiUrl(context) + "/category/" + id, params, responseHandler);
    }

    /**
     * Cancel pending requests.
     * @param context Context
     */
    public static void cancel(Context context) {
        client.cancelRequests(context, true);
    }
}
