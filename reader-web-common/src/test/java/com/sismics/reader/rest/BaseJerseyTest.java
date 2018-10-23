package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.util.TransactionUtil;
import com.sismics.reader.rest.descriptor.JerseyTestWebAppDescriptorFactory;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.test.framework.JerseyTest;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.junit.After;
import org.junit.Before;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Base class of integration tests with Jersey.
 * 
 * @author jtremeaux
 */
public abstract class BaseJerseyTest extends JerseyTest {
    /**
     * Test email server.
     */
    protected Wiser wiser;
    
    /**
     * Test HTTP server.
     */
    protected HttpServer httpServer;

    /**
     * The response from the last request.
     */
    protected ClientResponse response;

    /**
     * The set of current cookies.
     */
    protected Map<String, String> cookies = new HashMap<String, String>();

    /**
     * Constructor of BaseJerseyTest.
     */
    public BaseJerseyTest() {
        super(JerseyTestWebAppDescriptorFactory.build());
    }
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        wiser = new Wiser();
        wiser.setPort(2500);
        wiser.start();

        startHttpServer();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        wiser.stop();
        httpServer.stop();
    }

    /**
     * Starts the HTTP server.
     *
     */
    private void startHttpServer() throws Exception {
        String httpRoot = URLDecoder.decode(new File(getClass().getResource("/").getFile()).getAbsolutePath(), "utf-8");
        httpServer = HttpServer.createSimpleServer(httpRoot, "localhost", 9997);
        NetworkListener listener = httpServer.getListeners().iterator().next();
//        listener.setFilterChain(new DefaultFilterChain());

        // Disable file cache to fix https://java.net/jira/browse/GRIZZLY-1350
        ((StaticHttpHandler) httpServer.getServerConfiguration().getHttpHandlers().keySet().iterator().next()).setFileCacheEnabled(false);

        // Add a handler for temporary files
        addTempFileHandler();

        httpServer.start();
    }

    /**
     * Add a handler for temporaty files.
     *
     * Server Java temporary files on /temp
     */
    private void addTempFileHandler() {
        StaticHttpHandler staticHttpHandler = new StaticHttpHandler(System.getProperty("java.io.tmpdir"));
        staticHttpHandler.setFileCacheEnabled(false);
        httpServer.getServerConfiguration().addHttpHandler(staticHttpHandler, "/temp");
    }

    /**
     * Extracts an email from the queue and consumes the email.
     * 
     * @return Text of the email
     */
    protected String popEmail() throws MessagingException, IOException {
        List<WiserMessage> wiserMessageList = wiser.getMessages();
        if (wiserMessageList.isEmpty()) {
            return null;
        }
        WiserMessage wiserMessage = wiserMessageList.get(wiserMessageList.size() - 1);
        wiserMessageList.remove(wiserMessageList.size() - 1);
        MimeMessage message = wiserMessage.getMimeMessage();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        message.writeTo(os);

        return os.toString();
    }
    
    /**
     * Encodes a string to "quoted-printable" characters to compare with the contents of an email.
     * 
     * @param input String to encode
     * @return Encoded string
     */
    protected String encodeQuotedPrintable(String input) throws MessagingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = MimeUtility.encode(baos, "quoted-printable");
        os.write(input.getBytes());
        os.close();
        return baos.toString();
    }

    protected void copyTempResource(String file) {
//        httpServer.stop();
        File temp = new File(System.getProperty("java.io.tmpdir") + "/temp.xml");
        try {
            Files.copy(new File(getClass().getResource(file).getFile()), temp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        try {
//            httpServer.start();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * Simulates a network down situation, e.g. someone having installed the application on his laptop
     * and currently not having connection.
     *
     * @param runnable The code to run while the network is down
     */
    protected void withNetworkDown(Runnable runnable) throws Exception {
        try {
            httpServer.stop();

            runnable.run();
        } finally {
            startHttpServer();
        }
    }

    /**
     * Creates a user.
     *
     * @param username Username
     */
    public void createUser(String username) {
        // Login admin to create the user
        login("admin", "admin", false);

        // Create the user
        PUT("/user", ImmutableMap.of(
                "username", username,
                "email", username + "@reader.com",
                "password", "12345678",
                "time_zone", "Asia/Tokyo"
        ));
        assertIsOk();

        // Logout admin
        logout();
    }

    /**
     * Connects a user to the application.
     *
     * @param username Username
     * @param password Password
     * @param remember Remember user
     * @return Authentication token
     */
    public String login(String username, String password, Boolean remember) {
        POST("/user/login", ImmutableMap.of(
                "username", username,
                "password", password,
                "remember", remember.toString()
        ));
        assertIsOk();
        assertEquals(ClientResponse.Status.OK, ClientResponse.Status.fromStatusCode(response.getStatus()));

        return getAuthenticationCookie(response);
    }

    public void assertIsOk() {
        assertIsOk(response);
    }

    public void assertIsOk(ClientResponse response) {
        assertStatus(200, response);
    }

    public void assertIsBadRequest() {
        assertIsBadRequest(response);
    }

    public void assertIsBadRequest(ClientResponse response) {
        assertStatus(400, response);
    }

    public void assertIsForbidden() {
        assertIsForbidden(response);
    }

    public void assertIsForbidden(ClientResponse response) {
        assertStatus(403, response);
    }

    public void assertStatus(int status, ClientResponse response) {
        assertEquals("Response status error, out: " + response.toString(), status, response.getStatus());
    }

    /**
     * Connects a user to the application.
     *
     * @param username Username
     * @return Authentication token
     */
    public String login(String username) {
        return login(username, "12345678", false);
    }

    /**
     * Disconnects a user from the application.
     *
     */
    public void logout() {
        POST("/user/logout");
        assertIsOk();
    }

    /**
     * Extracts the authentication token of the response.
     *
     * @param response Response
     * @return Authentication token
     */
    public String getAuthenticationCookie(ClientResponse response) {
        String authToken = null;
        for (NewCookie cookie : response.getCookies()) {
            if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                authToken = cookie.getValue();
            }
        }
        return authToken;
    }

    /**
     * Force synchronization of all feeds.
     */
    public void synchronizeAllFeed() {
        TransactionUtil.handle(AppContext.getInstance().getFeedService()::synchronizeAllFeeds);
    }

    protected void GET(String url, Map<String, String> queryParams) {
        WebResource resource = resource().path(url);
        MultivaluedMapImpl params = new MultivaluedMapImpl();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            params.add(entry.getKey(), entry.getValue());
        }
        response = builder(resource.queryParams(params)).get(ClientResponse.class);
    }

    protected void GET(String resource) {
        GET(resource, new HashMap<String, String>());
        addCookiesFromResponse();
    }

    protected void PUT(String url, Map<String, String> putParams) {
        WebResource resource = resource().path(url);
        MultivaluedMapImpl params = new MultivaluedMapImpl();
        for (Map.Entry<String, String> entry : putParams.entrySet()) {
            params.add(entry.getKey(), entry.getValue());
        }
        response = builder(resource).put(ClientResponse.class, params);
        addCookiesFromResponse();
    }

    protected void PUT(String url, FormDataMultiPart form) {
        WebResource resource = resource().path(url);
        response = builder(resource).type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
        addCookiesFromResponse();
    }

    protected void PUT(String url) {
        PUT(url, new HashMap<String, String>());
    }

    protected void POST(String url, Map<String, String> postParams) {
        WebResource resource = resource().path(url);
        MultivaluedMapImpl params = new MultivaluedMapImpl();
        for (Map.Entry<String, String> entry : postParams.entrySet()) {
            params.add(entry.getKey(), entry.getValue());
        }
        response = builder(resource).post(ClientResponse.class, params);
        addCookiesFromResponse();
    }

    protected void POST(String url, Multimap<String, String> postParams) {
        WebResource resource = resource().path(url);
        MultivaluedMapImpl params = new MultivaluedMapImpl();
        for (Map.Entry<String, String> entry : postParams.entries()) {
            params.add(entry.getKey(), entry.getValue());
        }
        response = builder(resource).post(ClientResponse.class, params);
        addCookiesFromResponse();
    }

    protected void POST(String url) {
        POST(url, new HashMap<String, String>());
    }

    protected void DELETE(String url) {
        WebResource resource = resource().path(url);
        response = builder(resource).delete(ClientResponse.class);
        addCookiesFromResponse();
    }

    protected WebResource.Builder builder(WebResource resource) {
        WebResource.Builder builder = resource.getRequestBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            builder.cookie(new Cookie(entry.getKey(), entry.getValue()));
        }
        return builder;
    }

    private void addCookiesFromResponse() {
        for (Cookie cookie : response.getCookies()) {
            if (cookie.getName().equals(TokenBasedSecurityFilter.COOKIE_NAME)) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
        }
    }

    protected JSONObject getJsonResult() {
        return response.getEntity(JSONObject.class);
    }
}
