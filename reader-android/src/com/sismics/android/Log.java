package com.sismics.android;

/**
 * Logger qui encapsule l'activation / désactivation des debug.
 * Les logs sont au niveau WARN par défaut.
 * Pour les activer : adb shell setprop log.tag.TAG_NAME DEBUG
 * 
 * @author naku
 */
public class Log {
    /**
     * Affiche un message de débug.
     * 
     * @param tag Balise
     * @param msg Message
     */
    public static void d(final String tag, final String msg) {
        if (android.util.Log.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(tag, msg);
        }
    }
    
    /**
     * Affiche un message d'information.
     * 
     * @param tag Balise
     * @param msg Message
     */
    public static void w(final String tag, final String msg) {
        if (android.util.Log.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(tag, msg);
        }
    }
    
    /**
     * Affiche un message d'erreur.
     * 
     * @param tag Balise
     * @param msg Message
     * @param t Exception
     */
    public static void e(final String tag, final String msg, Throwable t) {
        if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag, msg, t);
        }
    }
    
    /**
     * Affiche un message d'erreur.
     * 
     * @param tag Balise
     * @param msg Message
     */
    public static void e(final String tag, final String msg) {
        if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag, msg);
        }
    }
}
