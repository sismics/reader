package com.sismics.reader.rest;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.reader.rest.BaseJerseyTest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * Test the API resource.
 * 
 * @author jtremeaux
 */
public class TestApiResource extends BaseJerseyTest {
    /**
     * Test the API resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testApiResource() throws JSONException {
        // Check the API info
        WebResource activityResource = resource().path("/api/info");
        ClientResponse response = activityResource.get(ClientResponse.class);
        response = activityResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String currentVersion = json.getString("current_version");
        Assert.assertNotNull(currentVersion);
        String minVersion = json.getString("min_version");
        Assert.assertNotNull(minVersion);
    }
}