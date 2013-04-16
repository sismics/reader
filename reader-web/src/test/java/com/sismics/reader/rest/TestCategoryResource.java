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
 * Exhaustive test of the category resource.
 * 
 * @author jtremeaux
 */
public class TestCategoryResource extends BaseJerseyTest {
    /**
     * Test of the category resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testCategoryResource() throws JSONException {
        // Create user category1
        clientUtil.createUser("category1");
        String category1AuthToken = clientUtil.login("category1");

        // Create a category : KO (name required)
        WebResource categoryResource = resource().path("/category");
        categoryResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("name", " ");
        ClientResponse response = categoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("more than 1"));
        
        // Create a category
        categoryResource = resource().path("/category");
        categoryResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("name", "techno");
        response = categoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String category1Id = json.optString("id");
        Assert.assertNotNull(category1Id);
        
        // Create a category
        categoryResource = resource().path("/category");
        categoryResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("name", "comics");
        response = categoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String category2Id = json.optString("id");
        Assert.assertNotNull(category2Id);
        
        // Subscribe to korben.info
        WebResource subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:2501/http/feeds/korben.xml");
        response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String subscription1Id = json.optString("id");
        Assert.assertNotNull(subscription1Id);
        
        // Check the category tree
        subscriptionResource = resource().path("/category");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
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
        Assert.assertEquals(2, categories.length());
        JSONObject technologyCategory = categories.optJSONObject(0);
        Assert.assertEquals(category1Id, technologyCategory.optString("id"));
        Assert.assertEquals("techno", technologyCategory.optString("name"));
        JSONObject comicsCategory = categories.optJSONObject(1);
        Assert.assertEquals(category2Id, comicsCategory.optString("id"));
        Assert.assertEquals("comics", comicsCategory.optString("name"));

        // Check the root category
        categoryResource = resource().path("/category/" + rootCategoryId);
        categoryResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        response = categoryResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(10, articles.length());

        // Move the korben.info subscription to "techno"
        subscriptionResource = resource().path("/subscription/" + subscription1Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("category", category1Id);
        response = subscriptionResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Subscribe to xkcd.com
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:2501/http/feeds/xkcd.xml");
        response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String subscription2Id = json.optString("id");
        Assert.assertNotNull(subscription2Id);
        
        // Move the xkcd.com subscription to "comics"
        subscriptionResource = resource().path("/subscription/" + subscription2Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("category", category2Id);
        response = subscriptionResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // List all subscriptions
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        categories = json.optJSONArray("categories");
        Assert.assertNotNull(categories);
        Assert.assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        categories = json.optJSONArray("categories");
        Assert.assertNotNull(categories);
        Assert.assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        categories = rootCategory.optJSONArray("categories");
        Assert.assertEquals(2, categories.length());
        technologyCategory = categories.optJSONObject(0);
        Assert.assertEquals("techno", technologyCategory.optString("name"));
        Assert.assertEquals(false, technologyCategory.optBoolean("folded"));
        JSONArray subscriptions = technologyCategory.optJSONArray("subscriptions");
        Assert.assertEquals(1, subscriptions.length());
        comicsCategory = categories.optJSONObject(1);
        Assert.assertEquals("comics", comicsCategory.optString("name"));
        Assert.assertEquals(false, comicsCategory.optBoolean("folded"));
        subscriptions = comicsCategory.optJSONArray("subscriptions");
        Assert.assertEquals(1, subscriptions.length());

        // Update a category
        categoryResource = resource().path("/category/" + category1Id);
        categoryResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("name", "technology");
        postParams.add("order", 1);
        postParams.add("folded", true);
        response = categoryResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Check category changes
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        int unreadCount = json.optInt("unread_count");
        Assert.assertTrue(unreadCount > 0);
        categories = json.optJSONArray("categories");
        Assert.assertNotNull(categories);
        Assert.assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        categories = rootCategory.optJSONArray("categories");
        Assert.assertEquals(2, categories.length());
        comicsCategory = categories.optJSONObject(0);
        Assert.assertEquals("comics", comicsCategory.optString("name"));
        Assert.assertEquals(false, comicsCategory.optBoolean("folded"));
        Integer comicsUnreadCount = comicsCategory.optInt("unread_count");
        Assert.assertTrue(comicsUnreadCount > 0);
        technologyCategory = categories.optJSONObject(1);
        Assert.assertEquals("technology", technologyCategory.optString("name"));
        Assert.assertEquals(true, technologyCategory.optBoolean("folded"));
        Assert.assertTrue(technologyCategory.optInt("unread_count") > 0);

        // Marks all articles in the technology category as read
        categoryResource = resource().path("/category/" + category1Id + "/read");
        categoryResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        response = categoryResource.post(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Check the category for unread articles
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(comicsUnreadCount.intValue(), json.optInt("unread_count"));
        categories = json.optJSONArray("categories");
        Assert.assertNotNull(categories);
        Assert.assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        categories = rootCategory.optJSONArray("categories");
        Assert.assertEquals(2, categories.length());
        comicsCategory = categories.optJSONObject(0);
        Assert.assertEquals("comics", comicsCategory.optString("name"));
        Assert.assertEquals(false, comicsCategory.optBoolean("folded"));
        Assert.assertEquals(comicsUnreadCount.intValue(), comicsCategory.optInt("unread_count"));
        technologyCategory = categories.optJSONObject(1);
        Assert.assertEquals("technology", technologyCategory.optString("name"));
        Assert.assertEquals(true, technologyCategory.optBoolean("folded"));
        Assert.assertEquals(0, technologyCategory.optInt("unread_count"));

        // Check the category for only unread articles
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("unread", true);
        response = subscriptionResource.queryParams(queryParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(comicsUnreadCount.intValue(), json.optInt("unread_count"));
        categories = json.optJSONArray("categories");
        Assert.assertNotNull(categories);
        Assert.assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        categories = rootCategory.optJSONArray("categories");
        Assert.assertEquals(1, categories.length());
        comicsCategory = categories.optJSONObject(0);
        Assert.assertEquals("comics", comicsCategory.optString("name"));
        Assert.assertEquals(false, comicsCategory.optBoolean("folded"));
        Assert.assertEquals(comicsUnreadCount.intValue(), comicsCategory.optInt("unread_count"));

        // Deletes a category
        categoryResource = resource().path("/category/" + category1Id);
        categoryResource.addFilter(new CookieAuthenticationFilter(category1AuthToken));
        response = categoryResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
    }
}