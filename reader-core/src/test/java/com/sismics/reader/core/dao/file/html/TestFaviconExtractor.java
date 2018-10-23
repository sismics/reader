package com.sismics.reader.core.dao.file.html;

import com.sismics.reader.core.util.http.ReaderHttpClient;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.assertTrue;

/**
 * Test of the favicon extractor.
 * 
 * @author jtremeaux
 */
public class TestFaviconExtractor {
    @Test
    public void faviconExtractorSismicsTest() throws Exception {
        final FaviconExtractor extractor = new FaviconExtractor("https://www.sismics.com/");
        new ReaderHttpClient() {
            
            @Override
            public Void process(InputStream is) throws Exception {
                extractor.readPage(is);
                return null;
            }
        }.open(new URL("https://www.sismics.com"));
        assertTrue(extractor.getFavicon().contains("/public/img/favicon.png"));
    }
}
