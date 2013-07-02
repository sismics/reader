package com.sismics.reader.resource;

import java.util.Locale;

import android.content.Context;

import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.sismics.android.SismicsHttpResponseHandler;

/**
 * Access to /user API.
 * 
 * @author bgamard
 */
public class UserResource extends BaseResource {

    /**
     * Resource initialization.
     * @param context
     */
    private static void init(Context context) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        client.setCookieStore(cookieStore);
        
        Locale locale = Locale.getDefault();
        client.addHeader("Accept-Language", locale.getLanguage() + "_" + locale.getCountry());
    }

    /**
     * POST /user/login.
     * @param context
     * @param username
     * @param password
     * @param responseHandler
     */
    public static void login(Context context, String username, String password, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);
        params.put("remember", "true");
        client.post(getApiUrl(context) + "/user/login", params, responseHandler);
    }

    /**
     * GET /user.
     * @param context
     * @param responseHandler
     */
    public static void info(Context context, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.get(getApiUrl(context) + "/user", params, responseHandler);
    }
    
    /**
     * POST /user/logout.
     * @param context
     * @param responseHandler
     */
    public static void logout(Context context, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.post(getApiUrl(context) + "/user/logout", params, responseHandler);
    }
}
