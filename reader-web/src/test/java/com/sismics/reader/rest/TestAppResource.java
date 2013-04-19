package com.sismics.reader.rest;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * Test the app resource.
 * 
 * @author jtremeaux
 */
public class TestAppResource extends BaseJerseyTest {
    /**
     * Test the API resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testAppResource() throws JSONException {
        // Check the API info
        WebResource appResource = resource().path("/app/version");
        ClientResponse response = appResource.get(ClientResponse.class);
        response = appResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String currentVersion = json.getString("current_version");
        Assert.assertNotNull(currentVersion);
        String minVersion = json.getString("min_version");
        Assert.assertNotNull(minVersion);
        
        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Rebuild articles index
        appResource = resource().path("/app/batch/reindex");
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = appResource.post(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        AppContext.getInstance().waitForAsync();
    }

    /**
     * Test the log resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testLogResource() throws JSONException {
        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Check the logs
        WebResource appResource = resource().path("/app/log");
        ClientResponse response = appResource.get(ClientResponse.class);
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = appResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        JSONArray logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() == 10);
    }
}