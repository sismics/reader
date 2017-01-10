package com.sismics.reader.core.dao.file.html;

import com.sismics.util.UrlUtil;
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
import java.util.ArrayList;
import java.util.List;

/**
 * HTML parser used to look for RSS / Atom feeds.
 *
 * @author jtremeaux
 */
public class RssExtractor extends DefaultHandler {
    private static final Logger log = LoggerFactory.getLogger(RssExtractor.class);

    /**
     * Original page URL.
     */
    private final URL url;

    /**
     * List of extracted feed URLs.
     */
    private List<String> feedList;
    
    /**
     * Constructor of RssExtractor.
     * 
     * @param url Url of the html page
     */
    public RssExtractor(String url) throws MalformedURLException {
        this.url = new URL(url);
        feedList = new ArrayList<String>();
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
            String type = StringUtils.trim(attributes.getValue("type"));
            String href = StringUtils.trim(attributes.getValue("href"));
            
            if ("alternate".equalsIgnoreCase(rel)) {
                try {
                    if ("application/rss+xml".equalsIgnoreCase(type)) {
                        feedList.add(UrlUtil.completeUrl(url.toString(), href));
                    } else if ("application/atom+xml".equalsIgnoreCase(type)) {
                        feedList.add(UrlUtil.completeUrl(url.toString(), href));
                    }
                } catch (MalformedURLException e) {
                    log.error(MessageFormat.format("Error parsing URL, extracted href: {0}", href), e);
                }
            }
            return;
        }
    }
    
    /**
     * Getter of feedList.
     *
     * @return feedList
     */
    public List<String> getFeedList() {
        return feedList;
    }
}
