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
                URL url = new URL(feed.getUrl());
                return new URL(url.getProtocol(), url.getHost(), url.getPort(), "").toString();
            } catch (MalformedURLException e) {
                return feed.getUrl();
            }
        } else {
            return null;
        }
    }

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
        baseUrl = Strings.nullToEmpty(baseUrl).trim();
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
