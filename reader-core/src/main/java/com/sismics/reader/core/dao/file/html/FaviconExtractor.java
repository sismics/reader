package com.sismics.reader.core.dao.file.html;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * HTML parser used to look for a favicon.
 *
 * @author jtremeaux
 */
public class FaviconExtractor extends DefaultHandler {
    private static final Logger log = LoggerFactory.getLogger(FaviconExtractor.class);

    /**
     * Original page URL.
     */
    private final URL url;

    private String favicon;
    
    /**
     * Constructor of FaviconExtractor.
     * 
     * @param url Url of the html page
     * @throws MalformedURLException
     */
    public FaviconExtractor(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    /**
     * Reads an HTML page and extracts RSS / Atom feeds.
     * 
     * @throws Exception
     */
    public void readPage() throws Exception {
        InputStream in = read();
        SAXParserImpl parser = SAXParserImpl.newInstance(null);
        parser.setFeature("http://xml.org/sax/features/namespaces", true);    
        parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        parser.parse(in, this);
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("link".equalsIgnoreCase(localName)) {
            String rel = StringUtils.trim(attributes.getValue("rel"));
            String href = StringUtils.trim(attributes.getValue("href"));
            
            if ("shortcut icon".equalsIgnoreCase(rel) || "icon".equalsIgnoreCase(rel)) {
                if (href.startsWith("http")) {
                    favicon = href;
                } else {
                    if (!href.startsWith("/")) {
                        href = "/" + href;
                    }
                    try {
                        favicon = new URL(url.getProtocol(), url.getHost(), url.getPort(), href).toString();
                    } catch (MalformedURLException e) {
                        log.error(MessageFormat.format("Error building absolute url for favicon {0} from page {1}", href, url.toString()), e);
                    }
                }
            }
            return;
        }
    }
    
    private InputStream read() {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Getter of favicon.
     *
     * @return favicon
     */
    public String getFavicon() {
        return favicon;
    }
}
