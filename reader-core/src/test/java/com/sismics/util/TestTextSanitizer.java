package com.sismics.util;

import com.sismics.reader.core.util.sanitizer.TextSanitizer;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Test of the text sanitizer.
 * 
 * @author jtremeaux
 */
public class TestTextSanitizer {
    /**
     * Tests the text sanitizer.
     * 
     */
    @Test
    public void textSanitizerTest() throws Exception {
        Assert.assertEquals("", TextSanitizer.sanitize(null));
        Assert.assertEquals("Test title", TextSanitizer.sanitize("Test title"));
        Assert.assertEquals("Test title", TextSanitizer.sanitize("Test <pre>title</pre>"));
        Assert.assertEquals("Test title — a title", TextSanitizer.sanitize("Test title &mdash; a title"));
        Assert.assertEquals("Weirdest DLC Sponsorship Ever: SimCity, Brought To You By Crest", TextSanitizer.sanitize("Weirdest DLC Sponsorship Ever: SimCity&lt;/em&gt;, Brought To You By Crest"));
    }
}
