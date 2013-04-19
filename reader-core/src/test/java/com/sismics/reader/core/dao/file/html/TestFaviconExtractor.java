package com.sismics.reader.core.dao.file.html;

import java.io.InputStream;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import com.sismics.reader.core.util.ReaderHttpClient;

/**
 * Test of the RSS extractor.
 * 
 * @author jtremeaux
 */
public class TestFaviconExtractor {
    @Test
    public void faviconExtractorSlashdotTest() throws Exception {
        final FaviconExtractor extractor = new FaviconExtractor("http://slashdot.org");
        new ReaderHttpClient() {
            
            @Override
            public void process(InputStream is) throws Exception {
                extractor.readPage(is);
            }
        }.open(new URL("http://slashdot.org"));
        Assert.assertEquals("http://slashdot.org/favicon.ico", extractor.getFavicon());
    }
}
