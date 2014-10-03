package com.sismics.reader.core.dao.file.html;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.sismics.reader.core.util.ReaderHttpClient;

/**
 * Test of the RSS extractor.
 * 
 * @author jtremeaux
 */
public class TestRssExtractor {
    @Test
    public void rssExtractKorbenTest() throws Exception {
        final RssExtractor extractor = new RssExtractor("http://korben.info");
        new ReaderHttpClient() {
            
            @Override
            public void process(InputStream is) throws Exception {
                extractor.readPage(is);
            }
        }.open(new URL("http://korben.info"));
        List<String> feedList = extractor.getFeedList();
        Assert.assertEquals(2, feedList.size());
        Assert.assertEquals("http://korben.info/feed", feedList.get(0));
        Assert.assertEquals("http://korben.info/feed/atom", feedList.get(1));
    }

    @Test
    public void rssExtractXkcdTest() throws Exception {
        final RssExtractor extractor = new RssExtractor("http://xkcd.com");
        new ReaderHttpClient() {
            
            @Override
            public void process(InputStream is) throws Exception {
                extractor.readPage(is);
            }
        }.open(new URL("http://xkcd.com"));
        List<String> feedList = extractor.getFeedList();
        Assert.assertEquals(2, feedList.size());
        Assert.assertEquals("http://xkcd.com/atom.xml", feedList.get(0));
        Assert.assertEquals("http://xkcd.com/rss.xml", feedList.get(1));
    }

    @Test
    public void rssExtractSpaceTest() throws Exception {
        final RssExtractor extractor = new RssExtractor("http://space.com");
        new ReaderHttpClient() {
            
            @Override
            public void process(InputStream is) throws Exception {
                extractor.readPage(is);
            }
        }.open(new URL("http://space.com"));
        List<String> feedList = extractor.getFeedList();
        Assert.assertEquals(0, feedList.size()); // Bad space.com, you provide no RSS link
    }

    @Test
    public void rssExtractPloumTest() throws Exception {
        final RssExtractor extractor = new RssExtractor("http://www.ploum.net");
        new ReaderHttpClient() {
            
            @Override
            public void process(InputStream is) throws Exception {
                extractor.readPage(is);
            }
        }.open(new URL("http://www.ploum.net"));
        List<String> feedList = extractor.getFeedList();
        Assert.assertEquals(2, feedList.size());
        Assert.assertEquals("https://ploum.net/feed/", feedList.get(0));
        Assert.assertEquals("https://ploum.net/comments/feed/", feedList.get(1));
    }
    
    @Test
    public void rssExtractMakiko() throws Exception {
        InputStream is = getClass().getResourceAsStream("/page/makiko-f.blogspot.fr.html");
        final RssExtractor extractor = new RssExtractor("http://makiko-f.blogspot.fr/");
        extractor.readPage(is);
        List<String> feedList = extractor.getFeedList();
        Assert.assertEquals(2, feedList.size());
        Assert.assertEquals("http://makiko-f.blogspot.com/feeds/posts/default", new FeedChooserStrategy().guess(feedList));
    }
}
