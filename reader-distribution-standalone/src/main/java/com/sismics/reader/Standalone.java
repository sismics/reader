package com.sismics.reader;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class Standalone {
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(8080);
        server.addConnector(connector);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar("sismicsreader.jar");
        server.setHandler(webapp);

        server.start();
        server.join();
    }
}
