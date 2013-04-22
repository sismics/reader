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
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Exhaustive test of the search resource.
 * 
 * @author bgamard
 */
public class TestSearchResource extends BaseJerseyTest {
    /**
     * Test of the search resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testSearchResource() throws JSONException {
        // Create user search1
        clientUtil.createUser("search1");
        String subscription1AuthToken = clientUtil.login("search1");

        // Subscribe to Korben RSS feed
        WebResource subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:9997/http/feeds/korben.xml");
        ClientResponse response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String subscription1Id = json.getString("id");
        Assert.assertNotNull(subscription1Id);
        
        // Wait indexing
        AppContext.getInstance().waitForAsync();
        
        // Search "zelda"
        WebResource searchResource = resource().path("/search/zelda");
        searchResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray articles = json.getJSONArray("articles");
        Assert.assertEquals(1, articles.length());
        JSONObject article = articles.getJSONObject(0);
        Assert.assertEquals("Quand <span class=\"highlight\">Zelda</span> prend les armes", article.getString("title"));
        
        // Search "something"
        searchResource = resource().path("/search/something");
        searchResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(0, articles.length());
        
        // Search "wifi"
        searchResource = resource().path("/search/wifi");
        searchResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(2, articles.length());
        Assert.assertEquals("Récupérer les clés <span class=\"highlight\">wifi</span> sur un téléphone Android", articles.getJSONObject(0).getString("title"));
        Assert.assertEquals("Partagez vos clés <span class=\"highlight\">WiFi</span> avec vos amis", articles.getJSONObject(1).getString("title"));
        
        // Search "google keep"
        searchResource = resource().path("/search/google%20keep");
        searchResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(2, articles.length());
    }
}