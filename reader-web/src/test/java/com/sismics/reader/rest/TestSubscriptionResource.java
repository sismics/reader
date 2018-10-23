package com.sismics.reader.rest;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.sismics.reader.core.model.context.AppContext;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import static junit.framework.Assert.*;

/**
 * Exhaustive test of the subscription resource.
 * 
 * @author jtremeaux
 */
public class TestSubscriptionResource extends BaseJerseyTest {
    /**
     * Test of the subscription add resource.
     * 
     */
    @Test
    public void testSubscriptionAddResource() throws JSONException {
        // Create user subscription1
        createUser("subscription1");
        login("subscription1");

        // Create a category
        PUT("/category", ImmutableMap.of("name", "techno"));
        assertIsOk();
        JSONObject json = getJsonResult();
        String category1Id = json.optString("id");
        assertNotNull(category1Id);
        
        // Subscribe to korben.info
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();
        json = getJsonResult();
        String subscription1Id = json.getString("id");
        assertNotNull(subscription1Id);
        
        // Move the korben.info subscription to "techno"
        POST("/subscription/" + subscription1Id, ImmutableMap.of("category", category1Id));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));

        // List all subscriptions
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        int unreadCount = json.optInt("unread_count");
        assertTrue(unreadCount > 0);
        JSONArray categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        categories = rootCategory.getJSONArray("categories");
        JSONObject technoCategory = categories.optJSONObject(0);
        JSONArray subscriptions = technoCategory.optJSONArray("subscriptions");
        assertEquals(1, subscriptions.length());
        JSONObject subscription = subscriptions.getJSONObject(0);
        assertEquals(10, subscription.getInt("unread_count"));
        assertEquals("http://localhost:9997/http/feeds/korben.xml", subscription.getString("url"));

        // Check the subscription data
        GET("/subscription/" + subscription1Id);
        assertIsOk();
        json = getJsonResult();
        subscription = json.optJSONObject("subscription");
        assertNotNull(subscription);
        assertEquals("Korben", subscription.optString("title"));
        assertEquals("Korben", subscription.optString("feed_title"));
        assertEquals("http://korben.info", subscription.optString("url"));
        assertEquals("Upgrade your mind", subscription.optString("description"));
        assertEquals("http://localhost:9997/http/feeds/korben.xml", subscription.optString("rss_url"));
        assertNotNull(subscription.optLong("create_date"));
        assertNotNull(subscription.optString("category_id"));
        assertEquals("techno", subscription.optString("category_name"));
        JSONArray articles = json.optJSONArray("articles");
        assertEquals(10, articles.length());
        JSONObject article = articles.optJSONObject(0);
        assertNotNull(article);
        String article0Id = article.getString("id");
        assertNotNull(article0Id);
        JSONObject articleSubscription = article.optJSONObject("subscription");
        assertNotNull(articleSubscription.getString("id"));
        assertNotNull(articleSubscription.getString("title"));
        assertNotNull(article.optString("comment_url"));
        article = (JSONObject) articles.get(1);
        String article1Id = article.getString("id");
        article = (JSONObject) articles.get(2);
        String article2Id = article.getString("id");

        // Check pagination
        GET("/subscription/" + subscription1Id, ImmutableMap.of("after_article", article1Id));
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(8, articles.length());
        assertEquals(article2Id, article.getString("id"));

        // Update the subscription
        POST("/subscription/" + subscription1Id, ImmutableMap.of(
                "order", Integer.valueOf(1).toString(),
                "title", "Korben.info"
        ));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // Check the updated subscription data
        GET("/subscription/" + subscription1Id);
        assertIsOk();
        json = getJsonResult();
        subscription = json.optJSONObject("subscription");
        assertNotNull(subscription);
        assertEquals("Korben.info", subscription.optString("title"));
        assertEquals("http://korben.info", subscription.optString("url"));
        assertEquals("Upgrade your mind", subscription.optString("description"));

        // Marks an article as read
        POST("/article/" + article0Id + "/read");
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // Marks an article as read (2nd time)
        POST("/article/" + article0Id + "/read");
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));

        // Check the subscription data
        GET("/subscription/" + subscription1Id, ImmutableMap.of("unread", "true"));
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(9, articles.length());

        // Check the subscription data
        GET("/subscription/" + subscription1Id, ImmutableMap.of("unread", "false"));
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());

        // Check all subscriptions for unread articles
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        assertEquals(9, json.optInt("unread_count"));

        // Marks an article as unread
        POST("/article/" + article0Id + "/unread");
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // Marks an article as unread (2nd time)
        POST("/article/" + article0Id + "/unread");
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));

        // Check the subscription data
        GET("/subscription/" + subscription1Id, ImmutableMap.of("unread", "true"));
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());

        // Marks all articles in this subscription as read
        POST("/subscription/" + subscription1Id + "/read");
        assertIsOk();

        // Check all subscriptions for unread articles
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        assertEquals(0, json.optInt("unread_count"));

        // Delete the subscription
        DELETE("/subscription/" + subscription1Id);
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
    }

    /**
     * Test of the article dates.
     *
     */
    @Test
    public void testArticleDate() throws JSONException {
        // Create a new user: OK
        createUser("subscription2");
        login("subscription2");

        // Subscribe to future date feed: OK
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/future_date.xml"));
        assertIsOk();
        JSONObject json = getJsonResult();
        String subscription1Id = json.getString("id");
        assertNotNull(subscription1Id);

        // Check the subscription data
        GET("/subscription/" + subscription1Id);
        assertIsOk();
        json = getJsonResult();
        JSONObject subscription = json.optJSONObject("subscription");
        assertNotNull(subscription);
        assertEquals("Feed from the future", subscription.optString("title"));
        JSONArray articles = json.optJSONArray("articles");
        assertEquals(1, articles.length());
        JSONObject article = articles.optJSONObject(0);
        assertNotNull(article);
        String article0Id = article.getString("id");
        assertNotNull(article0Id);
        assertEquals("This is an article from the future", article.getString("title"));
        assertTrue(new Date(article.getLong("date")).before(new Date()));
    }

    /**
     * Test of deleted articles.
     *
     */
    @Test
    public void testDeletedArticle() throws Exception {
        // Create a new user: OK
        createUser("subscription3");
        login("subscription3");

        // Subscribe to deleted feed: OK
        copyTempResource("/http/feeds/deleted/deleted0.xml");
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/temp/temp.xml"));
        assertIsOk();
        JSONObject json = getJsonResult();
        String feedSubscription0Id = json.getString("id");
        assertNotNull(feedSubscription0Id);

        // Check the subscription data: 3 articles
        GET("/subscription/" + feedSubscription0Id);
        assertIsOk();
        json = getJsonResult();
        JSONObject subscription = json.optJSONObject("subscription");
        assertNotNull(subscription);
        assertEquals("Deleted feeds", subscription.optString("title"));
        JSONArray articles = json.optJSONArray("articles");
        assertEquals(3, articles.length());
        JSONObject article = articles.getJSONObject(0);
        assertEquals("Article deleted2", article.getString("title"));
        article = articles.getJSONObject(1);
        assertEquals("Article deleted1", article.getString("title"));
        article = articles.getJSONObject(2);
        assertEquals("Article deleted0", article.getString("title"));

        // Check the subscription data: 3 unread articles
        GET("/subscription/");
        assertIsOk();
        json = getJsonResult();
        JSONArray categories = json.getJSONArray("categories");
        JSONObject rootCategory = categories.getJSONObject(0);
        JSONArray subscriptions = rootCategory.getJSONArray("subscriptions");
        subscription = subscriptions.getJSONObject(0);
        assertEquals(3, subscription.getInt("unread_count"));

        // Synchronize feeds: OK
        copyTempResource("/http/feeds/deleted/deleted1.xml");
        synchronizeAllFeed();

        // Check the subscription data: one deleted article
        GET("/subscription/" + feedSubscription0Id);
        assertIsOk();
        json = getJsonResult();
        subscription = json.optJSONObject("subscription");
        assertNotNull(subscription);
        assertEquals("Deleted feeds", subscription.optString("title"));
        articles = json.optJSONArray("articles");
        assertEquals(2, articles.length());
        article = articles.getJSONObject(0);
        assertEquals("Article deleted2", article.getString("title"));
        article = articles.getJSONObject(1);
        assertEquals("Article deleted0", article.getString("title"));

        // Check the subscription data: 2 unread articles
        GET("/subscription/");
        assertIsOk();
        json = getJsonResult();
        categories = json.getJSONArray("categories");
        rootCategory = categories.getJSONObject(0);
        subscriptions = rootCategory.getJSONArray("subscriptions");
        subscription = subscriptions.getJSONObject(0);
        assertEquals(2, subscription.getInt("unread_count"));

        // Synchronize feeds: OK
        copyTempResource("/http/feeds/deleted/deleted2.xml");
        synchronizeAllFeed();

        // Check the subscription data: one new articles, old articles are still there
        GET("/subscription/" + feedSubscription0Id);
        assertIsOk();
        json = getJsonResult();
        subscription = json.optJSONObject("subscription");
        assertNotNull(subscription);
        assertEquals("Deleted feeds", subscription.optString("title"));
        articles = json.optJSONArray("articles");
        assertEquals(3, articles.length());
        article = articles.getJSONObject(0);
        assertEquals("Article deleted3", article.getString("title"));
        article = articles.getJSONObject(1);
        assertEquals("Article deleted2", article.getString("title"));
        article = articles.getJSONObject(2);
        assertEquals("Article deleted0", article.getString("title"));

        // Check the subscription data: 3 unread articles
        GET("/subscription/");
        assertIsOk();
        json = getJsonResult();
        categories = json.getJSONArray("categories");
        rootCategory = categories.getJSONObject(0);
        subscriptions = rootCategory.getJSONArray("subscriptions");
        subscription = subscriptions.getJSONObject(0);
        assertEquals(3, subscription.getInt("unread_count"));
    }

    /**
     * Test of the subscription synchronization resource.
     *
     */
    @Test
    public void testSubscriptionSynchronizationResource() throws Exception {
        // Create user subscription_sync
        createUser("subscription_sync");
        login("subscription_sync");

        // Subscribe to korben.info
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();
        JSONObject json = getJsonResult();
        final String subscription1Id = json.getString("id");
        assertNotNull(subscription1Id);

        withNetworkDown(() -> {
            // Synchronize feeds
            synchronizeAllFeed();

            // Check the we don't get any synchronization update at all as the network is down
            GET("/subscription/" + subscription1Id + "/sync");
            assertIsOk();
            JSONObject json1 = getJsonResult();
            JSONArray synchronizations = json1.optJSONArray("synchronizations");
            assertNotNull(synchronizations);
            assertEquals(0, synchronizations.length());
        });

        // Synchronize feeds to add a feed synchronization entry
        synchronizeAllFeed();

        // Check the subscription synchronizations
        GET("/subscription/" + subscription1Id + "/sync");
        assertIsOk();
        json = getJsonResult();
        JSONArray synchronizations = json.optJSONArray("synchronizations");
        assertNotNull(synchronizations);
        assertEquals(1, synchronizations.length());
        assertTrue(synchronizations.getJSONObject(0).getBoolean("success"));
        assertFalse(synchronizations.getJSONObject(0).has("message"));
        assertTrue(synchronizations.getJSONObject(0).getInt("duration") > 0);
        
        // Check the subscriptions list (with zero errors)
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        JSONArray categories = json.optJSONArray("categories");
        JSONObject rootCategory = categories.optJSONObject(0);
        JSONArray subscriptions = rootCategory.optJSONArray("subscriptions");
        JSONObject subscription = subscriptions.getJSONObject(0);
        assertEquals(0, subscription.getInt("sync_fail_count"));
    }

    /**
     * Test of the import resource.
     * 
     */
    @Test
    public void testSubscriptionImportOpml() throws Exception {
        // Create user import_opml1
        createUser("import_opml1");
        login("import_opml1");

        // Import an OPML file
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream track = this.getClass().getResourceAsStream("/import/greader_subscriptions.xml");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(track),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        PUT("/subscription/import", form);
        assertIsOk();

        // List all subscriptions
        AppContext.getInstance().waitForAsync();
        GET("/subscription");
        assertIsOk();
        JSONObject json = getJsonResult();
        int unreadCount = json.optInt("unread_count");
        assertTrue(unreadCount > 0);
        JSONArray categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        categories = rootCategory.getJSONArray("categories");
        assertEquals(2, categories.length());
        JSONObject comicsCategory = categories.optJSONObject(0);
        assertEquals("Dev", comicsCategory.getString("name"));
        JSONArray subscriptions = comicsCategory.optJSONArray("subscriptions");
        assertEquals(1, subscriptions.length());
        
        // Export all subscriptions
        AppContext.getInstance().waitForAsync();
        GET("/subscription/export");
        assertIsOk();
        String text = CharStreams.toString(new InputStreamReader(response.getEntityInputStream(), Charsets.UTF_8));
        assertTrue(text.contains("Comics / Sub"));
        assertTrue(text.contains("Good Math, Bad Math"));
    }

    /**
     * Test of the import resource.
     * 
     */
    @Test
    public void testSubscriptionImportTakeout() throws Exception {
        // Create user import_takeout1
        createUser("import_takeout1");
        login("import_takeout1");

        // Import a Takeout file
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream track = this.getClass().getResourceAsStream("/import/test@gmail.com-takeout.zip");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(track),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        PUT("/subscription/import", form);
        assertIsOk();

        // List all subscriptions
        AppContext.getInstance().waitForAsync();
        GET("/subscription");
        assertIsOk();
        JSONObject json = getJsonResult();
        int unreadCount = json.optInt("unread_count");
        assertTrue(unreadCount > 0);
        JSONArray categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        categories = rootCategory.getJSONArray("categories");
        assertEquals(2, categories.length());
        JSONObject comicsCategory = categories.optJSONObject(0);
        assertEquals("Blogs", comicsCategory.getString("name"));
        JSONArray subscriptions = comicsCategory.optJSONArray("subscriptions");
        assertEquals(1, subscriptions.length());
        
        // Check the starred resource
        GET("/starred");
        assertIsOk();
        json = getJsonResult();
        JSONArray articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(3, articles.length());
    }
    
    /**
     * Test related to issue #110.
     * See https://github.com/sismics/reader/issues/110.
     * TODO Fixme 
     */
    @Test
    @Ignore
    public void testIssue110() throws JSONException {
        // Create user test_issue_110
        createUser("test_issue_110");
        login("test_issue_110");

        // Import a Takeout file
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream track = this.getClass().getResourceAsStream("/import/issue_110@gmail.com-takeout.zip");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(track),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        PUT("/subscription/import", form);
        assertIsOk();
        
        // Subscribe to questionablecontent.net
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/qc.xml"));
        assertIsOk();
        JSONObject json = getJsonResult();
        String subscription1Id = json.getString("id");
        assertNotNull(subscription1Id);
    }
    
    /**
     * Test related to issue #119.
     * See https://github.com/sismics/reader/issues/119.
     * issue_119.xml must be served somewhere and be editable.
     * Doesn't work automatically, use a breakpoing to edit the xml file.
     */
    @Test
    @Ignore
    public void testIssue119() throws JSONException {
        // Create user test_issue_119
        createUser("test_issue_119");
        login("test_issue_119");
        
        // Subscribe to korben.info
        PUT("/subscription", ImmutableMap.of("url", "http://localhost/korben.xml"));
        assertIsOk();
        JSONObject json = getJsonResult();
        String subscription1Id = json.getString("id");
        assertNotNull(subscription1Id);
        
        // Check all subscriptions for unread articles
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        assertEquals(3, json.optInt("unread_count"));
        
        // Check the subscription data
        GET("/subscription/" + subscription1Id);
        assertIsOk();
        json = getJsonResult();
        JSONArray articles = json.optJSONArray("articles");
        assertEquals(3, articles.length());
        
        // Delete the subscription
        DELETE("/subscription/" + subscription1Id);
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // At this moment, the subscription must have a new article
        
        // Subscribe again to korben.info
        PUT("/subscription", ImmutableMap.of("url", "http://localhost/korben.xml"));
        assertIsOk();
        json = getJsonResult();
        subscription1Id = json.getString("id");
        assertNotNull(subscription1Id);
        
        // Check all subscriptions for unread articles
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        assertEquals(4, json.optInt("unread_count"));
        
        // Check the subscription data
        GET("/subscription/" + subscription1Id);
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertEquals(4, articles.length());
    }
}