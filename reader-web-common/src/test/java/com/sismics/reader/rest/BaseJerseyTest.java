package com.sismics.reader.rest;

import com.sismics.reader.rest.descriptor.JerseyTestWebAppDescriptorFactory;
import com.sismics.reader.rest.util.ClientUtil;
import com.sun.jersey.test.framework.JerseyTest;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.junit.After;
import org.junit.Before;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.Callable;

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
     * Utility class for the REST client.
     */
    protected ClientUtil clientUtil;
    
    /**
     * Constructor of BaseJerseyTest.
     */
    public BaseJerseyTest() {
        super(JerseyTestWebAppDescriptorFactory.build());
        this.clientUtil = new ClientUtil(resource());
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
     * @throws Exception
     */
    private void startHttpServer() throws Exception {
        String httpRoot = URLDecoder.decode(new File(getClass().getResource("/").getFile()).getAbsolutePath(), "utf-8");
        httpServer = HttpServer.createSimpleServer(httpRoot, "localhost", 9997);
        // Disable file cache to fix https://java.net/jira/browse/GRIZZLY-1350
        ((StaticHttpHandler) httpServer.getServerConfiguration().getHttpHandlers().keySet().iterator().next()).setFileCacheEnabled(false);
        httpServer.start();
    }

    /**
     * Extracts an email from the queue and consumes the email.
     * 
     * @return Text of the email
     * @throws MessagingException
     * @throws IOException
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
        String body = os.toString();
        
        return body;
    }
    
    /**
     * Encodes a string to "quoted-printable" characters to compare with the contents of an email.
     * 
     * @param input String to encode
     * @return Encoded string
     * @throws MessagingException
     * @throws IOException
     */
    protected String encodeQuotedPrintable(String input) throws MessagingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = MimeUtility.encode(baos, "quoted-printable");
        os.write(input.getBytes());
        os.close();
        return baos.toString();
    }

    /**
     * Simulates a network down situation, e.g. someone having installed the application on his laptop
     * and currently not having connection.
     *
     * @param callable The code to run while the network is down
     * @return The result
     * @throws Exception
     */
    protected <T> T withNetworkDown(Callable<T> callable) throws Exception {
        try {
            httpServer.stop();

            return callable.call();
        } finally {
            startHttpServer();
        }
    }
}
