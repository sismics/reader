package com.sismics.reader.resource;

import java.util.Locale;

import android.content.Context;

import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.sismics.android.SismicsHttpResponseHandler;
import com.sismics.reader.constant.Constants;

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
     * /user/login
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
        client.post(Constants.READER_API_URL + "/user/login", params, responseHandler);
    }

    /**
     * /user/info
     * @param context
     * @param responseHandler
     */
    public static void info(Context context, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.get(Constants.READER_API_URL + "/user", params, responseHandler);
    }
    
    /**
     * /user/logout
     * @param context
     * @param responseHandler
     */
    public static void logout(Context context, SismicsHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.post(Constants.READER_API_URL + "/user/logout", params, responseHandler);
    }
}
