package com.sismics.util.cert;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Utility class for managing certificates.
 *
 * @author jtremeaux
 */
public class CertUtil {
    /**
     * Test a the specified host for ssl connection.
     *
     * @param hostname The host to test
     * @param port The port
     */
    public static void testSsl(String hostname, Integer port) {
        InputStream in = null;
        OutputStream out = null;
        SocketFactory sockerFactory = SSLSocketFactory.getDefault();
        try {
            Socket socket = sockerFactory.createSocket(hostname, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            out.write(42);
            while (in.available() > 0) {
                in.read();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // NOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // NOP
                }
            }
        }
    }

    /**
     * Returns an SSLSocketFactory that trusts all connections.
     *
     * @return The SSLSocketFactory
     */
    public static SSLSocketFactory getTrustAllSocketFactory() {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = {new TrustAllManager()};
            sc.init(null, trustManagers, null);
            return sc.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Error creating TrustAllSocketFactory", e);
        }
    }
}
