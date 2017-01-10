package com.sismics.reader.core.dao.file.html;

import org.apache.commons.lang.StringUtils;
import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

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

    /**
     * Extracted favicon URL.
     */
    private String favicon;
    
    /**
     * Constructor of FaviconExtractor.
     * 
     * @param url Url of the html page
     */
    public FaviconExtractor(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    /**
     * Reads an HTML page and extracts RSS / Atom feeds.
     * 
     * @param is Input stream
     */
    public void readPage(InputStream is) throws Exception {
        SAXParserImpl parser = SAXParserImpl.newInstance(null);
        parser.setFeature("http://xml.org/sax/features/namespaces", true);    
        parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    
        parser.parse(is, this);
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
                        // TODO Build relative links relative to base url
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
    
    /**
     * Getter of favicon.
     *
     * @return favicon
     */
    public String getFavicon() {
        return favicon;
    }
}
