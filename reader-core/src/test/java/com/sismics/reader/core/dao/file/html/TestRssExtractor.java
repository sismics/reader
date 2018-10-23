package com.sismics.reader.core.dao.file.html;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Test of the RSS extractor.
 * 
 * @author jtremeaux
 */
public class TestRssExtractor {
    @Test
    public void rssExtractKorbenTest() throws Exception {
        final RssExtractor extractor = new RssExtractor("http://korben.info");
        extractor.readPage(getClass().getResourceAsStream("/page/korben.html"));
        List<String> feedList = extractor.getFeedList();
        System.out.println(feedList);
        assertEquals(4, feedList.size());
        assertEquals("http://korben.info/feed", feedList.get(0));
        assertEquals("http://korben.info/feed/atom", feedList.get(1));
        assertEquals("http://korben.info/feed", feedList.get(2));
        assertEquals("http://korben.info/comments/feed", feedList.get(3));
    }

    @Test
    public void rssExtractXkcdTest() throws Exception {
        final RssExtractor extractor = new RssExtractor("http://xkcd.com");
        extractor.readPage(getClass().getResourceAsStream("/page/xkcd.html"));
        List<String> feedList = extractor.getFeedList();
        assertEquals(2, feedList.size());
        assertEquals("http://xkcd.com/atom.xml", feedList.get(0));
        assertEquals("http://xkcd.com/rss.xml", feedList.get(1));
    }

    @Test
    public void rssExtractSpaceTest() throws Exception {
        final RssExtractor extractor = new RssExtractor("http://space.com");
        extractor.readPage(getClass().getResourceAsStream("/page/space.html"));
        List<String> feedList = extractor.getFeedList();
        assertEquals(0, feedList.size()); // Bad space.com, you provide no RSS link
    }

    @Test
    public void rssExtractPloumTest() throws Exception {
        final RssExtractor extractor = new RssExtractor("http://www.ploum.net");
        extractor.readPage(getClass().getResourceAsStream("/page/ploum.html"));
        List<String> feedList = extractor.getFeedList();
        assertEquals(2, feedList.size());
        assertEquals("https://ploum.net/feed/", feedList.get(0));
        assertEquals("https://ploum.net/comments/feed/", feedList.get(1));
    }
    
    @Test
    public void rssExtractMakiko() throws Exception {
        final RssExtractor extractor = new RssExtractor("http://makiko-f.blogspot.fr/");
        extractor.readPage(getClass().getResourceAsStream("/page/makiko-f.blogspot.fr.html"));
        List<String> feedList = extractor.getFeedList();
        assertEquals(2, feedList.size());
        assertEquals("http://makiko-f.blogspot.com/feeds/posts/default", new FeedChooserStrategy().guess(feedList));
    }
}
