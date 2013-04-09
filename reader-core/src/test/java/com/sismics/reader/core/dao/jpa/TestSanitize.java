package com.sismics.reader.core.dao.jpa;

import java.util.regex.Pattern;

import org.junit.Test;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * @author jtremeaux
 */
public class TestSanitize {
    @Test
    public void sanitizeTester() {
        PolicyFactory videoPolicy = new HtmlPolicyBuilder()
            .allowStandardUrlProtocols()
            
            .allowElements("iframe")
            .allowAttributes("src")
            .matching(Pattern.compile("http://youtube.com/embed/.+"))
            .onElements("iframe")
            .disallowWithoutAttributes("iframe")
            
            .allowElements("a")
            .allowAttributes("href")
//            .matching(Pattern.compile("http://www.youtube.com/embed/.+"))
            .onElements("a")
            
            .toFactory();

        PolicyFactory policy = Sanitizers.BLOCKS
                .and(videoPolicy);
        System.out.println(policy.sanitize("<div>yo</div>\n" +
        		"<a href=\"http://youtube.com/embed/ploplop\">yo</a>\n" +
        		"<iframe src=\"http://youtube.com/embed/ploplop\">Hey</iframe>\n" +
        		"<iframe src=\"http://youfail.com/embed/ploplop\">Hey</iframe>"));
    }
}
