package com.sismics.reader.rest;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Exhaustive test of the subscription resource.
 * 
 * @author jtremeaux
 */
public class TestSubscriptionResource extends BaseJerseyTest {
    /**
     * Test of the subscription resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testSubscriptionAddResource() throws JSONException {
        // Create user subscription1
        clientUtil.createUser("subscription1");
        String subscription1AuthToken = clientUtil.login("subscription1");

        // Create a category
        WebResource categoryResource = resource().path("/category");
        categoryResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("name", "techno");
        ClientResponse response = categoryResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String category1Id = json.optString("id");
        Assert.assertNotNull(category1Id);
        
        // Subscribe to korben.info
        WebResource subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:9997/http/feeds/korben.xml");
        response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String subscription1Id = json.getString("id");
        Assert.assertNotNull(subscription1Id);
        
        // Move the korben.info subscription to "techno"
        subscriptionResource = resource().path("/subscription/" + subscription1Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("category", category1Id);
        response = subscriptionResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // List all subscriptions
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        int unreadCount = json.optInt("unread_count");
        Assert.assertTrue(unreadCount > 0);
        JSONArray categories = json.optJSONArray("categories");
        Assert.assertNotNull(categories);
        Assert.assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        categories = rootCategory.getJSONArray("categories");
        JSONObject technoCategory = categories.optJSONObject(0);
        JSONArray subscriptions = technoCategory.optJSONArray("subscriptions");
        Assert.assertEquals(1, subscriptions.length());
        JSONObject subscription = subscriptions.getJSONObject(0);
        Assert.assertEquals(10, subscription.getInt("unread_count"));
        Assert.assertEquals("http://localhost:9997/http/feeds/korben.xml", subscription.getString("url"));

        // Check the subscription data
        subscriptionResource = resource().path("/subscription/" + subscription1Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        subscription = json.optJSONObject("subscription");
        Assert.assertNotNull(subscription);
        Assert.assertEquals("Korben", subscription.optString("title"));
        Assert.assertEquals("http://korben.info", subscription.optString("url"));
        Assert.assertEquals("Upgrade your mind", subscription.optString("description"));
        JSONArray articles = json.optJSONArray("articles");
        Assert.assertEquals(10, articles.length());
        JSONObject article = articles.optJSONObject(0);
        Assert.assertNotNull(article);
        String article0Id = article.getString("id");
        Assert.assertNotNull(article0Id);
        JSONObject articleSubscription = article.optJSONObject("subscription");
        Assert.assertNotNull(articleSubscription.getString("id"));
        Assert.assertNotNull(articleSubscription.getString("title"));
        Assert.assertNotNull(article.optString("comment_url"));
        article = (JSONObject) articles.get(1);
        String article1Id = article.getString("id");
        article = (JSONObject) articles.get(2);
        String article2Id = article.getString("id");

        // Check pagination
        categoryResource = resource().path("/subscription/" + subscription1Id);
        categoryResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("after_article", article1Id);
        response = categoryResource.queryParams(queryParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(8, articles.length());
        Assert.assertEquals(article2Id, article.getString("id"));

        // Update the subscription
        subscriptionResource = resource().path("/subscription/" + subscription1Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("order", 1);
        postParams.add("title", "Korben.info");
        response = subscriptionResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Check the updated subscription data
        subscriptionResource = resource().path("/subscription/" + subscription1Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        subscription = json.optJSONObject("subscription");
        Assert.assertNotNull(subscription);
        Assert.assertEquals("Korben.info", subscription.optString("title"));
        Assert.assertEquals("http://korben.info", subscription.optString("url"));
        Assert.assertEquals("Upgrade your mind", subscription.optString("description"));

        // Marks an article as read
        WebResource articleResource = resource().path("/article/" + article0Id + "/read");
        articleResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        postParams = new MultivaluedMapImpl();
        response = articleResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check the subscription data
        subscriptionResource = resource().path("/subscription/" + subscription1Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        queryParams = new MultivaluedMapImpl();
        queryParams.add("unread", "true");
        response = subscriptionResource.queryParams(queryParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(9, articles.length());

        // Check the subscription data
        subscriptionResource = resource().path("/subscription/" + subscription1Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        queryParams = new MultivaluedMapImpl();
        queryParams.add("unread", "false");
        response = subscriptionResource.queryParams(queryParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(10, articles.length());

        // Check all subscriptions for unread articles
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(9, json.optInt("unread_count"));

        // Marks an article as unread
        articleResource = resource().path("/article/" + article0Id + "/unread");
        articleResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        postParams = new MultivaluedMapImpl();
        response = articleResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check the subscription data
        subscriptionResource = resource().path("/subscription/" + subscription1Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        queryParams = new MultivaluedMapImpl();
        queryParams.add("unread", "true");
        response = subscriptionResource.queryParams(queryParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(10, articles.length());

        // Marks all articles in this subscription as read
        subscriptionResource = resource().path("/subscription/" + subscription1Id + "/read");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = subscriptionResource.post(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Check all subscriptions for unread articles
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(0, json.optInt("unread_count"));

        // Delete the subscription
        subscriptionResource = resource().path("/subscription/" + subscription1Id);
        subscriptionResource.addFilter(new CookieAuthenticationFilter(subscription1AuthToken));
        response = subscriptionResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
    }

    /**
     * Test of the import resource.
     * 
     * @throws Exception
     */
    @Test
    public void testSubscriptionImportOpml() throws Exception {
        // Create user import_opml1
        clientUtil.createUser("import_opml1");
        String importOpml1AuthToken = clientUtil.login("import_opml1");

        // Import an OPML file
        WebResource trackResource = resource().path("/subscription/import");
        trackResource.addFilter(new CookieAuthenticationFilter(importOpml1AuthToken));
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream track = this.getClass().getResourceAsStream("/import/greader_subscriptions.xml");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(track),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        ClientResponse response = trackResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // List all subscriptions
        AppContext.getInstance().waitForAsync();
        WebResource subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(importOpml1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        int unreadCount = json.optInt("unread_count");
        Assert.assertTrue(unreadCount > 0);
        JSONArray categories = json.optJSONArray("categories");
        Assert.assertNotNull(categories);
        Assert.assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        categories = rootCategory.getJSONArray("categories");
        Assert.assertEquals(2, categories.length());
        JSONObject comicsCategory = categories.optJSONObject(0);
        Assert.assertEquals("Comics / Sub", comicsCategory.getString("name"));
        JSONArray subscriptions = comicsCategory.optJSONArray("subscriptions");
        Assert.assertEquals(2, subscriptions.length());
        
        // Export all subscriptions
        AppContext.getInstance().waitForAsync();
        subscriptionResource = resource().path("/subscription/export");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(importOpml1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        String text = CharStreams.toString(new InputStreamReader(response.getEntityInputStream(), Charsets.UTF_8));
        Assert.assertTrue(text.contains("Comics / Sub"));
        Assert.assertTrue(text.contains("Good Math, Bad Math"));
    }

    /**
     * Test of the import resource.
     * 
     * @throws Exception
     */
    @Test
    public void testSubscriptionImportTakeout() throws Exception {
        // Create user import_takeout1
        clientUtil.createUser("import_takeout1");
        String importTakeout1AuthToken = clientUtil.login("import_takeout1");

        // Import a Takeout file
        WebResource trackResource = resource().path("/subscription/import");
        trackResource.addFilter(new CookieAuthenticationFilter(importTakeout1AuthToken));
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream track = this.getClass().getResourceAsStream("/import/test@gmail.com-takeout.zip");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(track),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        ClientResponse response = trackResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // List all subscriptions
        AppContext.getInstance().waitForAsync();
        WebResource subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(importTakeout1AuthToken));
        response = subscriptionResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        int unreadCount = json.optInt("unread_count");
        Assert.assertTrue(unreadCount > 0);
        JSONArray categories = json.optJSONArray("categories");
        Assert.assertNotNull(categories);
        Assert.assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        categories = rootCategory.getJSONArray("categories");
        Assert.assertEquals(2, categories.length());
        JSONObject comicsCategory = categories.optJSONObject(0);
        Assert.assertEquals("Blogs", comicsCategory.getString("name"));
        JSONArray subscriptions = comicsCategory.optJSONArray("subscriptions");
        Assert.assertEquals(1, subscriptions.length());
        
        // Check the starred resource
        WebResource starredResource = resource().path("/starred");
        starredResource.addFilter(new CookieAuthenticationFilter(importTakeout1AuthToken));
        response = starredResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray articles = json.optJSONArray("articles");
        Assert.assertNotNull(articles);
        Assert.assertEquals(3, articles.length());
    }
}