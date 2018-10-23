package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import junit.framework.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test of the RSS reader.
 * 
 * @author jtremeaux
 */
public class TestRssReader {
    @Test
    public void rssReaderGizmodoTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_gizmodo.gzip");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Gizmodo", feed.getTitle());
        assertEquals("http://gizmodo.com", feed.getUrl());
        assertEquals("en", feed.getLanguage());
        assertEquals("The Gadget Guide", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(25, articleList.size());
        Article article = articleList.get(0);
        assertEquals("IKEA Uses a Staggering One Percent of the World's Wood", article.getTitle());
        assertEquals("http://gizmodo.com/ikea-uses-a-staggering-one-percent-of-the-worlds-wood-677540490", article.getUrl());
        assertEquals("677540490", article.getGuid());
        assertNotNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("Plenty of critics would argue that IKEA is unnecessarily"));
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderApodTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_apod.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("APOD", feed.getTitle());
        assertEquals("http://antwrp.gsfc.nasa.gov/", feed.getUrl());
        assertEquals("en-us", feed.getLanguage());
        assertEquals("Astronomy Picture of the Day", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(7, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Saturn Hurricane", article.getTitle());
        assertEquals("http://antwrp.gsfc.nasa.gov/apod/astropix.html", article.getUrl());
        assertEquals("903a8aa15ad5b186f58e9d3de9e8cd80ab2d8a34", article.getGuid()); // GUID based on URL
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("http://antwrp.gsfc.nasa.gov/apod/calendar/S_130502.jpg"));
        assertNull(article.getPublicationDate());
    }

    @Test
    public void rssReaderKorbenTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_korben.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Korben", feed.getTitle());
        assertEquals("http://korben.info", feed.getUrl());
        assertEquals("fr-FR", feed.getLanguage());
        assertEquals("Upgrade your mind", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(30, articleList.size());
        
        Article article = articleList.get(0);
        assertEquals("RetroN 5 – La console pour les nostalgiques de la cartouche", article.getTitle());
        assertEquals("http://korben.info/retron-5.html", article.getUrl());
        assertEquals("http://korben.info/?p=38958", article.getGuid());
        assertEquals("Korben", article.getCreator());
        assertEquals("http://korben.info/retron-5.html#comments", article.getCommentUrl());
        assertEquals(Integer.valueOf(4), article.getCommentCount());
        assertTrue(article.getDescription().contains("Hyper"));
        assertNotNull(article.getPublicationDate());
        assertNull(article.getEnclosureUrl());
        assertNull(article.getEnclosureLength());
        assertNull(article.getEnclosureType());
        
        article = articleList.get(14);
        assertEquals("http://media.eurekalert.org/multimedia_prod/pub/media/54033.flv", article.getEnclosureUrl());
        assertEquals(Integer.valueOf(7172109), article.getEnclosureLength());
        assertEquals("video/x-flv", article.getEnclosureType());
    }

    @Test
    public void atomReaderPloumTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_ploum.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("ploum.net", feed.getTitle());
        assertEquals("http://ploum.net", feed.getUrl());
        assertEquals("en-US", feed.getLanguage());
        assertEquals("Le blog de Lionel Dricot", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(10, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Ce blog est payant", article.getTitle());
        assertEquals("http://ploum.net/post/ce-blog-est-payant", article.getUrl());
        assertEquals("http://ploum.net/?p=3030", article.getGuid());
        assertEquals("Lionel Dricot", article.getCreator());
        assertEquals("http://ploum.net/post/ce-blog-est-payant#comments", article.getCommentUrl());
        assertEquals(Integer.valueOf(8), article.getCommentCount());
        assertTrue(article.getDescription().contains("Voilà, ce blog est désormais officiellement un blog payant"));
        assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rssReaderRottenTomatoesTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_rottentomatoes.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Rotten Tomatoes: News", feed.getTitle());
        assertEquals("http://www.rottentomatoes.com/news/", feed.getUrl());
        assertEquals("en-us", feed.getLanguage());
        assertEquals("Entertainment news and headlines compiled by the editors of Rotten Tomatoes", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(20, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Parental Guidance: Iron Man 3 and The Guilt Trip", article.getTitle());
        assertEquals("http://www.rottentomatoes.com/m/1927377/news/1927377/", article.getUrl());
        assertEquals("http://www.rottentomatoes.com/m/1927377/news/1927377/", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("This week in family filmgoing"));
        assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rssReaderXkcdTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_xkcd.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("xkcd.com", feed.getTitle());
        assertEquals("http://xkcd.com/", feed.getUrl());
        assertEquals("en", feed.getLanguage());
        assertEquals("xkcd.com: A webcomic of romance and math humor.", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(4, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Voyager 1", article.getTitle());
        assertEquals("http://xkcd.com/1189/", article.getUrl());
        assertEquals("http://xkcd.com/1189/", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("So far Voyager 1"));
        assertNotNull(article.getPublicationDate());
    }

    @Test
    public void atomReaderXkcdTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_xkcd.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("xkcd.com", feed.getTitle());
        assertEquals("http://xkcd.com/", feed.getUrl());
        assertEquals("en", feed.getLanguage());
        assertNull(feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(4, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Voyager 1", article.getTitle());
        assertEquals("http://xkcd.com/1189/", article.getUrl());
        assertEquals("http://xkcd.com/1189/", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("So far Voyager 1"));
        assertNotNull(article.getPublicationDate());
    }

    @Test
    public void atomReaderWhatifTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_whatif.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("What If?", feed.getTitle());
        assertEquals("http://what-if.xkcd.com", feed.getUrl());
        assertNull(feed.getLanguage());
        assertNull(feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(3, articleList.size());
    }

    @Test
    public void rssReaderSpaceTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_space.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("SPACE.com", feed.getTitle());
        assertEquals("http://www.space.com/", feed.getUrl());
        assertEquals("en-us", feed.getLanguage());
        assertEquals("Something amazing every day.", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(50, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Collision Course? A Comet Heads for Mars", article.getTitle());
        assertEquals("http://www.space.com/20417-collision-course-a-comet-heads-for-mars.html", article.getUrl());
        assertNotNull(article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("A recently discovered comet"));
        assertNotNull(article.getPublicationDate());
    }

    /**
     * This feed has a invalid URL https://itunes.apple.com/it/book/earth-from-space/id632294005?l=en&mt=11
     * 
     */
    @Test
    public void rssReaderEsaTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_esa.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("ESA Top News", feed.getTitle());
        assertEquals("www.esa.int/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(18, articleList.size());
        Article article = articleList.get(12);
        assertEquals("Now on iTunes", article.getTitle());
        assertEquals("https://itunes.apple.com/it/book/earth-from-space/id632294005?l=en=11", article.getUrl());
        assertEquals("545b523e2bd4ff020907159181eaca1b", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("ESA's first iBook"));
        assertNotNull(article.getPublicationDate());
    }
    
    /**
     * This feed if encoded if ISO-8859-1.
     * 
     */
    @Test
    public void rssReaderLemessagerTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_lemessager.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Genevois : Le Messager", feed.getTitle());
        assertEquals("http://www.lemessager.fr", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(10, articleList.size());
        Article article = articleList.get(2);
        assertEquals("L´Annemassienne Dana brille à la Nouvelle Star", article.getTitle());
        assertEquals("http://www.lemessager.fr/Actualite/Genevois/2013/12/18/article_l_annemassienne_dana_brille_a_la_nouvell.shtml", article.getUrl());
        assertEquals("gps://story/1790664", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertNotNull(article.getPublicationDate());
    }

    @Test
    public void atomReaderMakikoTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_makiko.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Makiko Furuichi Blog", feed.getTitle());
        assertEquals("http://makiko-f.blogspot.com/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(25, articleList.size());
        Article article = articleList.get(0);
        assertEquals("くいだおれ", article.getTitle());
        assertEquals("http://makiko-f.blogspot.com/2013/04/blog-post.html", article.getUrl());
        assertEquals("http://www.blogger.com/comment.g?blogID=9184161806327478331&postID=186540250833288646", article.getCommentUrl());
        assertEquals("tag:blogger.com,1999:blog-9184161806327478331.post-186540250833288646", article.getGuid());
        assertEquals("Makiko Furuichi", article.getCreator());
        assertTrue(article.getDescription().contains("甘エビやホタルイカ、もちろん新鮮なお魚を始め"));
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void atomReaderMarijnhaverbekeTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_marijnhaverbeke.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("marijnhaverbeke.nl/blog", feed.getTitle());
        assertEquals("http://marijnhaverbeke.nl", feed.getBaseUri());
        List<Article> articleList = reader.getArticleList();
        assertEquals(26, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Tern", article.getTitle());
        assertEquals("http://marijnhaverbeke.nl/blog/tern.html", article.getUrl());
        assertEquals("http://marijnhaverbeke.nl/blog/tern.html", article.getGuid());
        assertEquals("http://marijnhaverbeke.nl/blog/", article.getBaseUri());
        assertNull(article.getCreator());
        assertTrue(article.getDescription().contains("I spend a rather large fraction of my days inside Emacs"));
        assertTrue("Feed contains relative links", article.getDescription().contains("\"res/tern_simple_graph.png\""));
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderPloumTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_ploum.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("ploum.net", feed.getTitle());
        assertEquals("http://ploum.net", feed.getUrl());
        assertEquals("en-US", feed.getLanguage());
        assertEquals("Le blog de Lionel Dricot", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(10, articleList.size());
        Article article = articleList.get(0);
        assertEquals("The Disruptive Free Price", article.getTitle());
        assertEquals("http://ploum.net/post/the-disruptive-free-price", article.getUrl());
        assertEquals("http://ploum.net/?p=2810", article.getGuid());
        assertEquals("Lionel Dricot", article.getCreator());
        assertEquals("http://ploum.net/post/the-disruptive-free-price#comments", article.getCommentUrl());
        assertEquals(Integer.valueOf(4), article.getCommentCount());
        assertTrue(article.getDescription().contains("During most of my life"));
        assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rssReaderDeveloperWorksTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_developerworks.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("IBM developerWorks : Java technology", feed.getTitle());
        assertEquals("http://www.ibm.com/developerworks/", feed.getUrl());
        assertEquals("en", feed.getLanguage());
        assertEquals("The latest content from IBM developerWorks", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(100, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Process real-time big data with Twitter Storm", article.getTitle());
        assertEquals("", article.getUrl());
        assertEquals("84fea3ea30ced7029c0ff7f617c0c8be695f5525", article.getGuid()); // GUID based on title/description
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("Storm is an open source"));
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderSpaceDailyTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_spacedaily.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Space News From SpaceDaily.Com", feed.getTitle());
        assertEquals("http://www.spacedaily.com/index.html", feed.getUrl());
        assertEquals("en-us", feed.getLanguage());
        List<Article> articleList = reader.getArticleList();
        assertEquals(15, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Collision Course? A Comet Heads for Mars", article.getTitle());
        assertEquals("http://www.spacedaily.com/reports/Collision_Course_A_Comet_Heads_for_Mars_999.html", article.getUrl());
        assertEquals("6ee9faf3505ca964beff280ba87e1be57ea7eee0", article.getGuid()); // GUID based on URL
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("Over the years, the spacefaring nations of Earth"));
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderDeveloppezTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_developpez.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Developpez.com Développement Web", feed.getTitle());
        assertEquals("http://web.developpez.com/index/rss", feed.getUrl());
        assertEquals("fr-FR", feed.getLanguage());
        List<Article> articleList = reader.getArticleList();
        assertEquals(20, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Quel navigateur Web recommandez-vous en 2013 ? Participez au débat sur le meilleur navigateur grand public", article.getTitle());
        assertEquals("http://web.developpez.com/actu/56767/Quel-navigateur-Web-recommandez-vous-en-2013-Participez-au-debat-sur-le-meilleur-navigateur-grand-public/", article.getUrl());
        assertEquals("http://web.developpez.com/actu/56767/Quel-navigateur-Web-recommandez-vous-en-2013-Participez-au-debat-sur-le-meilleur-navigateur-grand-public/", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertTrue(article.getDescription().contains("Depuis, les choses ont encore beaucoup évolué et nous aimerions connaitre votre avis actuel"));
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderHaverbekeTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_haverbeke.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("marijnhaverbeke.nl/blog", feed.getTitle());
        List<Article> articleList = reader.getArticleList();
        assertEquals(26, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Tern", article.getTitle());
        assertEquals("http://marijnhaverbeke.nl/blog/tern.html", article.getUrl());
        assertEquals("http://marijnhaverbeke.nl/blog/tern.html", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        // TODO SAX Parser ignore content if not wrapped in CDATA
        // assertTrue(article.getDescription().contains("I spend a rather large fraction of my days inside Emacs"));
        assertNotNull(article.getPublicationDate());
    }
    
    /**
     * Related to issue #84.
     * 
     */
    @Test
    public void rssReaderSlackwareTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_slackware.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Alien's Slackware packages", feed.getTitle());
        List<Article> articleList = reader.getArticleList();
        assertEquals(17, articleList.size());
        Article article = articleList.get(7);
        assertEquals("Mon,  9 Dec 2013 18:51:52 GMT", article.getTitle());
        assertEquals("http://www.slackware.com/~alien/slackbuilds/ChangeLog.txt", article.getUrl());
        assertEquals("20131209195152", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertNotNull(article.getPublicationDate());
    }
    
    /**
     * Related to issue #84.
     * 
     */
    @Test
    public void rssReaderMalikiTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_maliki.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Le webcomic", feed.getTitle());
        List<Article> articleList = reader.getArticleList();
        assertEquals(20, articleList.size());
        Article article = articleList.get(0);
        assertEquals("strip : Rétro gamines 2", article.getTitle());
        assertEquals("http://www.maliki.com/strip.php?strip=342", article.getUrl());
        assertEquals("70cf99d7c7347ec08b6cec6cd795b572a63481f2", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderNovaTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_nova.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("NOVA | PBS", feed.getTitle());
        List<Article> articleList = reader.getArticleList();
        assertEquals(35, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Killer Typhoon", article.getTitle());
        assertEquals("/wgbh/nova/earth/killer-typhoon.html", article.getUrl());
        assertEquals("/wgbh/nova/earth/killer-typhoon.html", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rdfReaderAutostripTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rdf_autostrip.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("autostrip", feed.getTitle());
        assertEquals("http://autostrip.fr/index.php", feed.getUrl());
        assertEquals("fr", feed.getLanguage());
        assertEquals("", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(10, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Bill, le cocker coquin...", article.getTitle());
        assertEquals("http://autostrip.fr/index.php?2013/06/09/214-bill-le-cocker-coquin", article.getUrl());
        assertEquals("http://autostrip.fr/index.php?2013/06/09/214-bill-le-cocker-coquin", article.getGuid());
        assertEquals("Tristan", article.getCreator());
        assertTrue(article.getDescription().contains("Ahahahahahahahah"));
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rdfReaderLxerTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rdf_lxer.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("LXer Linux News", feed.getTitle());
        assertEquals("http://lxer.com/", feed.getUrl());
        assertEquals("en-us", feed.getLanguage());
        assertEquals("Linux and Open Source news headlines", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(20, articleList.size());
        Article article = articleList.get(0);
        assertEquals("GCC vs. LLVM/Clang On The AMD Richland APU", article.getTitle());
        assertEquals("http://lxer.com/module/newswire/ext_link.php?rid=187870", article.getUrl());
        assertEquals("http://lxer.com/module/newswire/ext_link.php?rid=187870", article.getGuid());
        assertEquals("Michael Larabel", article.getCreator());
        assertTrue(article.getDescription().contains("Along with benchmarking the AMD A10-6800K"));
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rdfReaderMeisalamTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rdf_meisalam.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("琥珀色の小箱に恋をして:*:･･:*:･国際結婚生活日記:*:･･:*:･", feed.getTitle());
        assertEquals("http://meisalam.blog.fc2.com/", feed.getUrl());
        assertEquals("ja", feed.getLanguage());
        assertEquals("琥珀色の瞳をした彼に恋をして結婚。日本でポカポカ生息中。異文化交流の新婚生活と彼の観察日記（笑）を気ままに綴ります。", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(5, articleList.size());
        Article article = articleList.get(0);
        assertEquals("外免切替に挑戦！－④最終回", article.getTitle());
        assertEquals("http://meisalam.blog.fc2.com/blog-entry-105.html", article.getUrl());
        assertEquals("http://meisalam.blog.fc2.com/blog-entry-105.html", article.getGuid());
        assertEquals("meisa", article.getCreator());
        assertTrue(article.getDescription().contains("そして外免切替実技試験第二回を控えたある日。"));
        assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rdfReaderOatmealTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rdf_oatmeal.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("The Oatmeal - Comics, Quizzes, & Stories", feed.getTitle());
        assertEquals("http://theoatmeal.com/", feed.getUrl());
        assertNull(feed.getLanguage());
        assertEquals("The oatmeal tastes better than stale skittles found under the couch cushions", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        assertEquals(9, articleList.size());
        Article article = articleList.get(0);
        assertEquals("What the World War Z movie has in common with the book", article.getTitle());
        assertEquals("http://theoatmeal.com/comics/wwz", article.getUrl());
        assertEquals("http://theoatmeal.com/comics/wwz", article.getGuid());
        assertEquals("Matthew Inman", article.getCreator());
        assertTrue(article.getDescription().contains("What the World War Z movie has in common with the book"));
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderIboxTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_ibox.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("Topsport.bg - Испания", feed.getTitle());
        assertEquals("http://topsport.ibox.bg/section/id_20", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(20, articleList.size());
        Article article = articleList.get(0);
        assertEquals("Роналдо не спира да бележи, Реал набира скорост", article.getTitle());
        assertEquals("http://topsport.ibox.bg/news/id_636352968", article.getUrl());
        assertEquals("636352968", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertEquals("Португалецът с хеттрик", article.getDescription());
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderBysmeTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_bysme.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("by.S", feed.getTitle());
        assertEquals("http://by-s.me", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(30, articleList.size());
        Article article = articleList.get(0);
        assertEquals("キスの相性が悪いと別れる確率7割！？SEXの相性より大切な”キスの科学”", article.getTitle());
        assertEquals("http://by-s.me/article/161508051440523422", article.getUrl());
        assertEquals("http://by-s.me/article/161508051440523422", article.getGuid());
        assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderNasaTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_nasa.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        assertEquals("NASA Breaking News", feed.getTitle());
        assertEquals("http://www.nasa.gov/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        assertEquals(10, articleList.size());
        Article article = articleList.get(0);
        assertEquals("NASA Signs Agreement with Space Florida to Operate Historic Landing Facility", article.getTitle());
        assertEquals("http://www.nasa.gov/press-release/nasa-signs-agreement-with-space-florida-to-operate-historic-landing-facility-0", article.getUrl());
        assertEquals("http://www.nasa.gov/press-release/nasa-signs-agreement-with-space-florida-to-operate-historic-landing-facility-0", article.getGuid());
        assertNull(article.getCreator());
        assertNull(article.getCommentUrl());
        assertNull(article.getCommentCount());
        assertEquals("A new agreement marks another step in the transformation of NASA’s Kennedy Space Center in Florida to a multi-user spaceport. NASA’s historic Shuttle Landing Facility, the site of one of the longest runways in the world, has a new operator.", article.getDescription());
        assertNotNull(article.getPublicationDate());
    }

    @Test
    public void htmlLeMessagerFailTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_error_lemessager.html");
        RssReader reader = new RssReader();
        try {
            reader.readRssFeed(is);
            Assert.fail("Root element should be wrong");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Root element"));
        }
    }

    @Test
    public void htmlTooManyRecoveryTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_error_toomanyfatalerror.html");
        RssReader reader = new RssReader();
        try {
            reader.readRssFeed(is);
            Assert.fail("Root element should be wrong");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Tried to recover too many times"));
        }
    }
}
