package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Exhaustive test of the starred resource.
 * 
 * @author jtremeaux
 */
public class TestStarredResource extends BaseJerseyTest {
    /**
     * Test of the all resource.
     * 
     */
    @Test
    public void testStarredResource() throws JSONException {
        // Create user starred1
        createUser("starred1");
        login("starred1");

        // Subscribe to korben.info
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();
        JSONObject json = getJsonResult();
        String subscription1Id = json.optString("id");
        assertNotNull(subscription1Id);
        
        // Check the all resource
        GET("/all");
        assertIsOk();
        json = getJsonResult();
        JSONArray articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());
        JSONObject article0 = articles.getJSONObject(0);
        String article0Id = article0.getString("id");
        JSONObject article1 = articles.getJSONObject(1);
        String article1Id = article1.getString("id");

        // Create a new starred article
        PUT("/starred/" + article0Id);
        assertIsOk();

        // Create a new starred article
        PUT("/starred/" + article1Id);
        assertIsOk();

        // Check the starred resource
        GET("/starred");
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(2, articles.length());
        JSONObject articleAfter = articles.getJSONObject(0);
        String articleAfterId = articleAfter.getString("id");

        // Check pagination
        GET("/starred", ImmutableMap.of("after_article", articleAfterId));
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(1, articles.length());

        // Delete a starred article
        DELETE("/starred/" + article0Id);
        assertIsOk();

        // Check the starred resource
        GET("/starred");
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(1, articles.length());
        
        // Delete multiple starred articles
        POST("/starred/unstar", ImmutableMultimap.of(
                "id", article0Id,
                "id", article1Id));
        assertIsOk();

        // Check the starred resource
        GET("/starred");
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(0, articles.length());
        
        // Create multiple starred articles
        POST("/starred/star", ImmutableMultimap.of(
                "id", article0Id,
                "id", article1Id));
        assertIsOk();

        // Check the starred resource
        GET("/starred");
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(2, articles.length());
    }
}