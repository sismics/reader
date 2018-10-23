package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Exhaustive test of the search resource.
 * 
 * @author bgamard
 */
public class TestSearchResource extends BaseJerseyTest {
    /**
     * Test of the search resource.
     */
    @Test
    public void testSearchResource() throws Exception {
        // Create user search1
        createUser("search1");
        login("search1");

        // Subscribe to Korben RSS feed
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();

        // Search "zelda": OK, one result
        GET("/search/searchtermzelda");
        assertIsOk();
        JSONObject json = getJsonResult();
        JSONArray articles = json.getJSONArray("articles");
        assertEquals(1, articles.length());
        assertSearchResult(articles, "Quand <span class=\"highlight\">searchtermZelda</span> prend les armes", 0);
        
        // Search "njloinzejrmklsjd": OK, no result
        GET("/search/njloinzejrmklsjd");
        assertIsOk();
        json = getJsonResult();
        articles = json.getJSONArray("articles");
        assertEquals(0, articles.length());
        
        // Search "wifi": OK, 2 results
        GET("/search/searchtermwifi");
        assertIsOk();
        json = getJsonResult();
        articles = json.getJSONArray("articles");
        assertEquals(2, articles.length());
        assertSearchResult(articles, "Récupérer les clés <span class=\"highlight\">searchtermwifi</span> sur un téléphone Android", 0);
        assertSearchResult(articles, "Partagez vos clés <span class=\"highlight\">searchtermWiFi</span> avec vos amis", 1);
        
        // Search "google keep": OK, 2 results
        GET("/search/searchtermgoogle%20searchtermkeep");
        assertIsOk();
        json = getJsonResult();
        articles = json.getJSONArray("articles");
        assertEquals(2, articles.length());
        assertSearchResult(articles, "<span class=\"highlight\">searchtermGoogle</span> <span class=\"highlight\">searchtermKeep</span>…eut pas vraiment en faire plus (pour le moment)", 0);
        assertSearchResult(articles, "Quand searchtermZelda prend les armes", 1);
        
        // Create user search2
        createUser("search2");
        login("search2");

        // Subscribe to Korben RSS feed again to force articles updating
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();

        // Check if nothing is broken by searching "google keep"
        GET("/search/searchtermgoogle%20searchtermkeep");
        assertIsOk();
        json = getJsonResult();
        articles = json.getJSONArray("articles");
        assertEquals(2, articles.length());
        
        // Create user search3
        createUser("search3");
        login("search3");
        
        // Search "njloinzejrmklsjd"
        GET("/search/njloinzejrmklsjd");
        assertIsOk();
        json = getJsonResult();
        articles = json.getJSONArray("articles");
        assertEquals(0, articles.length());
        
        // Search "zelda"
        GET("/search/searchtermzelda");
        assertIsOk();
        json = getJsonResult();
        articles = json.getJSONArray("articles");
        assertEquals(1, articles.length());
        assertSearchResult(articles, "Quand <span class=\"highlight\">searchtermZelda</span> prend les armes", 0);
        
        // Subscribe to Korben RSS feed (alternative URL)
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben2.xml"));
        assertIsOk();
        
        // Search "zelda"
        GET("/search/searchtermzelda");
        assertIsOk();
        json = getJsonResult();
        articles = json.getJSONArray("articles");
        assertEquals(1, articles.length());
        assertSearchResult(articles, "Quand <span class=\"highlight\">searchtermZelda</span> prend les armes", 0);
    }
    
    /**
     * Assert that an article exists with a specific title in the provided articles set.
     * 
     * @param articles Articles from search
     * @param title Expected title
     * @param index Index
     */
    private void assertSearchResult(JSONArray articles, String title, int index) throws JSONException {
		JSONObject article = articles.getJSONObject(index);
		if (article.getString("title").equals(title)) {
			return;
		}
    	Assert.fail("[" + title + "] not found in [" + article.getString("title") + "]");
    }
}