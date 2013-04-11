package com.sismics.reader.core.util;

import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.owasp.html.AttributePolicy;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sanitize the contents of an article : removes iframes, JS etc.
 *
 * @author jtremeaux 
 */
public class ArticleSanitizer {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ArticleSanitizer.class);

    /**
     * Feed website URL.
     */
    private String baseUrl;
    
    /**
     * Constructor of ArticleSanitizer.
     * 
     * @param baseUrl Feed website URL
     */
    public ArticleSanitizer(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private static final AttributePolicy INTEGER_POLICY = new AttributePolicy() {
        public String apply(String elementName, String attributeName, String value) {
            int n = value.length();
            if (n == 0) {
                return null;
            }
            for (int i = 0; i < n; ++i) {
                char ch = value.charAt(i);
                if (ch == '.') {
                    if (i == 0) {
                        return null;
                    }
                    return value.substring(0, i); // truncate to integer.
                } else if (!('0' <= ch && ch <= '9')) {
                    return null;
                }
            }
            return value;
        }
    };

    private AttributePolicy IMG_SRC_POLICY = new AttributePolicy() {
        
        @Override
        public @Nullable
        String apply(String elementName, String attributeName, String value) {
            try {
                return UrlUtil.completeUrl(baseUrl, value);
            } catch (MalformedURLException e) {
                if (log.isWarnEnabled()) {
                    log.warn(MessageFormat.format("Error transforming URL {0} to absolute with base URL {1}", value, baseUrl), e);
                }
                return value;
            }
        }
    };

    /**
    * Sanitize HTML contents.
    * 
    * @param html HTML to sanitize
    * @return Sanitized HTML
    */
    public String sanitize(String html) {
        // Allow YouTube iframes
        PolicyFactory videoPolicyFactory = new HtmlPolicyBuilder()
                .allowStandardUrlProtocols()
                .allowElements("iframe")
                .allowAttributes("src")
                .matching(Pattern.compile("http://(www.)?youtube.com/embed/.+"))
                .onElements("iframe")
                .disallowWithoutAttributes("iframe")
                .toFactory();
        
        // Allow images and transform relative links to absolute
        PolicyFactory imagePolicyFactory = new HtmlPolicyBuilder()
                .allowUrlProtocols("http", "https")
                .allowElements("img")
                .allowAttributes("alt").onElements("img")
                .allowAttributes("src").matching(IMG_SRC_POLICY).onElements("img")
                .allowAttributes("border", "height", "width").matching(INTEGER_POLICY).onElements("img")
                .toFactory();

        PolicyFactory policy = Sanitizers.BLOCKS
                .and(Sanitizers.FORMATTING)
                .and(imagePolicyFactory)
                .and(Sanitizers.LINKS)
                .and(Sanitizers.STYLES)
                .and(videoPolicyFactory);
        
        String safeHtml = policy.sanitize(html);
        
        return safeHtml;
    }
}
