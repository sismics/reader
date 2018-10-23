package com.sismics.reader.rest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Test the theme resource.
 * 
 * @author jtremeaux
 */
public class TestThemeResource extends BaseJerseyTest {
    /**
     * Test the theme resource.
     * 
     */
    @Test
    public void testThemeResource() throws JSONException {
        GET("/theme");
        assertIsOk();
        JSONObject json = getJsonResult();
        JSONArray theme = json.getJSONArray("themes");
        assertTrue(theme.length() > 0);
    }
}