package com.sismics.reader;

import android.app.Application;

import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.reader.model.application.ApplicationContext;
import com.sismics.reader.util.PreferenceUtil;

import org.json.JSONObject;

/**
 * Main application.
 * 
 * @author bgamard
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        // Fetching /user/info from cache
        JSONObject json = PreferenceUtil.getCachedJson(getApplicationContext(), PreferenceUtil.PREF_CACHED_USER_INFO_JSON);
        ApplicationContext.getInstance().setUserInfo(getApplicationContext(), json);

        // TODO Changing the category of a subscription
        // TODO Renaming subscriptions
        // TODO Deleting subscriptions
        // TODO Admin interface if the user is admin

        // TODO Articles drawer: select article on opening

        super.onCreate();
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        BitmapAjaxCallback.clearCache();
    }
}
