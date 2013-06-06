package com.sismics.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Classe utilitaire pour la connectivité réseau.
 * 
 * @author bgamard
 */
public class ConnectivityUtil {
    
    /**
     * Vérifie la connectivité avant d'effectuer une opération nécessitant le réseau.
     * 
     * @param context Contexte
     * @return Connectivité présente
     */
    public static boolean checkConnectivity(Context context) {
        // Vérifie la connectivité
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }
        
        return false;
    }
}
