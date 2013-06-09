package com.sismics.reader.model.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.widget.BaseAdapter;

import com.sismics.android.SismicsHttpResponseHandler;
import com.sismics.reader.listener.CallbackListener;
import com.sismics.reader.resource.UserResource;
import com.sismics.reader.util.PreferenceUtil;

/**
 * Global context of the application.
 * 
 * @author bgamard
 */
public class ApplicationContext {
    /**
     * Singleton's instance.
     */
    private static ApplicationContext applicationContext;
    
    /**
     * Response of /user/info
     */
    private JSONObject userInfo;
    
    /**
     * Articles list of current feed context.
     */
    private List<JSONObject> articleItems;
    
    /**
     * Private constructor.
     */
    private ApplicationContext() {
    }
    
    /**
     * Returns a singleton of ApplicationContext.
     * 
     * @return Singleton of ApplicationContext
     */
    public static ApplicationContext getInstance() {
        if (applicationContext == null) {
            applicationContext = new ApplicationContext();
        }
        return applicationContext;
    }
    
    /**
     * Returns true if current user is logged in.
     * @return
     */
    public boolean isLoggedIn() {
        return userInfo != null && !userInfo.optBoolean("anonymous");
    }

    /**
     * Getter of userInfo
     * @return
     */
    public JSONObject getUserInfo() {
        return userInfo;
    }

    /**
     * Setter of userInfo
     * @param userInfo
     */
    public void setUserInfo(Context context, JSONObject json) {
        this.userInfo = json;
        PreferenceUtil.setCachedUserInfoJson(context, json);
    }
    
    /**
     * Asynchronously get user info.
     * @param activity
     * @param callbackListener
     */
    public void fetchUserInfo(final Activity activity, final CallbackListener callbackListener) {
        UserResource.info(activity.getApplicationContext(), new SismicsHttpResponseHandler() {
            @Override
            public void onSuccess(final JSONObject json) {
                // Save data in application context
                if (!json.optBoolean("anonymous", true)) {
                    setUserInfo(activity.getApplicationContext(), json);
                }
            }
            
            @Override
            public void onFinish() {
                if (callbackListener != null) {
                    callbackListener.onComplete();
                }
            }
        });
    }
    
    // TODO Move next to a shared adapter list pattern class
    
    /**
     * Getter of articleItems.
     * @return articleItems
     */
    public List<JSONObject> getArticleItems() {
        if (articleItems == null) {
            articleItems = new ArrayList<JSONObject>();
        }
        return articleItems;
    }
    
    Set<Object> adapters = new HashSet<Object>();
    
    public void addOnArticleItemsChanged(Object adapter) {
        adapters.add(adapter);
    }
    
    public void removeOnArticleItemsChanged(Object adapter) {
        adapters.remove(adapter);
    }
    
    public void onArticleItemsChanged() {
        for (Object adapter : adapters) {
            if (adapter instanceof BaseAdapter) {
                ((BaseAdapter) adapter).notifyDataSetChanged();
            }
            if (adapter instanceof PagerAdapter) {
                ((PagerAdapter) adapter).notifyDataSetChanged();
            }
        }
    }
}
