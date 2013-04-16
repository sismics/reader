package com.sismics.reader.rest;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.reader.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Exhaustive test of the starred resource.
 * 
 * @author jtremeaux
 */
public class TestStarredResource extends BaseJerseyTest {
    /**
     * Test of the all resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testStarredResource() throws JSONException {
        // Create user starred1
        clientUtil.createUser("starred1");
        String starred1AuthToken = clientUtil.login("starred1");

        // Subscribe to korben.info
        WebResource subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(starred1AuthToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:9997/http/feeds/korben.xml");
        ClientResponse response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String subscription1Id = json.optString("id");
        Assert.assertNotNull(subscription1Id);
        
        // Check the all resource
        WebResource allResource = resource().path("/all");
        allResource.addFilter(new CookieAuthenticationFilter(starred1AuthToken));
        response = allResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(10, articles.length());
        JSONObject article1 = articles.getJSONObject(0);
        String article1Id = article1.getString("id");

        // Create a new starred article
        WebResource starredResource = resource().path("/starred/" + article1Id);
        starredResource.addFilter(new CookieAuthenticationFilter(starred1AuthToken));
        response = starredResource.put(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);

        // Check the starred resource
        starredResource = resource().path("/starred");
        starredResource.addFilter(new CookieAuthenticationFilter(starred1AuthToken));
        response = starredResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(1, articles.length());
        Assert.assertEquals(1, json.optInt("total"));

        // Deletes a new starred article
        starredResource = resource().path("/starred/" + article1Id);
        starredResource.addFilter(new CookieAuthenticationFilter(starred1AuthToken));
        response = starredResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);

        // Check the starred resource
        starredResource = resource().path("/starred");
        starredResource.addFilter(new CookieAuthenticationFilter(starred1AuthToken));
        response = starredResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(0, articles.length());
        Assert.assertEquals(0, json.optInt("total"));
    }
}