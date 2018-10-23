package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Exhaustive test of the user resource.
 * 
 * @author jtremeaux
 */
public class TestUserResource extends BaseJerseyTest {
    /**
     * Test the user resource as anonymous.
     * 
     */
    @Test
    public void testUserResourceAnonymous() throws JSONException {
        // Check anonymous user information
        GET("/user");
        assertIsOk();
        JSONObject json = getJsonResult();
        assertTrue(json.getBoolean("is_default_password"));
    }
        
    /**
     * Test the user resource.
     *
     */
    @Test
    public void testUserResource() throws JSONException {
        // Create alice user
        createUser("alice");

        // Login admin
        login("admin", "admin", false);
        
        // List all users
        GET("/user/list", ImmutableMap.of(
                "sort_column", "2",
                "asc", "false"));
        JSONObject json = getJsonResult();
        assertIsOk();
        JSONArray users = json.getJSONArray("users");
        assertTrue(users.length() > 0);
        JSONObject user = users.getJSONObject(0);
        assertNotNull(user.optString("id"));
        
        // Create a user KO (login length validation)
        PUT("/user", ImmutableMap.of(
                "username", "   bb  ",
                "email", "bob@reader.com",
                "password", "12345678"));
        assertIsBadRequest();
        json = getJsonResult();
        assertEquals("ValidationError", json.getString("type"));
        assertTrue(json.getString("message"), json.getString("message").contains("more than 3"));

        // Create a user KO (login format validation)
        PUT("/user", ImmutableMap.of(
                "username", "bob-",
                "email", " bob@reader.com ",
                "password", "12345678"));
        assertIsBadRequest();
        json = getJsonResult();
        assertEquals("ValidationError", json.getString("type"));
        assertTrue(json.getString("message"), json.getString("message").contains("alphanumeric"));

        // Create a user KO (email format validation)
        PUT("/user", ImmutableMap.of(
                "username", "bob",
                "email", " bobreader.com ",
                "password", "12345678"));
        assertIsBadRequest();
        json = getJsonResult();
        assertEquals("ValidationError", json.getString("type"));
        assertTrue(json.getString("message"), json.getString("message").contains("must be an email"));

        // Create a user bob OK
        PUT("/user", ImmutableMap.of(
                "username", " bob ",
                "email", " bob@reader.com ",
                "password", " 12345678 ",
                "locale", "ko"));
        assertIsOk();

        // Create a user bob KO : duplicate username
        PUT("/user", ImmutableMap.of(
                "username", " bob ",
                "email", " bob@reader.com ",
                "password", " 12345678 ",
                "locale", "ko"));
        assertStatus(500, response); //FIXME should be 400
        json = getJsonResult();
        assertEquals("AlreadyExistingUsername", json.getString("type"));

        // Check if a username is free: OK
        GET("/user/check_username", ImmutableMap.of("username", "carol"));
        assertIsOk();

        // Check if a username is free : KO
        GET("/user/check_username", ImmutableMap.of("username", "alice"));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ko", json.getString("status"));

        // Login alice with extra whitespaces: OK
        POST("/user/login", ImmutableMap.of(
                "username", " alice ",
                "password", " 12345678 "
        ));
        assertIsOk();

        // Check alice user information
        login("alice");
        GET("/user");
        assertIsOk();
        json = getJsonResult();
        assertEquals("alice@reader.com", json.getString("email"));
        assertEquals("default.less", json.getString("theme"));
        assertFalse(json.getBoolean("display_title_web"));
        assertTrue(json.getBoolean("display_title_mobile"));
        assertTrue(json.getBoolean("display_unread_web"));
        assertTrue(json.getBoolean("display_unread_mobile"));
        assertFalse(json.getBoolean("narrow_article"));
        assertFalse(json.getBoolean("first_connection"));
        assertFalse(json.getBoolean("is_default_password"));
        
        // Check bob user information
        login("bob");
        GET("/user");
        assertIsOk();
        json = getJsonResult();
        assertEquals("bob@reader.com", json.getString("email"));
        assertEquals("ko", json.getString("locale"));
        
        // Test login KO (user not found)
        logout();
        POST("/user/login", ImmutableMap.of(
                "username", "intruder",
                "password", "12345678"));
        assertIsForbidden();

        // Test login KO (wrong password)
        POST("/user/login", ImmutableMap.of(
                "username", "alice",
                "password", "error"));
        assertIsForbidden();

        // User alice updates her information + changes her email
        login("alice");
        POST("/user", ImmutableMap.<String, String>builder()
                .put("email", " alice2@reader.com ")
                .put("theme", " highcontrast ")
                .put("locale", " en ")
                .put("display_title_web", "true")
                .put("display_title_mobile", "false")
                .put("display_unread_web", "false")
                .put("display_unread_mobile", "false")
                .put("narrow_article", "true")
                .build());
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // Check the update
        GET("/user");
        assertIsOk();
        json = getJsonResult();
        assertEquals("highcontrast", json.getString("theme"));
        assertTrue(json.getBoolean("display_title_web"));
        assertFalse(json.getBoolean("display_title_mobile"));
        assertFalse(json.getBoolean("display_unread_web"));
        assertFalse(json.getBoolean("display_unread_mobile"));
        assertTrue(json.getBoolean("narrow_article"));
        
        // Delete user alice
        DELETE("/user");
        assertIsOk();
        
        // Check the deletion
        POST("/user/login", ImmutableMap.of(
                "username", "alice",
                "password", "12345678"));
        assertIsForbidden();
    }

    /**
     * Test the user resource admin functions.
     * 
     */
    @Test
    public void testUserResourceAdmin() throws JSONException {
        // Create admin_user1 user
        createUser("admin_user1");

        // Login admin
        login("admin", "admin", false);

        // Check admin information
        GET("/user");
        assertIsOk();
        JSONObject json = getJsonResult();
        assertTrue(json.getBoolean("first_connection"));
        assertTrue(json.getBoolean("is_default_password"));

        // User admin updates his information
        POST("/user", ImmutableMap.of("first_connection", "false"));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));

        // Check admin information update
        GET("/user");
        assertIsOk();
        json = getJsonResult();
        assertFalse(json.getBoolean("first_connection"));

        // User admin update admin_user1 information
        POST("/user", ImmutableMap.<String, String>builder()
                .put("email", " alice2@reader.com ")
                .put("theme", " highcontrast ")
                .put("locale", " en ")
                .put("display_title_web", "true")
                .put("display_title_mobile", "false")
                .put("display_unread_web", "false")
                .put("display_unread_mobile", "false")
                .put("narrow_article", "true")
                .build());
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // User admin deletes himself: forbidden
        DELETE("/user");
        assertIsBadRequest();
        json = getJsonResult();
        assertEquals("ForbiddenError", json.getString("type"));

        // User admin deletes himself: forbidden
        DELETE("/user/admin");
        assertIsBadRequest();
        json = getJsonResult();
        assertEquals("ForbiddenError", json.getString("type"));

        // User admin deletes user admin_user1
        DELETE("/user/admin_user1");
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        
        // User admin deletes user admin_user1 : KO (user doesn't exist)
        DELETE("/user/admin_user1");
        assertIsBadRequest();
        json = getJsonResult();
        assertEquals("UserNotFound", json.getString("type"));
    }
}