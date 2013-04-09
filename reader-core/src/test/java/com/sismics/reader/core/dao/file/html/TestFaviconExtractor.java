package com.sismics.reader.core.dao.file.html;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test of the RSS extractor.
 * 
 * @author jtremeaux
 */
public class TestFaviconExtractor {
    @Test
    public void faviconExtractorSlashdotTest() throws Exception {
        FaviconExtractor extractor = new FaviconExtractor("http://slashdot.org");
        extractor.readPage();
        Assert.assertEquals("http://slashdot.org/favicon.ico", extractor.getFavicon());
    }
}
