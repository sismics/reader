package com.sismics.reader.core.dao.file.html;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.sismics.reader.core.dao.file.html.FeedChooserStrategy;
import com.sismics.reader.core.dao.file.html.RssExtractor;

/**
 * Test of the RSS extractor.
 * 
 * @author jtremeaux
 */
public class TestRssExtractor {
    @Test
    public void rssExtractKorbenTest() throws Exception {
        RssExtractor extractor = new RssExtractor("http://korben.info");
        extractor.readPage();
        List<String> feedList = extractor.getFeedList();
        Assert.assertEquals(3, feedList.size());
        Assert.assertEquals("http://korben.info/feed", feedList.get(0));
        Assert.assertEquals("http://korben.info/feed/atom", feedList.get(1));
        Assert.assertEquals("http://korben.info/wp-content/plugins/nextgen-gallery/xml/media-rss.php", feedList.get(2));
    }

    @Test
    public void rssExtractXkcdTest() throws Exception {
        RssExtractor extractor = new RssExtractor("http://xkcd.com");
        extractor.readPage();
        List<String> feedList = extractor.getFeedList();
        Assert.assertEquals(2, feedList.size());
        Assert.assertEquals("http://xkcd.com/atom.xml", feedList.get(0));
        Assert.assertEquals("http://xkcd.com/rss.xml", feedList.get(1));
    }

    @Test
    public void rssExtractSpaceTest() throws Exception {
        RssExtractor extractor = new RssExtractor("http://space.com");
        extractor.readPage();
        List<String> feedList = extractor.getFeedList();
        Assert.assertEquals(0, feedList.size()); // Bad space.com, you provide no RSS link
    }

    @Test
    public void rssExtractMakiko() throws Exception {
        String url = new File(getClass().getResource("/page/makiko-f.blogspot.fr.html").getFile()).toURI().toString();
        RssExtractor extractor = new RssExtractor(url);
        extractor.readPage();
        List<String> feedList = extractor.getFeedList();
        Assert.assertEquals(2, feedList.size());
        Assert.assertEquals("http://makiko-f.blogspot.com/feeds/posts/default", new FeedChooserStrategy().guess(feedList));
    }
}
