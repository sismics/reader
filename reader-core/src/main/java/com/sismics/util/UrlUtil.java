package com.sismics.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL utilities.
 *
 * @author jtremeaux 
 */
public class UrlUtil {

    /**
     * Completes and validates relative URLs.
     * 
     * @param baseUrl base URL
     * @param relativeUrl URL to complete
     * @return Completed URL
     * @throws MalformedURLException
     */
    public static String completeUrl(String baseUrl, String relativeUrl) throws MalformedURLException {
        // Trim URL
        baseUrl = baseUrl.trim();
        relativeUrl = relativeUrl.trim();
        
        // If the URL is already absolute, just validate
        if (relativeUrl.toLowerCase().startsWith("http")) {
            return new URL(relativeUrl).toString();
        }
        
        // Leading double slash: inherit the current protocol
        if (relativeUrl.startsWith("//")) {
            URL base = new URL(baseUrl);
            URL url = new URL(base.getProtocol() + ":" + relativeUrl);
            return url.toString();
        }
        
        URL base = new URL(baseUrl);
        String basePath = base.getPath() != null ? base.getPath() : "";
        if (!(basePath.endsWith("/") || relativeUrl.startsWith("/"))) {
            relativeUrl = "/" + relativeUrl;
        }
        if (basePath.endsWith("/") && relativeUrl.startsWith("/")) {
            relativeUrl = relativeUrl.substring(1);
        }
        URL url = new URL(base.getProtocol(), base.getHost(), base.getPort(), basePath + relativeUrl);
        return url.toString();
    }
}
