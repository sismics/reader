package com.sismics.util.cert;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * A trust manager that trusts all certificates (unsecure).
 *
 * @author jtremeaux
 */
public class TrustAllManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        // NOP
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) {
        // NOP
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
            return null;
    }
}
