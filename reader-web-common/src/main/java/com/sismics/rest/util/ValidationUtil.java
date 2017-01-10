package com.sismics.rest.util;

import com.google.common.base.Strings;
import com.sismics.rest.exception.ClientException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;

import java.text.MessageFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Utility class to validate parameters.
 *
 * @author jtremeaux
 */
public class ValidationUtil {
    private static Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");
    
    private static Pattern HTTP_URL_PATTERN = Pattern.compile("https?://.+");
    
    private static Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
    
    /**
     * Checks that the argument is not null.
     * 
     * @param s Object tu validate
     * @param name Name of the parameter
     */
    public static void validateRequired(Object s, String name) throws JSONException {
        if (s == null) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
        }
    }
    
    /**
     * Validate a string length.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @param lengthMin Minimum length (or null)
     * @param lengthMax Maximum length (or null)
     * @param nullable True if the string can be empty or null
     * @return String without white spaces
     */
    public static String validateLength(String s, String name, Integer lengthMin, Integer lengthMax, boolean nullable) throws JSONException {
        s = StringUtils.strip(s);
        if (nullable && StringUtils.isEmpty(s)) {
            return s;
        }
        if (s == null) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
        }
        if (lengthMin != null && s.length() < lengthMin) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be more than {1} characters", name, lengthMin));
        }
        if (lengthMax != null && s.length() > lengthMax) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be more than {1} characters", name, lengthMax));
        }
        return s;
    }
    
    /**
     * Validate a string length. The string mustn't be empty.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @param lengthMin Minimum length (or null)
     * @param lengthMax Maximum length (or null)
     * @return String without white spaces
     */
    public static String validateLength(String s, String name, Integer lengthMin, Integer lengthMax) throws JSONException {
        return validateLength(s, name, lengthMin, lengthMax, false);
    }
    
    /**
     * Checks if the string is not null and is not only whitespaces.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @return String without white spaces
     */
    public static String validateStringNotBlank(String s, String name) throws JSONException {
        return validateLength(s, name, 1, null, false);
    }
    
    /**
     * Checks if the string is an email.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     */
    public static void validateEmail(String s, String name) throws JSONException {
        if (!EMAIL_PATTERN.matcher(s).matches()) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be an email", name));
        }
    }
    
    /**
     * Validates that the provided string matches an URL with HTTP or HTTPS scheme.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @return Stripped URL
     */
    public static String validateHttpUrl(String s, String name) throws JSONException {
        s = StringUtils.strip(s);
        if (!HTTP_URL_PATTERN.matcher(s).matches()) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be an HTTP(s) URL", name));
        }
        return s;
    }
    
    /**
     * Checks if the string uses only alphanumerical or underscore characters.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     */
    public static void validateAlphanumeric(String s, String name) throws JSONException {
        if (!ALPHANUMERIC_PATTERN.matcher(s).matches()) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must have only alphanumeric or underscore characters", name));
        }
    }
    
    /**
     * Validates and parses a date.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @param nullable True if the string can be empty or null
     * @return Parsed date
     */
    public static Date validateDate(String s, String name, boolean nullable) throws JSONException {
        if (Strings.isNullOrEmpty(s)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
            } else {
                return null;
            }
        }
        try {
            return new DateTime(Long.parseLong(s)).toDate();
        } catch (NumberFormatException e) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be a date", name));
        }
    }
}
