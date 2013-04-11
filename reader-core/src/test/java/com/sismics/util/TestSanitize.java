package com.sismics.util;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.util.ArticleSanitizer;

/**
 * Test of the sanitizer.
 * 
 * @author jtremeaux
 */
public class TestSanitize {
    /**
     * Test of the OWASP sanitizer library. 
     */
    @Test
    public void sanitizeTester() {
        PolicyFactory videoPolicy = new HtmlPolicyBuilder()
            .allowStandardUrlProtocols()
            
            .allowElements("iframe")
            .allowAttributes("src")
            .matching(Pattern.compile("http://youtube.com/embed/.+"))
            .onElements("iframe")
            .disallowWithoutAttributes("iframe")
            
            .allowElements("a")
            .allowAttributes("href")
//            .matching(Pattern.compile("http://www.youtube.com/embed/.+"))
            .onElements("a")
            
            .toFactory();

        PolicyFactory policy = Sanitizers.BLOCKS
                .and(videoPolicy);
        System.out.println(policy.sanitize("<div>yo</div>\n" +
        		"<a href=\"http://youtube.com/embed/ploplop\">yo</a>\n" +
        		"<iframe src=\"http://youtube.com/embed/ploplop\">Hey</iframe>\n" +
        		"<iframe src=\"http://youfail.com/embed/ploplop\">Hey</iframe>"));
    }

    /**
     * Tests the article sanitizer.
     * 
     * @throws Exception
     */
    @Test
    public void articleSanitizerTester() throws Exception {
        // Load a feed
        String url = new File(getClass().getResource("/feed/feed_atom_akewea.xml").getFile()).toURI().toString();
        RssReader reader = new RssReader(url);
        reader.readRssFeed();
        Feed feed = reader.getFeed();
        Assert.assertEquals("http://blog.akewea.com/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(20, articleList.size());
        Article article = articleList.get(0);

        // Transform relative link in images
        ArticleSanitizer articleSanitizer = new ArticleSanitizer(feed.getUrl());
        String html = articleSanitizer.sanitize(article.getDescription());
        Assert.assertTrue(html.contains("http://blog.akewea.com/themes/akewea-4/smilies/redface.png\""));
    }
}
