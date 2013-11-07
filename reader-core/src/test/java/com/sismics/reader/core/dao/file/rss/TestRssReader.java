package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import junit.framework.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

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
        Assert.assertEquals("Gizmodo", feed.getTitle());
        Assert.assertEquals("http://gizmodo.com", feed.getUrl());
        Assert.assertEquals("en", feed.getLanguage());
        Assert.assertEquals("The Gadget Guide", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(25, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("IKEA Uses a Staggering One Percent of the World's Wood", article.getTitle());
        Assert.assertEquals("http://gizmodo.com/ikea-uses-a-staggering-one-percent-of-the-worlds-wood-677540490", article.getUrl());
        Assert.assertEquals("677540490", article.getGuid());
        Assert.assertNotNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("Plenty of critics would argue that IKEA is unnecessarily"));
        Assert.assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderApodTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_apod.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("APOD", feed.getTitle());
        Assert.assertEquals("http://antwrp.gsfc.nasa.gov/", feed.getUrl());
        Assert.assertEquals("en-us", feed.getLanguage());
        Assert.assertEquals("Astronomy Picture of the Day", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(7, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Saturn Hurricane", article.getTitle());
        Assert.assertEquals("http://antwrp.gsfc.nasa.gov/apod/astropix.html", article.getUrl());
        Assert.assertEquals("903a8aa15ad5b186f58e9d3de9e8cd80ab2d8a34", article.getGuid()); // GUID based on URL
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("http://antwrp.gsfc.nasa.gov/apod/calendar/S_130502.jpg"));
        Assert.assertNull(article.getPublicationDate());
    }

    @Test
    public void rssReaderKorbenTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_korben.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("Korben", feed.getTitle());
        Assert.assertEquals("http://korben.info", feed.getUrl());
        Assert.assertEquals("fr-FR", feed.getLanguage());
        Assert.assertEquals("Upgrade your mind", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(30, articleList.size());
        
        Article article = articleList.get(0);
        Assert.assertEquals("RetroN 5 – La console pour les nostalgiques de la cartouche", article.getTitle());
        Assert.assertEquals("http://korben.info/retron-5.html", article.getUrl());
        Assert.assertEquals("http://korben.info/?p=38958", article.getGuid());
        Assert.assertEquals("Korben", article.getCreator());
        Assert.assertEquals("http://korben.info/retron-5.html#comments", article.getCommentUrl());
        Assert.assertEquals(Integer.valueOf(4), article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("Hyper"));
        Assert.assertNotNull(article.getPublicationDate());
        Assert.assertNull(article.getEnclosureUrl());
        Assert.assertNull(article.getEnclosureLength());
        Assert.assertNull(article.getEnclosureType());
        
        article = articleList.get(14);
        Assert.assertEquals("http://media.eurekalert.org/multimedia_prod/pub/media/54033.flv", article.getEnclosureUrl());
        Assert.assertEquals(Integer.valueOf(7172109), article.getEnclosureLength());
        Assert.assertEquals("video/x-flv", article.getEnclosureType());
    }

    @Test
    public void atomReaderPloumTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_ploum.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("ploum.net", feed.getTitle());
        Assert.assertEquals("http://ploum.net", feed.getUrl());
        Assert.assertEquals("en-US", feed.getLanguage());
        Assert.assertEquals("Le blog de Lionel Dricot", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(10, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Ce blog est payant", article.getTitle());
        Assert.assertEquals("http://ploum.net/post/ce-blog-est-payant", article.getUrl());
        Assert.assertEquals("http://ploum.net/?p=3030", article.getGuid());
        Assert.assertEquals("Lionel Dricot", article.getCreator());
        Assert.assertEquals("http://ploum.net/post/ce-blog-est-payant#comments", article.getCommentUrl());
        Assert.assertEquals(Integer.valueOf(8), article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("Voilà, ce blog est désormais officiellement un blog payant"));
        Assert.assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rssReaderRottenTomatoesTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_rottentomatoes.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("Rotten Tomatoes: News", feed.getTitle());
        Assert.assertEquals("http://www.rottentomatoes.com/news/", feed.getUrl());
        Assert.assertEquals("en-us", feed.getLanguage());
        Assert.assertEquals("Entertainment news and headlines compiled by the editors of Rotten Tomatoes", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(20, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Parental Guidance: Iron Man 3 and The Guilt Trip", article.getTitle());
        Assert.assertEquals("http://www.rottentomatoes.com/m/1927377/news/1927377/", article.getUrl());
        Assert.assertEquals("http://www.rottentomatoes.com/m/1927377/news/1927377/", article.getGuid());
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("This week in family filmgoing"));
        Assert.assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rssReaderXkcdTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_xkcd.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("xkcd.com", feed.getTitle());
        Assert.assertEquals("http://xkcd.com/", feed.getUrl());
        Assert.assertEquals("en", feed.getLanguage());
        Assert.assertEquals("xkcd.com: A webcomic of romance and math humor.", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(4, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Voyager 1", article.getTitle());
        Assert.assertEquals("http://xkcd.com/1189/", article.getUrl());
        Assert.assertEquals("http://xkcd.com/1189/", article.getGuid());
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("So far Voyager 1"));
        Assert.assertNotNull(article.getPublicationDate());
    }

    @Test
    public void atomReaderXkcdTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_xkcd.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("xkcd.com", feed.getTitle());
        Assert.assertEquals("http://xkcd.com/", feed.getUrl());
        Assert.assertEquals("en", feed.getLanguage());
        Assert.assertNull(feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(4, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Voyager 1", article.getTitle());
        Assert.assertEquals("http://xkcd.com/1189/", article.getUrl());
        Assert.assertEquals("http://xkcd.com/1189/", article.getGuid());
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("So far Voyager 1"));
        Assert.assertNotNull(article.getPublicationDate());
    }

    @Test
    public void atomReaderWhatifTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_whatif.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("What If?", feed.getTitle());
        Assert.assertEquals("http://what-if.xkcd.com", feed.getUrl());
        Assert.assertNull(feed.getLanguage());
        Assert.assertNull(feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(3, articleList.size());
    }

    @Test
    public void rssReaderSpaceTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_space.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("SPACE.com", feed.getTitle());
        Assert.assertEquals("http://www.space.com/", feed.getUrl());
        Assert.assertEquals("en-us", feed.getLanguage());
        Assert.assertEquals("Something amazing every day.", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(50, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Collision Course? A Comet Heads for Mars", article.getTitle());
        Assert.assertEquals("http://www.space.com/20417-collision-course-a-comet-heads-for-mars.html", article.getUrl());
        Assert.assertNotNull(article.getGuid());
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("A recently discovered comet"));
        Assert.assertNotNull(article.getPublicationDate());
    }

    /**
     * This feed has a invalid URL https://itunes.apple.com/it/book/earth-from-space/id632294005?l=en&mt=11
     * 
     * @throws Exception
     */
    @Test
    public void rssReaderEsaTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_esa.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("ESA Top News", feed.getTitle());
        Assert.assertEquals("www.esa.int/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(18, articleList.size());
        Article article = articleList.get(12);
        Assert.assertEquals("Now on iTunes", article.getTitle());
        Assert.assertEquals("https://itunes.apple.com/it/book/earth-from-space/id632294005?l=en=11", article.getUrl());
        Assert.assertEquals("545b523e2bd4ff020907159181eaca1b", article.getGuid());
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("ESA's first iBook"));
        Assert.assertNotNull(article.getPublicationDate());
    }

    @Test
    public void atomReaderMakikoTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_makiko.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("Makiko Furuichi Blog", feed.getTitle());
        Assert.assertEquals("http://makiko-f.blogspot.com/", feed.getUrl());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(25, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("くいだおれ", article.getTitle());
        Assert.assertEquals("http://makiko-f.blogspot.com/2013/04/blog-post.html", article.getUrl());
        Assert.assertEquals("http://www.blogger.com/comment.g?blogID=9184161806327478331&postID=186540250833288646", article.getCommentUrl());
        Assert.assertEquals("tag:blogger.com,1999:blog-9184161806327478331.post-186540250833288646", article.getGuid());
        Assert.assertEquals("Makiko Furuichi", article.getCreator());
        Assert.assertTrue(article.getDescription().contains("甘エビやホタルイカ、もちろん新鮮なお魚を始め"));
        Assert.assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void atomReaderMarijnhaverbekeTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_marijnhaverbeke.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("marijnhaverbeke.nl/blog", feed.getTitle());
        Assert.assertEquals("http://marijnhaverbeke.nl", feed.getBaseUri());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(26, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Tern", article.getTitle());
        Assert.assertEquals("http://marijnhaverbeke.nl/blog/tern.html", article.getUrl());
        Assert.assertEquals("http://marijnhaverbeke.nl/blog/tern.html", article.getGuid());
        Assert.assertEquals("http://marijnhaverbeke.nl/blog/", article.getBaseUri());
        Assert.assertNull(article.getCreator());
        Assert.assertTrue(article.getDescription().contains("I spend a rather large fraction of my days inside Emacs"));
        Assert.assertTrue("Feed contains relative links", article.getDescription().contains("\"res/tern_simple_graph.png\""));
        Assert.assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderPloumTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_ploum.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("ploum.net", feed.getTitle());
        Assert.assertEquals("http://ploum.net", feed.getUrl());
        Assert.assertEquals("en-US", feed.getLanguage());
        Assert.assertEquals("Le blog de Lionel Dricot", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(10, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("The Disruptive Free Price", article.getTitle());
        Assert.assertEquals("http://ploum.net/post/the-disruptive-free-price", article.getUrl());
        Assert.assertEquals("http://ploum.net/?p=2810", article.getGuid());
        Assert.assertEquals("Lionel Dricot", article.getCreator());
        Assert.assertEquals("http://ploum.net/post/the-disruptive-free-price#comments", article.getCommentUrl());
        Assert.assertEquals(Integer.valueOf(4), article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("During most of my life"));
        Assert.assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rssReaderDeveloperWorksTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_developerworks.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("IBM developerWorks : Java technology", feed.getTitle());
        Assert.assertEquals("http://www.ibm.com/developerworks/", feed.getUrl());
        Assert.assertEquals("en", feed.getLanguage());
        Assert.assertEquals("The latest content from IBM developerWorks", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(100, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Process real-time big data with Twitter Storm", article.getTitle());
        Assert.assertEquals("", article.getUrl());
        Assert.assertEquals("84fea3ea30ced7029c0ff7f617c0c8be695f5525", article.getGuid()); // GUID based on title/description
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("Storm is an open source"));
        Assert.assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderSpaceDailyTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_spacedaily.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("Space News From SpaceDaily.Com", feed.getTitle());
        Assert.assertEquals("http://www.spacedaily.com/index.html", feed.getUrl());
        Assert.assertEquals("en-us", feed.getLanguage());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(15, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Collision Course? A Comet Heads for Mars", article.getTitle());
        Assert.assertEquals("http://www.spacedaily.com/reports/Collision_Course_A_Comet_Heads_for_Mars_999.html", article.getUrl());
        Assert.assertEquals("6ee9faf3505ca964beff280ba87e1be57ea7eee0", article.getGuid()); // GUID based on URL
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("Over the years, the spacefaring nations of Earth"));
        Assert.assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderDeveloppezTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rss2_developpez.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("Developpez.com Développement Web", feed.getTitle());
        Assert.assertEquals("http://web.developpez.com/index/rss", feed.getUrl());
        Assert.assertEquals("fr-FR", feed.getLanguage());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(20, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Quel navigateur Web recommandez-vous en 2013 ? Participez au débat sur le meilleur navigateur grand public", article.getTitle());
        Assert.assertEquals("http://web.developpez.com/actu/56767/Quel-navigateur-Web-recommandez-vous-en-2013-Participez-au-debat-sur-le-meilleur-navigateur-grand-public/", article.getUrl());
        Assert.assertEquals("http://web.developpez.com/actu/56767/Quel-navigateur-Web-recommandez-vous-en-2013-Participez-au-debat-sur-le-meilleur-navigateur-grand-public/", article.getGuid());
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        Assert.assertTrue(article.getDescription().contains("Depuis, les choses ont encore beaucoup évolué et nous aimerions connaitre votre avis actuel"));
        Assert.assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rssReaderHaverbekeTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_atom_haverbeke.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("marijnhaverbeke.nl/blog", feed.getTitle());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(26, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Tern", article.getTitle());
        Assert.assertEquals("http://marijnhaverbeke.nl/blog/tern.html", article.getUrl());
        Assert.assertEquals("http://marijnhaverbeke.nl/blog/tern.html", article.getGuid());
        Assert.assertNull(article.getCreator());
        Assert.assertNull(article.getCommentUrl());
        Assert.assertNull(article.getCommentCount());
        // TODO SAX Parser ignore content if not wrapped in CDATA
        // Assert.assertTrue(article.getDescription().contains("I spend a rather large fraction of my days inside Emacs"));
        Assert.assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rdfReaderAutostripTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rdf_autostrip.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("autostrip", feed.getTitle());
        Assert.assertEquals("http://autostrip.fr/index.php", feed.getUrl());
        Assert.assertEquals("fr", feed.getLanguage());
        Assert.assertEquals("", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(10, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("Bill, le cocker coquin...", article.getTitle());
        Assert.assertEquals("http://autostrip.fr/index.php?2013/06/09/214-bill-le-cocker-coquin", article.getUrl());
        Assert.assertEquals("http://autostrip.fr/index.php?2013/06/09/214-bill-le-cocker-coquin", article.getGuid());
        Assert.assertEquals("Tristan", article.getCreator());
        Assert.assertTrue(article.getDescription().contains("Ahahahahahahahah"));
        Assert.assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rdfReaderLxerTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rdf_lxer.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("LXer Linux News", feed.getTitle());
        Assert.assertEquals("http://lxer.com/", feed.getUrl());
        Assert.assertEquals("en-us", feed.getLanguage());
        Assert.assertEquals("Linux and Open Source news headlines", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(20, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("GCC vs. LLVM/Clang On The AMD Richland APU", article.getTitle());
        Assert.assertEquals("http://lxer.com/module/newswire/ext_link.php?rid=187870", article.getUrl());
        Assert.assertEquals("http://lxer.com/module/newswire/ext_link.php?rid=187870", article.getGuid());
        Assert.assertEquals("Michael Larabel", article.getCreator());
        Assert.assertTrue(article.getDescription().contains("Along with benchmarking the AMD A10-6800K"));
        Assert.assertNotNull(article.getPublicationDate());
    }
    
    @Test
    public void rdfReaderMeisalamTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rdf_meisalam.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("琥珀色の小箱に恋をして:*:･･:*:･国際結婚生活日記:*:･･:*:･", feed.getTitle());
        Assert.assertEquals("http://meisalam.blog.fc2.com/", feed.getUrl());
        Assert.assertEquals("ja", feed.getLanguage());
        Assert.assertEquals("琥珀色の瞳をした彼に恋をして結婚。日本でポカポカ生息中。異文化交流の新婚生活と彼の観察日記（笑）を気ままに綴ります。", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(5, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("外免切替に挑戦！－④最終回", article.getTitle());
        Assert.assertEquals("http://meisalam.blog.fc2.com/blog-entry-105.html", article.getUrl());
        Assert.assertEquals("http://meisalam.blog.fc2.com/blog-entry-105.html", article.getGuid());
        Assert.assertEquals("meisa", article.getCreator());
        Assert.assertTrue(article.getDescription().contains("そして外免切替実技試験第二回を控えたある日。"));
        Assert.assertNotNull(article.getPublicationDate());
    }

    @Test
    public void rdfReaderOatmealTest() throws Exception {
        InputStream is = getClass().getResourceAsStream("/feed/feed_rdf_oatmeal.xml");
        RssReader reader = new RssReader();
        reader.readRssFeed(is);
        Feed feed = reader.getFeed();
        Assert.assertEquals("The Oatmeal - Comics, Quizzes, & Stories", feed.getTitle());
        Assert.assertEquals("http://theoatmeal.com/", feed.getUrl());
        Assert.assertNull(feed.getLanguage());
        Assert.assertEquals("The oatmeal tastes better than stale skittles found under the couch cushions", feed.getDescription());
        List<Article> articleList = reader.getArticleList();
        Assert.assertEquals(9, articleList.size());
        Article article = articleList.get(0);
        Assert.assertEquals("What the World War Z movie has in common with the book", article.getTitle());
        Assert.assertEquals("http://theoatmeal.com/comics/wwz", article.getUrl());
        Assert.assertEquals("http://theoatmeal.com/comics/wwz", article.getGuid());
        Assert.assertEquals("Matthew Inman", article.getCreator());
        Assert.assertTrue(article.getDescription().contains("What the World War Z movie has in common with the book"));
        Assert.assertNotNull(article.getPublicationDate());
    }
}
