package com.sismics.reader.core.dao.file.html;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.*;

/**
 * Test of the favicon downloader.
 * 
 * @author jtremeaux
 */
public class TestFaviconDownloader {
    @Test
    public void faviconDownloaderSlashdotTest() {
        // Download directly
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFavicon("https://slashdot.org/favicon.ico", tempDir, "favicon");
        assertNotNull(filename);
        assertTrue(new File(filename).exists());

        // Download from page
        downloader = new FaviconDownloader();
        filename = downloader.downloadFaviconFromPage("https://slashdot.org", tempDir, "favicon");
        assertNotNull(filename);
        assertTrue(new File(filename).exists());
    }

    @Test
    @Ignore
    public void faviconDownloaderBlipTest() {
        // Download directly
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFavicon("http://blip.fm/favicon.ico", tempDir, "favicon");
        assertNotNull(filename);
        assertTrue(new File(filename).exists());

        // Download from page
        downloader = new FaviconDownloader();
        filename = downloader.downloadFaviconFromPage("http://blip.fm", tempDir, "favicon");
        assertNotNull(filename);
        assertTrue(new File(filename).exists());
    }

    @Test
    @Ignore
    public void faviconDownloaderLyonUrbainTest() {
        // Download directly
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFavicon("http://www.lyon-urbain.com/favicon.ico", tempDir, "favicon");
        assertNull(filename);

        // Download from page
        downloader = new FaviconDownloader();
        filename = downloader.downloadFaviconFromPage("http://www.lyon-urbain.com", tempDir, "favicon");
        assertNull(filename);
    }

    @Test
    public void faviconDownloaderAbstruseGooseTest() {
        // Download from page
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFaviconFromPage("http://abstrusegoose.com", tempDir, "favicon");
        assertNotNull(filename);
        assertTrue(new File(filename).exists());
    }
    
    /**
     * Related to issue #2.
     * 
     */
    @Test
    public void faviconDownloaderDoubleFineTest() {
        // Download from page
        FaviconDownloader downloader = new FaviconDownloader();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = downloader.downloadFaviconFromPage("/projects/doublefine/double-fine-adventure/posts", tempDir, "favicon");
        if (filename == null) {
            // If nothing is found, try again with the RSS URL
            filename = downloader.downloadFaviconFromPage("https://www.kickstarter.com/projects/66710809/double-fine-adventure/posts.atom", tempDir, "favicon");
        }
        assertNotNull(filename);
        assertTrue(new File(filename).exists());
    }
}
