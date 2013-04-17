package com.sismics.reader.core.util.sanitizer;

import java.util.regex.Pattern;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Sanitize some HTML text contents. Removes all elements, converts HTML entities to unicode.
 *
 * @author jtremeaux 
 */
public class TextSanitizer {
    private final static PolicyFactory policy = new HtmlPolicyBuilder().toFactory();
    private final static Pattern TAG_PATTERN = Pattern.compile("&lt;.+&gt;");
    
    /**
    * Sanitize title contents.
    * 
    * @param html HTML to sanitize
    * @return Sanitized HTML
    */
    public static String sanitize(String html) {
        final String safeHtml = policy.sanitize(html);
        return TAG_PATTERN.matcher(safeHtml).replaceAll("");
    }
}
