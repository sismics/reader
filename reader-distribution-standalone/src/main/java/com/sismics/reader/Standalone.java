package com.sismics.reader;

import java.text.MessageFormat;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Reader standalone launcher with an embedded Jetty.
 *
 * @author jtremeaux
 */
public class Standalone {
    /**
     * Default port.
     */
    private static final int DEFAULT_PORT = 4001;
    
    /**
     * Default context path.
     */
    private static final String DEFAULT_CONTEXT_PATH = "/";
    
    /**
     * Entry Point.
     * 
     * @param args Args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        
        // Is SSL activated
        String secure = System.getProperty("reader.secure");
        
        Connector connector = new SelectChannelConnector();
        if (Boolean.valueOf(secure)) {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(System.getProperty("reader.keystore_path"));
            sslContextFactory.setKeyStorePassword(System.getProperty("reader.keystore_password"));
            sslContextFactory.setKeyManagerPassword(System.getProperty("reader.keymanager_password"));
            connector = new SslSocketConnector(sslContextFactory);
        }
        
        // Set host
        String host = System.getProperty("reader.host");
        if (host != null && !host.trim().isEmpty()) {
            connector.setHost(host.trim());
        }
        server.addConnector(connector);

        // Set port
        int port = DEFAULT_PORT;
        String portString = null;
        try {
            portString = System.getProperty("reader.port");
            if (portString != null && !portString.trim().isEmpty()) {
                port = Integer.parseInt(portString);
            }
        } catch (NumberFormatException e) {
            System.err.println(MessageFormat.format("Error parsing port: {0}", portString));
        }
        connector.setPort(port);
        
        // Set the War
        WebAppContext webapp = new WebAppContext();
        webapp.setWar("sismicsreader.war");

        // Set the context path
        String contextPath = System.getProperty("reader.contextPath");
        if (contextPath != null && !contextPath.trim().isEmpty()) {
            webapp.setContextPath(contextPath.trim());
        } else {
            webapp.setContextPath(DEFAULT_CONTEXT_PATH);
        }

        // Start the server
        server.setHandler(webapp);
        server.start();
        server.join();
    }
}
