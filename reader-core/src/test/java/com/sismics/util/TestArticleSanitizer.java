package com.sismics.util;

import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.util.sanitizer.ArticleSanitizer;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test of the article sanitizer.
 * 
 * @author jtremeaux
 */
public class TestArticleSanitizer {
    /**
     * Tests that the image relative URLs are transformed to absolute form.
     * 
     */
    @Test
    public void articleSanitizerImageAkeweaTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_akewea.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("http://blog.akewea.com/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(20, articleList.size());
        Article article = articleList.get(0);

        // Images: transform relative URLs to absolute
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String baseUrl = UrlUtil.getBaseUri(feed, article);
        String html = articleSanitizer.sanitize(baseUrl, article.getDescription());
        assertTrue(html.contains("\"http://blog.akewea.com/themes/akewea-4/smilies/redface.png\""));
        assertTrue(html.contains("\"http://blog.akewea.com/themes/akewea-4/smilies/test.png\""));
    }

    /**
     * Tests that the image relative URLs are transformed to absolute form.
     *
     */
    @Test
    public void articleSanitizerImageDilbertTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_dilbert.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("http://dilbert.com/blog", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(20, articleList.size());
        Article article = articleList.get(0);

        // Images: transform relative URLs to absolute
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String baseUrl = UrlUtil.getBaseUri(feed, article);
        String html = articleSanitizer.sanitize(baseUrl, article.getDescription());
        assertTrue(html.contains("\"http://dilbert.com/dyn/tiny/File/photo.JPG\""));
    }

    /**
     * Tests that the image relative URLs are transformed to absolute form.
     * 
     */
    @Test
    public void articleSanitizerImageXmlBaseTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_marijnhaverbeke.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("marijnhaverbeke.nl/blog", feed.getTitle());
        List<Article> articleList = reader.getArticleList();
        assertEquals(26, articleList.size());
        Article article = articleList.get(0);

        // Images: transform relative URLs to absolute
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        assertTrue(article.getDescription().contains("\"res/tern_simple_graph.png\""));
        assertEquals("http://marijnhaverbeke.nl/blog/", article.getBaseUri());
        String html = articleSanitizer.sanitize(article.getBaseUri(), article.getDescription());
        assertFalse(html.contains("\"res/tern_simple_graph.png\""));
        assertTrue(html.contains("\"http://marijnhaverbeke.nl/blog/res/tern_simple_graph.png\""));
    }

    /**
     * Tests that the image relative links are transformed to absolute form.
     *
     */
    @Test
    public void articleSanitizerLinkTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_github_user.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("naku’s Activity", feed.getTitle());
        List<Article> articleList = reader.getArticleList();
        assertEquals(7, articleList.size());
        Article article = articleList.get(0);

        // Links: transform relative URLs to absolute
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        assertTrue(article.getDescription().contains("\"/sismics/reader/commit/b7414b12d88c13b5af15df7d30ba8c1e47232d4d\""));
        String baseUrl = UrlUtil.getBaseUri(feed, article);
        assertEquals("https://github.com", baseUrl);
        String html = articleSanitizer.sanitize(baseUrl, article.getDescription());
        assertFalse(html.contains("\"/sismics/reader/commit/b7414b12d88c13b5af15df7d30ba8c1e47232d4d\""));
        assertTrue(html.contains("\"https://github.com/sismics/reader/commit/b7414b12d88c13b5af15df7d30ba8c1e47232d4d\""));
    }

    /**
     * Tests the article sanitizer.
     * 
     */
    @Test
    public void articleSanitizerIframeVimeoTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_fubiz.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("http://www.fubiz.net", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(10, articleList.size());
        Article article = articleList.get(0);

        // Allow iframes to Vimeo
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("Déjà nominé dans la"));
        assertTrue(html.contains("<iframe src=\"//player.vimeo.com/video/63898090?title&#61;0&amp;byline&#61;0&amp;portrait&#61;0&amp;color&#61;ffffff\" width=\"640\" height=\"360\" sandbox=\"allow-scripts allow-same-origin\">"));
    }

    /**
     * Tests the article sanitizer.
     * 
     */
    @Test
    public void articleSanitizerIframeDailymotionTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_korben.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("http://korben.info", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(30, articleList.size());
        Article article = articleList.get(21);

        // Allow iframes to Vimeo
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("On ne va pas faire les étonnés, hein"));
        assertTrue(html.contains("<iframe src=\"//www.dailymotion.com/embed/video/xy9zdc\" height=\"360\" width=\"640\" sandbox=\"allow-scripts allow-same-origin\">"));
    }

    /**
     * Tests the article sanitizer.
     * 
     */
    @Test
    public void articleSanitizerIframeYoutubeTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_korben.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("http://korben.info", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(30, articleList.size());
        Article article = articleList.get(20);

        // Allow iframes to Youtube
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("Y&#39;a pas que XBMC dans la vie"));
        assertTrue(html.contains("<iframe src=\"//www.youtube.com/embed/n2d4c8JIT0E?feature&#61;player_embedded\" height=\"360\" width=\"640\" sandbox=\"allow-scripts allow-same-origin\">"));
        
        // Allow iframes to Youtube HTTPS
        article = articleList.get(0);
        articleSanitizer = new ArticleSanitizer();
        html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("La RetroN 5 sera équipée d&#39;une sortie HDMI"));
        assertTrue(html.contains("<iframe src=\"//www.youtube.com/embed/5OcNy7t17LA?feature&#61;player_embedded\" height=\"360\" width=\"640\" sandbox=\"allow-scripts allow-same-origin\">"));
        
        // Allow iframes to Youtube without protocol
        article = articleList.get(15);
        articleSanitizer = new ArticleSanitizer();
        html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("Si quand vous étiez petit"));
        assertTrue(html.contains("<iframe src=\"//www.youtube.com/embed/7vIi0U4rSX4?feature&#61;player_embedded\" height=\"360\" width=\"640\" sandbox=\"allow-scripts allow-same-origin\">"));
    }
    
    /**
     * Tests the article sanitizer.
     * 
     */
    @Test
    public void articleSanitizerIframeGoogleMapsTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_korben.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        List<Article> articleList = reader.getArticleList();

        // Allow iframes to Google Maps
        Article article = articleList.get(15);
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("<iframe src=\"//maps.google.com/?t&#61;m&amp;layer&#61;c&amp;panoid&#61;JkQZAcDH9c2tky4T8irVUg&amp;cbp&#61;13,219.16,,0,41.84&amp;cbll&#61;35.370043,138.739238&amp;ie&#61;UTF8&amp;source&#61;embed&amp;ll&#61;35.336203,138.739128&amp;spn&#61;0.117631,0.216293&amp;z&#61;12&amp;output&#61;svembed\" height=\"420\" width=\"630\" sandbox=\"allow-scripts allow-same-origin\">"));
    }
    
    /**
     * Tests the article sanitizer.
     * 
     */
    @Test
    public void articleSanitizerIframeSoundCloudTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_korben.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        List<Article> articleList = reader.getArticleList();

        // Allow iframes to SoundCloud
        Article article = articleList.get(15);
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("<iframe height=\"166\" src=\"//w.soundcloud.com/player/?url&#61;http%3A%2F%2Fapi.soundcloud.com%2Ftracks%2F105401675\" width=\"100%\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
    }

    /**
     * Tests the article sanitizer.
     * 
     */
    @Test
    public void articleSanitizerIframeSlashdotTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_slashdot.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("http://slashdot.org/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(25, articleList.size());
        Article article = articleList.get(0);

        // Allow unknown iframes
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("Higgs data and the cosmic microwave background map from the Planck mission"));
        assertTrue(html.contains("<iframe src=\"//slashdot.org/slashdot-it.pl?op&#61;discuss&amp;id&#61;3658423&amp;smallembed&#61;1\" style=\"height: 300px; width: 100%;\" sandbox=\"allow-scripts allow-same-origin\">"));
    }

    /**
     * Tests the article sanitizer.
     *
     */
    @Test
    public void articleSanitizerIframeWhydTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_cultiz.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("http://cultiz.com/blog", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(10, articleList.size());
        Article article = articleList.get(2);

        // Allow unknown iframes
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("Quoi de mieux"));
        assertTrue(html.contains("<iframe src=\"//whyd.com/u/514ad8737e91c862b2ab7ef1/playlist/6?format&#61;embedV2&amp;embedW&#61;480\" height=\"600\" width=\"600\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
    }

    /**
     * Tests the article sanitizer.
     *
     */
    @Test
    public void articleSanitizerIframeBandcampTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_distractionware.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("http://distractionware.com/blog", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(10, articleList.size());
        Article article = articleList.get(4);

        // Allow unknown iframes
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("Naya’s Quest"));
        assertTrue(html.contains("<iframe style=\" width: 350px; height: 659px;\" src=\"//bandcamp.com/EmbeddedPlayer/album&#61;578259909/size&#61;large/bgcol&#61;ffffff/linkcol&#61;0687f5/transparent&#61;true/\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
    }
    
    /**
     * Tests the article sanitizer.
     *
     */
    @Test
    public void articleSanitizerIframeVineTest() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_distractionware2.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("http://distractionware.com/blog", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(10, articleList.size());
        Article article = articleList.get(0);

        // Allow unknown iframes
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(feed.getUrl(), article.getDescription());
        assertTrue(html.contains("Dark Souls"));
        assertTrue(html.contains("<iframe src=\"//vine.co/v/hUMBgqHAOdU/embed/simple\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
    }
    
    /**
     * Tests the article sanitizer related to issue #71.
     *
     */
    @Test
    public void articleSanitizer71Test() throws Exception {
        // Load a feed
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_whatif2.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("What If?", feed.getTitle());
        List<Article> articleList = reader.getArticleList();
        assertEquals(5, articleList.size());

        // Check that absolute URL are constructed from the feed base URL
        Article article = articleList.get(0);
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(UrlUtil.getBaseUri(feed, article), article.getDescription());
        assertTrue(html.contains("The Constant Groundskeeper"));
        assertFalse(html.contains("src=\"/imgs/a/70/lawn_cougar.png\""));
        assertTrue(html.contains("src=\"http://what-if.xkcd.com/imgs/a/70/lawn_cougar.png\""));
    }
    
    /**
     * Tests various iframes sanitizing related to issue #99.
     * 
     */
    @Test
    public void articleSanitizer99Test() throws Exception {
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize("http://localhost/", "<iframe src=\"http://www.deezer.com/plugins/player?autoplay=false&playlist=true\" width=\"480\" height=\"480\"></iframe>"
                + "<iframe src=\"//giphy.com/embed/9hDNJVBbDyZGw\" width=\"480\" height=\"480\"></iframe>"
                + "<iframe src=\"//www.slideshare.net/slideshow/embed_code/37454624\" width=\"480\" height=\"480\"></iframe>"
                + "<iframe src=\"//hitbox.tv/#!/embed/CymaticBruce\" width=\"480\" height=\"480\"></iframe>"
                + "<iframe src=\"//embed.spotify.com/?uri=spotify:album:3NNSJt3gWSmPmnjCwZyLA5\" width=\"480\" height=\"480\"></iframe>"
                + "<iframe src=\"//soundsgood.co/embed/5634c158722be1b60e7651be\" width=\"480\" height=\"480\"></iframe>"
                + "<iframe src=\"http://cdn.livestream.com/embed/spaceflightnow?layout=4&height=340&width=560&autoplay=false\" width=\"480\" height=\"480\"></iframe>"
                + "<iframe src=\"http://v.24liveblog.com/live/?id=1312491\" width=\"480\" height=\"480\"></iframe>"
                + "<iframe src=\"//fiddle.jshell.net/toddmotto/0oarywLe/show/light/\" width=\"480\" height=\"480\"></iframe>"
                + "<iframe src=\"//www.kickstarter.com/projects/223628811/the-airboard-sketch-internet-of-things-fast/widget/video.html\" width=\"480\" height=\"480\"></iframe>");
        System.out.println(html);
        assertTrue(html.contains("<iframe src=\"//www.deezer.com/plugins/player?autoplay&#61;false&amp;playlist&#61;true\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
        assertTrue(html.contains("<iframe src=\"//giphy.com/embed/9hDNJVBbDyZGw\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
        assertTrue(html.contains("<iframe src=\"//www.slideshare.net/slideshow/embed_code/37454624\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
        assertTrue(html.contains("<iframe src=\"//hitbox.tv/#!/embed/CymaticBruce\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
        assertTrue(html.contains("<iframe src=\"//embed.spotify.com/?uri&#61;spotify:album:3NNSJt3gWSmPmnjCwZyLA5\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
        assertTrue(html.contains("<iframe src=\"//soundsgood.co/embed/5634c158722be1b60e7651be\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
        assertTrue(html.contains("<iframe src=\"//cdn.livestream.com/embed/spaceflightnow?layout&#61;4&amp;height&#61;340&amp;width&#61;560&amp;autoplay&#61;false\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
        assertTrue(html.contains("<iframe src=\"//v.24liveblog.com/live/?id&#61;1312491\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
        assertTrue(html.contains("<iframe src=\"//fiddle.jshell.net/toddmotto/0oarywLe/show/light/\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
        assertTrue(html.contains("<iframe src=\"//www.kickstarter.com/projects/223628811/the-airboard-sketch-internet-of-things-fast/widget/video.html\" width=\"480\" height=\"480\" sandbox=\"allow-scripts allow-same-origin\"></iframe>"));
    }

    /**
     * Tests various iframes sanitizing related to issue #99.
     *
     */
    @Test
    public void articleSanitizerMailto()  {
        ArticleSanitizer articleSanitizer = new ArticleSanitizer();
        String html = articleSanitizer.sanitize(null, "<a href=\"mailto:test@test.com\">Mail us!</a>");
        System.out.println(html);
    }
}
