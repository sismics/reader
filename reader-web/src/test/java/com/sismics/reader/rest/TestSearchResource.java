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
 * Exhaustive test of the search resource.
 * 
 * @author bgamard
 */
public class TestSearchResource extends BaseJerseyTest {
    /**
     * Test of the search resource.
     * @throws Exception 
     */
    @Test
    public void testSearchResource() throws Exception {
        // Create user search1
        clientUtil.createUser("search1");
        String search1AuthToken = clientUtil.login("search1");

        // Subscribe to Korben RSS feed
        WebResource subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:9997/http/feeds/korben.xml");
        ClientResponse response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        
        // Search "zelda"
        WebResource searchResource = resource().path("/search/zelda");
        searchResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray articles = json.getJSONArray("articles");
        Assert.assertEquals(1, articles.length());
        assertSearchResult(articles, "Quand <span class=\"highlight\">Zelda</span> prend les armes", 0);
        
        // Search "njloinzejrmklsjd"
        searchResource = resource().path("/search/njloinzejrmklsjd");
        searchResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(0, articles.length());
        
        // Search "wifi"
        searchResource = resource().path("/search/wifi");
        searchResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(2, articles.length());
        assertSearchResult(articles, "Récupérer les clés <span class=\"highlight\">wifi</span> sur un téléphone Android", 0);
        assertSearchResult(articles, "Partagez vos clés <span class=\"highlight\">WiFi</span> avec vos amis", 1);
        
        // Search "google keep"
        searchResource = resource().path("/search/google%20keep");
        searchResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(3, articles.length());
        assertSearchResult(articles, "Ask Slashdot: Measuring (and Constraining) Mobile Data Use?", 0);
        assertSearchResult(articles, "<span class=\"highlight\">Google</span> <span class=\"highlight\">Keep</span>…eut pas vraiment en faire plus (pour le moment)", 1);
        assertSearchResult(articles, "Quand Zelda prend les armes", 2);
        
        // Create user search2
        clientUtil.createUser("search2");
        String search2AuthToken = clientUtil.login("search2");

        // Subscribe to Korben RSS feed again to force articles updating
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(search2AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:9997/http/feeds/korben.xml");
        response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        
        // Check if nothing is broken by searching "google keep"
        searchResource = resource().path("/search/google%20keep");
        searchResource.addFilter(new CookieAuthenticationFilter(search2AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(3, articles.length());
        
        // Create user search3
        clientUtil.createUser("search3");
        String search3AuthToken = clientUtil.login("search3");
        
        // Search "njloinzejrmklsjd"
        searchResource = resource().path("/search/njloinzejrmklsjd");
        searchResource.addFilter(new CookieAuthenticationFilter(search3AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(0, articles.length());
        
        // Search "zelda"
        searchResource = resource().path("/search/zelda");
        searchResource.addFilter(new CookieAuthenticationFilter(search3AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(1, articles.length());
        assertSearchResult(articles, "Quand <span class=\"highlight\">Zelda</span> prend les armes", 0);
        
        // Subscribe to Korben RSS feed (alternative URL)
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:9997/http/feeds/korben2.xml");
        response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Search "zelda"
        searchResource = resource().path("/search/zelda");
        searchResource.addFilter(new CookieAuthenticationFilter(search3AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(1, articles.length());
        assertSearchResult(articles, "Quand <span class=\"highlight\">Zelda</span> prend les armes", 0);
    }
    
    /**
     * Assert that an article exists with a specific title in the provided articles set.
     * 
     * @param articles Articles from search
     * @param title Expected title
     * @param index Index
     * @throws JSONException
     */
    private void assertSearchResult(JSONArray articles, String title, int index) throws JSONException {
		JSONObject article = articles.getJSONObject(index);
		if (article.getString("title").equals(title)) {
			return;
		}
    	Assert.fail();
    }
}