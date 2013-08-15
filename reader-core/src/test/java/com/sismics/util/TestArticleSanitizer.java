package com.sismics.util;

import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.util.sanitizer.ArticleSanitizer;
import junit.framework.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * Test of the article sanitizer.
 * 
 * @author jtremeaux
 */
public class TestArticleSanitizer {
    /**
     * Tests the article sanitizer.
     * 
     * @throws Exception
     */
    @Test
    public void articleSanitizerImageAkeweaTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_akewea.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("http://blog.akewea.com/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(20, articleList.size());
        Article article = articleList.get(0);

        // Images: transform relative URLs to absolute
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        Assert.assertTrue(html.contains("\"http://blog.akewea.com/themes/akewea-4/smilies/redface.png\""));
        Assert.assertTrue(html.contains("\"http://blog.akewea.com/themes/akewea-4/smilies/test.png\""));
    }

    /**
     * Tests the article sanitizer.
     *
     * @throws Exception
     */
    @Test
    public void articleSanitizerImageDilbertTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_dilbert.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("http://dilbert.com/blog", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(20, articleList.size());
        Article article = articleList.get(0);

        // Images: transform relative URLs to absolute
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        Assert.assertTrue(html.contains("\"http://dilbert.com/dyn/tiny/File/photo.JPG\""));
    }

    /**
     * Tests the article sanitizer.
     * 
     * @throws Exception
     */
    @Test
    public void articleSanitizerImageXmlBaseTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_marijnhaverbeke.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("marijnhaverbeke.nl/blog", feed.getTitle());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(26, articleList.size());
        Article article = articleList.get(0);

        // Images: transform relative URLs to absolute
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        Assert.assertTrue(article.getDescription().contains("\"res/tern_simple_graph.png\""));
        String html = articleSanitizer.sanitize(article.getBaseUri(), article.getDescription());
        Assert.assertEquals("http://marijnhaverbeke.nl/blog/", article.getBaseUri());
        Assert.assertFalse(html.contains("\"res/tern_simple_graph.png\""));
        Assert.assertTrue(html.contains("\"http://marijnhaverbeke.nl/blog/res/tern_simple_graph.png\""));
    }

    /**
     * Tests the article sanitizer.
     * 
     * @throws Exception
     */
    @Test
    public void articleSanitizerIframeVimeoTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_fubiz.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("http://www.fubiz.net", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(10, articleList.size());
        Article article = articleList.get(0);

        // Allow iframes to Vimeo
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        Assert.assertTrue(html.contains("Déjà nominé dans la"));
        Assert.assertTrue(html.contains("<iframe src=\"http://player.vimeo.com/video/63898090?title&#61;0&amp;byline&#61;0&amp;portrait&#61;0&amp;color&#61;ffffff\" width=\"640\" height=\"360\">"));
    }

    /**
     * Tests the article sanitizer.
     * 
     * @throws Exception
     */
    @Test
    public void articleSanitizerIframeDailymotionTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_korben.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("http://korben.info", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(30, articleList.size());
        Article article = articleList.get(21);

        // Allow iframes to Vimeo
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        Assert.assertTrue(html.contains("On ne va pas faire les étonnés, hein"));
        Assert.assertTrue(html.contains("<iframe src=\"http://www.dailymotion.com/embed/video/xy9zdc\" height=\"360\" width=\"640\">"));
    }

    /**
     * Tests the article sanitizer.
     * 
     * @throws Exception
     */
    @Test
    public void articleSanitizerIframeYoutubeTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_korben.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("http://korben.info", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(30, articleList.size());
        Article article = articleList.get(20);

        // Allow iframes to Youtube
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        Assert.assertTrue(html.contains("Y&#39;a pas que XBMC dans la vie"));
        Assert.assertTrue(html.contains("<iframe src=\"http://www.youtube.com/embed/n2d4c8JIT0E?feature&#61;player_embedded\" height=\"360\" width=\"640\">"));
        
        // Allow iframes to Youtube HTTPS
        article = articleList.get(0);
        articleSanitizer = new ArticleSanitizer();
        html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        Assert.assertTrue(html.contains("La RetroN 5 sera équipée d&#39;une sortie HDMI"));
        Assert.assertTrue(html.contains("<iframe src=\"https://www.youtube.com/embed/5OcNy7t17LA?feature&#61;player_embedded\" height=\"360\" width=\"640\">"));
    }

    /**
     * Tests the article sanitizer.
     * 
     * @throws Exception
     */
    @Test
    public void articleSanitizerIframeSlashdotTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_slashdot.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("http://slashdot.org/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(25, articleList.size());
        Article article = articleList.get(0);

        // Allow unknown iframes
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        Assert.assertTrue(html.contains("Higgs data and the cosmic microwave background map from the Planck mission"));
        Assert.assertTrue(html.contains("<iframe src=\"http://slashdot.org/slashdot-it.pl?op&#61;discuss&amp;id&#61;3658423&amp;smallembed&#61;1\" style=\"height: 300px; width: 100%;\">"));
    }
}
