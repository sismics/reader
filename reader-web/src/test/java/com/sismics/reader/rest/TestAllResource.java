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
 * Exhaustive test of the all resource.
 * 
 * @author jtremeaux
 */
public class TestAllResource extends BaseJerseyTest {
    /**
     * Test of the all resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testAllResource() throws JSONException {
        // Create user all1
        clientUtil.createUser("all1");
        String all1AuthToken = clientUtil.login("all1");

        // Subscribe to korben.info
        WebResource subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(all1AuthToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://korben.info/feed");
        ClientResponse response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String subscription1Id = json.optString("id");
        Assert.assertNotNull(subscription1Id);
        
        // Check the category tree
        subscriptionResource = resource().path("/category");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(all1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray categories = json.optJSONArray("categories");
        Assert.assertNotNull(categories);
        Assert.assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        String rootCategoryId = rootCategory.optString("id");
        Assert.assertNotNull(rootCategoryId);
        categories = rootCategory.optJSONArray("categories");
        Assert.assertEquals(0, categories.length());

        // Check the root category
        WebResource categoryResource = resource().path("/category/" + rootCategoryId);
        categoryResource.addFilter(new CookieAuthenticationFilter(all1AuthToken));
        response = categoryResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(10, articles.length());
        Integer total = json.optInt("total");
        Assert.assertTrue(total >= 10);

        // Check the all resource
        WebResource allResource = resource().path("/all");
        allResource.addFilter(new CookieAuthenticationFilter(all1AuthToken));
        response = allResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(10, articles.length());
        Assert.assertEquals(total.intValue(), json.optInt("total"));

        // Marks all articles as read
        allResource = resource().path("/all/read");
        allResource.addFilter(new CookieAuthenticationFilter(all1AuthToken));
        response = allResource.post(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Check the all resource
        allResource = resource().path("/all");
        allResource.addFilter(new CookieAuthenticationFilter(all1AuthToken));
        response = allResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(10, articles.length());
        Assert.assertEquals(total.intValue(), json.optInt("total"));

        // Check the all resource for unread articles
        allResource = resource().path("/all");
        allResource.addFilter(new CookieAuthenticationFilter(all1AuthToken));
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("unread", true);
        response = allResource.queryParams(queryParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(0, articles.length());
        Assert.assertEquals(0, json.optInt("total"));
    }
}