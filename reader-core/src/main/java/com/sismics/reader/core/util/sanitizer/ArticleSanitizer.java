package com.sismics.reader.core.util.sanitizer;

import com.sismics.util.UrlUtil;
import org.owasp.html.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.List;

/**
 * Sanitize the contents of an article: removes iframes, JS etc.
 *
 * @author jtremeaux 
 */
public class ArticleSanitizer {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ArticleSanitizer.class);

    private static final AttributePolicy INTEGER_POLICY = (elementName, attributeName, value) -> {
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
    };

    /**
     * Sanitize HTML contents.
     * 
     * @param baseUri Base URI
     * @param html HTML to sanitize
     * @return Sanitized HTML
     */
    public String sanitize(final String baseUri, String html) {
        AttributePolicy transformLinkToAbsolutePolicy = new AttributePolicy() {
            @Override
            public @Nullable
            String apply(String elementName, String attributeName, String value) {
                try {
                    return UrlUtil.completeUrl(baseUri, value);
                } catch (MalformedURLException e) {
                    if (log.isWarnEnabled()) {
                        log.warn(MessageFormat.format("Error transforming URL {0} to absolute with base URL {1}", value, baseUri), e);
                    }
                    return value;
                }
            }
        };

        // Allow common elements
        PolicyFactory blocksPolicyFactory = new HtmlPolicyBuilder()
                .allowElements(ElementPolicy.IDENTITY_ELEMENT_POLICY, 
                        "p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li",
                        "blockquote", "pre")
                .toFactory();
        
        // Allow iframes
        PolicyFactory iframePolicyFactory = new HtmlPolicyBuilder()
                .allowUrlProtocols("http", "https")
                .allowAttributes("src", "height", "width", "style")
                .matching(new AttributePolicy() {
                    @Override
                    public @Nullable
                    String apply(String elementName, String attributeName, String value) {
                        if ("height".equals(attributeName) || "width".equals(attributeName)) {
                            return value;
                        }
                        
                        if ("src".equals(attributeName)) {
                            // Deleting the protocol part
                            if (value.startsWith("https:")) value = value.substring(6);
                            else if (value.startsWith("http:")) value = value.substring(5);
                            
                            return value;
                        }
                        
                        // Sanitize CSS
                        if ("style".equals(attributeName)) {
                            if (value == null) {
                                return value;
                            }
                            String[] propertyList = value.split(";");
                            StringBuilder sb = new StringBuilder();
                            for (String property : propertyList) {
                                String[] subProperty = property.split(":");
                                if (subProperty.length != 2 || !subProperty[0].trim().equals("width") && !subProperty[0].trim().equals("height")) {
                                    continue;
                                }
                                sb.append(subProperty[0] + ":" + subProperty[1] + ";");
                            }
                            return sb.toString();
                        }
                        return null;
                    }
                })
                .onElements("iframe")
                .allowElements(new ElementPolicy() {
                    @Nullable
                    @Override
                    public String apply(String elementName, List<String> attrs) {
                        attrs.add("sandbox");
                        attrs.add("allow-scripts allow-same-origin");
                        return elementName;
                    }
                }, "iframe")
                .disallowWithoutAttributes("iframe")
                .toFactory();

        // Allow images and transform relative links to absolute
        PolicyFactory imagePolicyFactory = new HtmlPolicyBuilder()
                .allowUrlProtocols("http", "https", " http", " https")
                .allowElements("img")
                .allowAttributes("alt", "align", "title").onElements("img")
                .allowAttributes("src").matching(transformLinkToAbsolutePolicy).onElements("img")
                .allowAttributes("border", "height", "width", "hspace", "vspace").matching(INTEGER_POLICY).onElements("img")
                .toFactory();

        // Allow links and transform relative links to absolute
        PolicyFactory linksPolicyFactory = new HtmlPolicyBuilder()
                .allowStandardUrlProtocols()
                .allowElements("a")
                .allowAttributes("href")
                .matching(transformLinkToAbsolutePolicy)
                .onElements("a")
                .requireRelNofollowOnLinks()
                .toFactory();

        PolicyFactory policy = blocksPolicyFactory
                .and(Sanitizers.FORMATTING)
                .and(imagePolicyFactory)
                .and(linksPolicyFactory)
                .and(Sanitizers.STYLES)
                .and(iframePolicyFactory);
        
        String safeHtml = policy.sanitize(html);
        
        return safeHtml;
    }
}
