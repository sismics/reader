package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Exhaustive test of the category resource.
 * 
 * @author jtremeaux
 */
public class TestCategoryResource extends BaseJerseyTest {
    /**
     * Test of the category resource.
     * 
     */
    @Test
    public void testCategoryResource() throws JSONException {
        // Create user category1
        createUser("category1");
        login("category1");

        // Create a category : KO (name required)
        PUT("/category", ImmutableMap.of("name", " "));
        assertIsBadRequest();
        JSONObject json = getJsonResult();
        assertEquals("ValidationError", json.getString("type"));
        assertTrue(json.getString("message"), json.getString("message").contains("more than 1"));
        
        // Create a category
        PUT("/category", ImmutableMap.of("name", "techno"));
        assertIsOk();
        json = getJsonResult();
        String category1Id = json.optString("id");
        assertNotNull(category1Id);
        
        // Create a category
        PUT("/category", ImmutableMap.of("name", "comics"));
        assertIsOk();
        json = getJsonResult();
        String category2Id = json.optString("id");
        assertNotNull(category2Id);
        
        // Subscribe to korben.info
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();
        json = getJsonResult();
        String subscription1Id = json.optString("id");
        assertNotNull(subscription1Id);
        
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
        assertEquals(2, categories.length());
        JSONObject technologyCategory = categories.optJSONObject(0);
        assertEquals(category1Id, technologyCategory.optString("id"));
        assertEquals("techno", technologyCategory.optString("name"));
        JSONObject comicsCategory = categories.optJSONObject(1);
        assertEquals(category2Id, comicsCategory.optString("id"));
        assertEquals("comics", comicsCategory.optString("name"));

        // Check the root category
        GET("/category/" + rootCategoryId);
        assertIsOk();
        json = getJsonResult();
        JSONArray articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());

        // Move the korben.info subscription to "techno"
        POST("/subscription/" + subscription1Id, ImmutableMap.of("category", category1Id));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // Subscribe to xkcd.com
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/xkcd.xml"));
        assertIsOk();
        json = getJsonResult();
        String subscription2Id = json.optString("id");
        assertNotNull(subscription2Id);
        
        // Move the xkcd.com subscription to "comics"
        POST("/subscription/" + subscription2Id, ImmutableMap.of("category", category2Id));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // List all subscriptions
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        categories = rootCategory.optJSONArray("categories");
        assertEquals(2, categories.length());
        technologyCategory = categories.optJSONObject(0);
        assertEquals("techno", technologyCategory.optString("name"));
        assertEquals(false, technologyCategory.optBoolean("folded"));
        JSONArray subscriptions = technologyCategory.optJSONArray("subscriptions");
        assertEquals(1, subscriptions.length());
        comicsCategory = categories.optJSONObject(1);
        assertEquals("comics", comicsCategory.optString("name"));
        assertEquals(false, comicsCategory.optBoolean("folded"));
        subscriptions = comicsCategory.optJSONArray("subscriptions");
        assertEquals(1, subscriptions.length());

        // Update a category
        POST("/category/" + category1Id, ImmutableMap.of(
                "name", "technology",
                "order", "1",
                "folded", Boolean.TRUE.toString()
        ));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // Check category changes
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        int unreadCount = json.optInt("unread_count");
        assertTrue(unreadCount > 0);
        categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        categories = rootCategory.optJSONArray("categories");
        assertEquals(2, categories.length());
        comicsCategory = categories.optJSONObject(0);
        assertEquals("comics", comicsCategory.optString("name"));
        assertEquals(false, comicsCategory.optBoolean("folded"));
        Integer comicsUnreadCount = comicsCategory.optInt("unread_count");
        assertTrue(comicsUnreadCount > 0);
        technologyCategory = categories.optJSONObject(1);
        assertEquals("technology", technologyCategory.optString("name"));
        assertEquals(true, technologyCategory.optBoolean("folded"));
        assertTrue(technologyCategory.optInt("unread_count") > 0);

        // Marks all articles in the technology category as read
        POST("/category/" + category1Id + "/read");
        assertIsOk();

        // Check the category for unread articles
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        assertEquals(comicsUnreadCount.intValue(), json.optInt("unread_count"));
        categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        categories = rootCategory.optJSONArray("categories");
        assertEquals(2, categories.length());
        comicsCategory = categories.optJSONObject(0);
        assertEquals("comics", comicsCategory.optString("name"));
        assertEquals(false, comicsCategory.optBoolean("folded"));
        assertEquals(comicsUnreadCount.intValue(), comicsCategory.optInt("unread_count"));
        technologyCategory = categories.optJSONObject(1);
        assertEquals("technology", technologyCategory.optString("name"));
        assertEquals(true, technologyCategory.optBoolean("folded"));
        assertEquals(0, technologyCategory.optInt("unread_count"));

        // Check the category for only unread articles
        GET("/subscription", ImmutableMap.of("unread", Boolean.TRUE.toString()));
        assertIsOk();
        json = getJsonResult();
        assertEquals(comicsUnreadCount.intValue(), json.optInt("unread_count"));
        categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        categories = rootCategory.optJSONArray("categories");
        assertEquals(1, categories.length());
        comicsCategory = categories.optJSONObject(0);
        assertEquals("comics", comicsCategory.optString("name"));
        assertEquals(false, comicsCategory.optBoolean("folded"));
        assertEquals(comicsUnreadCount.intValue(), comicsCategory.optInt("unread_count"));

        // Deletes a category
        DELETE("/category/" + category1Id);
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
    }
}