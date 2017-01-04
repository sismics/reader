package com.sismics.reader.rest;

import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

/**
 * Test the locale resource.
 * 
 * @author jtremeaux
 */
public class TestLocaleResource extends BaseJerseyTest {
    /**
     * Test the locale resource.
     * 
     */
    @Test
    public void testLocaleResource() throws JSONException {
        GET("/locale");
        assertIsOk();
        JSONObject json = getJsonResult();
        JSONArray locale = json.getJSONArray("locales");
        Assert.assertTrue(locale.length() > 0);
    }
}