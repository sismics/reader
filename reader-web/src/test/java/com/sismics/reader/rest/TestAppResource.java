package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the app resource.
 * 
 * @author jtremeaux
 */
public class TestAppResource extends BaseJerseyTest {
    /**
     * Test the API resource.
     * 
     */
    @Test
    public void testAppResource() throws JSONException {
        // Check the application info
        GET("/app");
        assertIsOk();
        JSONObject json = getJsonResult();
        String currentVersion = json.getString("current_version");
        Assert.assertNotNull(currentVersion);
        String minVersion = json.getString("min_version");
        Assert.assertNotNull(minVersion);
        Long freeMemory = json.getLong("free_memory");
        Assert.assertTrue(freeMemory > 0);
        Long totalMemory = json.getLong("total_memory");
        Assert.assertTrue(totalMemory > 0 && totalMemory > freeMemory);
        
        // Login admin
        login("admin", "admin", false);
        
        // Rebuild articles index
        POST("/app/batch/reindex");
        assertIsOk();
    }

    /**
     * Test the map port resource.
     * 
     */
    @Test
    @Ignore
    public void testMapPortResource() throws JSONException {
        // Login admin
        login("admin", "admin", false);
        
        // Map port using UPnP
        POST("/app/map_port");
        assertIsOk();
    }
    
    /**
     * Test the log resource.
     * 
     */
    @Test
    public void testLogResource() throws JSONException {
        // Login admin
        login("admin", "admin", false);
        
        // Check the logs (page 1)
        GET("/app/log", ImmutableMap.of("level", "DEBUG"));
        assertIsOk();
        JSONObject json = getJsonResult();
        JSONArray logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() == 10);
        Long date1 = logs.optJSONObject(0).optLong("date");
        Long date2 = logs.optJSONObject(9).optLong("date");
        Assert.assertTrue(date1 >= date2);
        
        // Check the logs (page 2)
        GET("/app/log", ImmutableMap.of(
                "offset",  "10",
                "level", "DEBUG"));
        assertIsOk();
        json = getJsonResult();
        logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() == 10);
        Long date3 = logs.optJSONObject(0).optLong("date");
        Long date4 = logs.optJSONObject(9).optLong("date");
        Assert.assertTrue(date3 >= date4);
    }
}