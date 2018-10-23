package com.sismics.reader.core.dao.file.json;

import com.google.common.io.Closer;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

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
        InputStream is;
        try {
            is = closer.register(getClass().getResourceAsStream("/google_takeout/starred.json"));
            StarredReader starredReader = new StarredReader();
            starredReader.setStarredArticleListener(this);
            starredReader.read(is);
            
            assertEquals(3, feedList.size());
            Feed feed = feedList.get(1);
            assertEquals("http://blogs.lmax.com/rss20.xml", feed.getRssUrl());
            assertEquals("http://blogs.lmax.com/", feed.getUrl());
            assertEquals("LMAX Blogs", feed.getTitle());

            assertEquals(3, articleList.size());
            Article article = articleList.get(1);
            assertEquals("http://www.testfeed.co.uk/integration-vs-acceptance-tests/", article.getUrl());
            assertEquals("Adrian Rapan: Integration vs Acceptance tests", article.getTitle());
            assertNotNull(article.getDescription());
            assertTrue(article.getDescription().contains("Recently at LMAX"));

            article = articleList.get(2);
            assertEquals("http://martinfowler.com/articles/bigData/", article.getUrl());
            assertEquals("Infodeck on Big Data", article.getTitle());
            assertNotNull(article.getDescription());
            assertTrue(article.getDescription().contains("“Big Data” has leapt rapidly"));

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
