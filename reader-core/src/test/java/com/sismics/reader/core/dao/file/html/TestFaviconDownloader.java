package com.sismics.reader.core.dao.file.html;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test of the favicon downloader.
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
        Assert.assertNotNull(filename);
        Assert.assertTrue(new File(filename).exists());

        // Download from page
        downloader = new FaviconDownloader();
        filename = downloader.downloadFaviconFromPage("http://slashdot.org", tempDir, "favicon");
        Assert.assertNotNull(filename);
        Assert.assertTrue(new File(filename).exists());
    }

    @Test
    public void faviconDownloaderBlipTest() throws Exception {
        // Download directly
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFavicon("http://blip.fm/favicon.ico", tempDir, "favicon");
        Assert.assertNotNull(filename);
        Assert.assertTrue(new File(filename).exists());

        // Download from page
        downloader = new FaviconDownloader();
        filename = downloader.downloadFaviconFromPage("http://blip.fm", tempDir, "favicon");
        Assert.assertNotNull(filename);
        Assert.assertTrue(new File(filename).exists());
    }

    @Test
    public void faviconDownloaderLyonUrbainTest() throws Exception {
        // Download directly
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFavicon("http://www.lyon-urbain.com/favicon.ico", tempDir, "favicon");
        Assert.assertNull(filename);

        // Download from page
        downloader = new FaviconDownloader();
        filename = downloader.downloadFaviconFromPage("http://www.lyon-urbain.com", tempDir, "favicon");
        Assert.assertNull(filename);
    }

    @Test
    public void faviconDownloaderAbstruseGooseTest() throws Exception {
        // Download from page
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFaviconFromPage("http://abstrusegoose.com", tempDir, "favicon");
        Assert.assertNotNull(filename);
        Assert.assertTrue(new File(filename).exists());
    }
    
    @Test
    public void faviconDownloaderDoubleFineTest() throws Exception {
        // Download from page
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFaviconFromPage("http://www.kickstarter.com/projects/66710809/double-fine-adventure/posts.atom", tempDir, "favicon");
        Assert.assertNotNull(filename);
        Assert.assertTrue(new File(filename).exists());
    }
}
