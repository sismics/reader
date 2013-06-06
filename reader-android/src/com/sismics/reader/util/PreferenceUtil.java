package com.sismics.reader.util;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Utility class on preferences.
 * 
 * @author bgamard
 */
public class PreferenceUtil {
    /**
     * Cache of /user/info.
     */
    public static final String PREF_CACHED_USER_INFO_JSON = "pref_cachedUserInfoJson";
    
    /**
     * Returns a preference of boolean type.
     * @param context
     * @param key
     * @return
     */
    public static boolean getBooleanPreference(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, true);
    }
    
    /**
     * Returns a preference of string type.
     * @param context
     * @param key
     * @return
     */
    public static String getStringPreference(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, null);
    }
    
    /**
     * Returns a preference of integer type.
     * @param context
     * @param key
     * @return
     */
    public static int getIntegerPreference(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String pref = sharedPreferences.getString(key, "");
            try {
                return Integer.parseInt(pref);
            } catch (NumberFormatException e) {
                return 0;
            }
        } catch (ClassCastException e) {
            return sharedPreferences.getInt(key, 0);
        }
        
    }
    
    /**
     * Update cache of /user/info.
     * @param context
     */
    public static void setCachedUserInfoJson(Context context, JSONObject json) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(PREF_CACHED_USER_INFO_JSON, json != null ? json.toString() : null).commit();
    }
    
    /**
     * Empty user caches.
     * @param context
     */
    public static void resetUserCache(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putString(PREF_CACHED_USER_INFO_JSON, null);
        editor.commit();
    }
}
