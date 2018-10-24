package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import com.sismics.util.filter.HeaderBasedSecurityFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Test of the security layer.
 * 
 * @author jtremeaux
 * @author pacien
 */
public class TestSecurity extends BaseJerseyTest {
    /**
     * Test of the security layer.
     * 
     */
    @Test
    public void testSecurity() throws JSONException {
        // Create a user
        createUser("testsecurity");

        // Changes a user's email KO : the user is not connected
        POST("/user/update", ImmutableMap.of("email", "testsecurity2@reader.com"));
        assertIsForbidden();
        JSONObject json = getJsonResult();
        assertEquals("ForbiddenError", json.getString("type"));
        assertEquals("You don't have access to this resource", json.getString("message"));

        // User testsecurity logs in
        login("testsecurity");

        // User testsecurity creates a new user KO : no permission
        PUT("/user");
        assertIsForbidden();
        assertEquals("ForbiddenError", json.getString("type"));
        assertEquals("You don't have access to this resource", json.getString("message"));

        // User testsecurity changes his email OK
        POST("/user", ImmutableMap.of(
                "email", "testsecurity2@reader.com",
                "locale", "en"
        ));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));

        // User testsecurity logs out
        POST("/user/logout");
        assertIsOk();
        String authToken = getAuthenticationCookie(response);
        assertTrue(StringUtils.isEmpty(authToken));

        // User testsecurity logs out KO : he is not connected anymore
        POST("/user/logout");
        assertIsForbidden();

        // User testsecurity logs in with a long lived session
        login("testsecurity", "12345678", true);

        // User testsecurity logs out
        logout();
    }

    @Test
    public void testHeaderBasedAuthentication() {
        final String userName = "header_auth_test";
        final WebResource resource = resource().path("/user");
        createUser(userName);

        assertEquals(Status.FORBIDDEN.getStatusCode(), resource
                .post(ClientResponse.class)
                .getStatus());

        assertEquals(Status.OK.getStatusCode(), resource
                .header(HeaderBasedSecurityFilter.AUTHENTICATED_USER_HEADER, userName)
                .post(ClientResponse.class)
                .getStatus());

        assertEquals(Status.FORBIDDEN.getStatusCode(), resource
                .header(HeaderBasedSecurityFilter.AUTHENTICATED_USER_HEADER, "erroneous_" + userName)
                .post(ClientResponse.class)
                .getStatus());
    }
}
