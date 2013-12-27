package com.sismics.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utility class for network connectivity.
 * 
 * @author bgamard
 */
public class ConnectivityUtil {
    
    /**
     * Check if a network connection is available.
     * 
     * @param context Context
     * @return Network connection available
     */
    public static boolean checkConnectivity(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }
        
        return false;
    }
}
