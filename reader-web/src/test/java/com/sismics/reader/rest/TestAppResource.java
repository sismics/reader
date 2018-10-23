package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import com.sismics.rest.exception.ClientException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.*;

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
        assertNotNull(currentVersion);
        String minVersion = json.getString("min_version");
        assertNotNull(minVersion);
        Long freeMemory = json.getLong("free_memory");
        assertTrue(freeMemory > 0);
        Long totalMemory = json.getLong("total_memory");
        assertTrue(totalMemory > 0 && totalMemory > freeMemory);
        
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

        // Generate some error logs
        for (int i = 0; i < 20; i++) {
            new ClientException("type", "some error " + i, null);
        }

        // Check the logs (page 1)
        GET("/app/log", ImmutableMap.of("level", "ERROR"));
        assertIsOk();
        JSONObject json = getJsonResult();
        JSONArray logs = json.getJSONArray("logs");
        assertEquals(10, logs.length());
        Long date1 = logs.optJSONObject(0).optLong("date");
        Long date2 = logs.optJSONObject(9).optLong("date");
        assertTrue(date1 >= date2);
        
        // Check the logs (page 2)
        GET("/app/log", ImmutableMap.of(
                "offset",  "10",
                "level", "ERROR"));
        assertIsOk();
        json = getJsonResult();
        logs = json.getJSONArray("logs");
        assertTrue(logs.length() == 10);
        Long date3 = logs.optJSONObject(0).optLong("date");
        Long date4 = logs.optJSONObject(9).optLong("date");
        assertTrue(date3 >= date4);
    }
}