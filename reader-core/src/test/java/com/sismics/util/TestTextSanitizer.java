package com.sismics.util;

import com.sismics.reader.core.util.sanitizer.TextSanitizer;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

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
        assertEquals("", TextSanitizer.sanitize(null));
        assertEquals("Test title", TextSanitizer.sanitize("Test title"));
        assertEquals("Test title", TextSanitizer.sanitize("Test <pre>title</pre>"));
        assertEquals("Test title — a title", TextSanitizer.sanitize("Test title &mdash; a title"));
        assertEquals("Weirdest DLC Sponsorship Ever: SimCity, Brought To You By Crest", TextSanitizer.sanitize("Weirdest DLC Sponsorship Ever: SimCity&lt;/em&gt;, Brought To You By Crest"));
    }
}
