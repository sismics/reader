package com.sismics.reader.core.dao.file.json;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.io.Closer;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;

/**
 * Test of the starred JSON reader.
 * 
 * @author jtremeaux
 */
public class TestStarredReader {
    @Test
    public void starredReaderTest() throws Exception {
        Closer closer = Closer.create();
        InputStream is = null;
        try {
            is = closer.register(getClass().getResourceAsStream("/google_takeout/starred.json"));
            StarredReader starredReader = new StarredReader();
            starredReader.read(is);
            
            List<Feed> feedList = starredReader.getFeedList();
            Assert.assertEquals(2, feedList.size());
            Feed feed = feedList.get(0);
            Assert.assertEquals("http://blogs.lmax.com/rss20.xml", feed.getRssUrl());
            Assert.assertEquals("http://blogs.lmax.com/", feed.getUrl());
            Assert.assertEquals("LMAX Blogs", feed.getTitle());

            Map<String, List<Article>> articleMap = starredReader.getArticleMap();
            Assert.assertEquals(2, articleMap.size());
            List<Article> articleList = articleMap.get("http://blogs.lmax.com/rss20.xml");
            Assert.assertEquals(1, articleList.size());
            Article article = articleList.get(0);
            Assert.assertEquals("Adrian Rapan: Integration vs Acceptance tests", article.getTitle());
            Assert.assertNotNull(article.getDescription());
            Assert.assertTrue(article.getDescription().contains("Recently at LMAX"));

            articleList = articleMap.get("http://martinfowler.com/bliki/bliki.atom");
            Assert.assertEquals(2, articleList.size());
            article = articleList.get(0);
            Assert.assertEquals("An Appropriate Use of Metrics", article.getTitle());
            Assert.assertNotNull(article.getDescription());
            Assert.assertTrue(article.getDescription().contains("Like many of us"));

        } finally {
            closer.close();
        }
    }
}
