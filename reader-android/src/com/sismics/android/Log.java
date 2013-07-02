package com.sismics.android;

/**
 * Wrapper around Android logger.
 * Logs are WARN level by default.
 * To activate them : adb shell setprop log.tag.TAG_NAME DEBUG
 * 
 * @author jtremeaux
 */
public class Log {
    /**
     * Display a debug message.
     * 
     * @param tag Tag
     * @param msg Message
     */
    public static void d(final String tag, final String msg) {
        if (android.util.Log.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(tag, msg);
        }
    }
    
    /**
     * Display an info message.
     * 
     * @param tag Tag
     * @param msg Message
     */
    public static void w(final String tag, final String msg) {
        if (android.util.Log.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(tag, msg);
        }
    }
    
    /**
     * Display an error message.
     * 
     * @param tag Tag
     * @param msg Message
     * @param t Exception
     */
    public static void e(final String tag, final String msg, Throwable t) {
        if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag, msg, t);
        }
    }
    
    /**
     * Display an error message.
     * 
     * @param tag Tag
     * @param msg Message
     */
    public static void e(final String tag, final String msg) {
        if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag, msg);
        }
    }
}
