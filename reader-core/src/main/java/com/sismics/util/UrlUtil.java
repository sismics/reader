package com.sismics.util;

import com.google.common.base.Strings;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL utilities.
 *
 * @author jtremeaux 
 */
public class UrlUtil {

    /**
     * Get the relative URI for links in an article.
     *
     * @param feed Feed
     * @param article Article
     * @return Relative URI
     */
    public static String getBaseUri(Feed feed, Article article) {
        if (article.getBaseUri() != null) {
            // Use xml:base from Atom spec
            return article.getBaseUri();
        }
        if (feed.getBaseUri() != null) {
            // Use xml:base from Atom spec
            return feed.getBaseUri();
        }

        // Use the website root URL
        if (feed.getUrl() != null) {
            try {
                return getBaseUri(feed.getUrl());
            } catch (MalformedURLException e) {
                // NOP
            }
        }

        return null;
    }

    /**
     * Try to extract the base URL without the full path.
     * e.g. http://somehost.com/asset/img/ -> http://somehost.com/
     *
     * @param urlString Source URL
     * @return Extracted URL
     */
    public static String getBaseUri(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), "").toString();
    }

    /**
     * Completes and validates relative URLs.
     * 
     * @param baseUrl base URL
     * @param relativeUrl URL to complete
     * @return Completed URL
     */
    public static String completeUrl(String baseUrl, String relativeUrl) throws MalformedURLException {
        // Trim URL
        baseUrl = Strings.nullToEmpty(baseUrl).trim();
        relativeUrl = relativeUrl.trim();
        
        // If the URL is already absolute, just validate
        if (relativeUrl.toLowerCase().startsWith("http")) {
            return new URL(relativeUrl).toString();
        }
        
        // If the URL is already absolute, just validate
        if (relativeUrl.toLowerCase().startsWith("mailto:")) {
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
