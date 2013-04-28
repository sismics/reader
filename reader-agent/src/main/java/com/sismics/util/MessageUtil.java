package com.sismics.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message utilities.
 *
 * @author jtremeaux
 */
public class MessageUtil {
    private static final Locale locale = Locale.getDefault();
    
    /**
     * Returns a message formated in the specified locale.
     * Returns **key** if no message is set for this key.
     * 
     * @param key Message key
     * @param args Arguments of the message
     * @return Formated message
     */
    public static String getMessage(String key, Object... args) {
        ResourceBundle resources = ResourceBundle.getBundle("messages", locale);
        String message = null;
        try {
            message = resources.getString(key);
        } catch (MissingResourceException e) {
            message = "**" + key + "**";
        }
        return MessageFormat.format(message, args);
    }
}
