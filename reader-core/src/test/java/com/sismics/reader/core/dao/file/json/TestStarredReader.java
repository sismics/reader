package com.sismics.reader.core.dao.file.json;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Closer;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;

/**
 * Test of the starred JSON reader.
 * 
 * @author jtremeaux
 */
public class TestStarredReader implements StarredArticleImportedListener {
    private List<Article> articleList;
    
    private List<Feed> feedList;
    
    @Before
    public void init() {
        articleList = new ArrayList<Article>();
        feedList = new ArrayList<Feed>();
    }
    
    @Test
    public void starredReaderTest() throws Exception {
        Closer closer = Closer.create();
        InputStream is = null;
        try {
            is = closer.register(getClass().getResourceAsStream("/google_takeout/starred.json"));
            StarredReader starredReader = new StarredReader();
            starredReader.setStarredArticleListener(this);
            starredReader.read(is);
            
            Assert.assertEquals(3, feedList.size());
            Feed feed = feedList.get(1);
            Assert.assertEquals("http://blogs.lmax.com/rss20.xml", feed.getRssUrl());
            Assert.assertEquals("http://blogs.lmax.com/", feed.getUrl());
            Assert.assertEquals("LMAX Blogs", feed.getTitle());

            Assert.assertEquals(3, articleList.size());
            Article article = articleList.get(1);
            Assert.assertEquals("http://www.testfeed.co.uk/integration-vs-acceptance-tests/", article.getUrl());
            Assert.assertEquals("Adrian Rapan: Integration vs Acceptance tests", article.getTitle());
            Assert.assertNotNull(article.getDescription());
            Assert.assertTrue(article.getDescription().contains("Recently at LMAX"));

            article = articleList.get(2);
            Assert.assertEquals("http://martinfowler.com/articles/bigData/", article.getUrl());
            Assert.assertEquals("Infodeck on Big Data", article.getTitle());
            Assert.assertNotNull(article.getDescription());
            Assert.assertTrue(article.getDescription().contains("“Big Data” has leapt rapidly"));

        } finally {
            closer.close();
        }
    }

    @Override
    public void onStarredArticleImported(final StarredArticleImportedEvent event) {
        feedList.add(event.getFeed());
        articleList.add(event.getArticle());
    }
}
