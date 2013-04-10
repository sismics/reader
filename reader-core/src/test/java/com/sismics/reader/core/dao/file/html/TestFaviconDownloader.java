package com.sismics.reader.core.dao.file.html;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test of the RSS downloader.
 * 
 * @author jtremeaux
 */
public class TestFaviconDownloader {
    @Test
    public void faviconDownloaderSlashdotTest() throws Exception {
        // Download directly
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFavicon("http://slashdot.org/favicon.ico", tempDir, "favicon");
        Assert.assertEquals("favicon.ico", filename);
        Assert.assertTrue(new File(filename).exists());

        // Download from page
        downloader = new FaviconDownloader();
        filename = downloader.downloadFaviconFromPage("http://slashdot.org", tempDir, "favicon");
        Assert.assertEquals("favicon.ico", filename);
        Assert.assertTrue(new File(filename).exists());
    }
}
