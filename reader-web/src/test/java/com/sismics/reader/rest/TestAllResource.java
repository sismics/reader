package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Exhaustive test of the all resource.
 * 
 * @author jtremeaux
 */
public class TestAllResource extends BaseJerseyTest {
    /**
     * Test of the all resource.
     * 
     */
    @Test
    public void testAllResource() throws JSONException {
        // Create user all1
        createUser("all1");
        login("all1");

        // Subscribe to korben.info
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();
        JSONObject json = getJsonResult();
        String subscription0Id = json.optString("id");
        assertNotNull(subscription0Id);
        
        // Check the category tree
        GET("/category");
        assertIsOk();
        json = getJsonResult();
        JSONArray categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        String rootCategoryId = rootCategory.optString("id");
        assertNotNull(rootCategoryId);
        categories = rootCategory.optJSONArray("categories");
        assertEquals(0, categories.length());

        // Check the root category
        GET("/category/" + rootCategoryId);
        assertIsOk();
        json = getJsonResult();
        JSONArray articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());
        JSONObject article = (JSONObject) articles.get(1);
        String article1Id = article.getString("id");
        article = (JSONObject) articles.get(2);
        String article2Id = article.getString("id");

        // Check pagination
        GET("/category/" + rootCategoryId, ImmutableMap.of("after_article", article1Id));
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(8, articles.length());
        assertEquals(article2Id, article.getString("id"));

        // Check the all resource
        GET("/all");
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());
        article = (JSONObject) articles.get(1);
        article1Id = article.getString("id");
        article = (JSONObject) articles.get(2);
        article2Id = article.getString("id");

        // Check pagination
        GET("/all", ImmutableMap.of("after_article", article1Id));
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(8, articles.length());
        assertEquals(article2Id, article.getString("id"));

        // Marks all articles as read
        POST("/all/read");
        assertIsOk();

        // Check the all resource
        GET("/all");
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());

        // Check in the subscriptions that there are no unread articles left
        GET("/subscription", ImmutableMap.of("after_article", article1Id));
        assertIsOk();
        json = getJsonResult();
        assertEquals(0, json.optInt("unread_count"));
        categories = json.getJSONArray("categories");
        rootCategory = categories.getJSONObject(0);
        JSONArray subscriptions = rootCategory.getJSONArray("subscriptions");
        JSONObject subscription0 = subscriptions.getJSONObject(0);
        assertEquals(0, subscription0.optInt("unread_count"));

        // Check the all resource for unread articles
        GET("/all", ImmutableMap.of("unread", Boolean.TRUE.toString()));
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(0, articles.length());
    }

    @Test
    public void testMultipleUsers() throws JSONException {
        // Create user multiple1
        createUser("multiple1");
        login("multiple1");

        // Subscribe to korben.info
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();
        JSONObject json = getJsonResult();
        String subscription0Id = json.optString("id");
        assertNotNull(subscription0Id);
        
        // Check the all resource
        GET("/all", ImmutableMap.of("unread", "true"));
        assertIsOk();
        json = getJsonResult();
        JSONArray articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());

        // Create user multiple2
        createUser("multiple2");
        login("multiple2");

        // Subscribe to korben.info (alternative URL)
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben2.xml"));
        assertIsOk();
        json = getJsonResult();
        subscription0Id = json.optString("id");
        assertNotNull(subscription0Id);
        
        // Check the all resource
        GET("/all", ImmutableMap.of("unread", "true"));
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());
    }
}