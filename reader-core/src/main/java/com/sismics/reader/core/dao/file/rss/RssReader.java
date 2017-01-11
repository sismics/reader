package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.util.StreamUtil;
import com.sismics.util.DateUtil;
import com.sismics.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.*;

/**
 * RSS / Atom feed parser.
 *
 * @author jtremeaux
 */
public class RssReader extends DefaultHandler {
    private static final Logger log = LoggerFactory.getLogger(RssReader.class);

    /**
     * A list of common date formats used in RSS feeds.
     */
    public static final DateTimeFormatter DF_RSS = new DateTimeFormatterBuilder()
            .append(null, new DateTimeParser[] {
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm zzz").getParser(),
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").getParser(),
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz").getParser(),
                    DateTimeFormat.forPattern("EEE,  d MMM yyyy HH:mm:ss zzz").getParser(),
                    DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss Z").getParser(),
                    DateTimeFormat.forPattern("yyyy-mm-dd HH:mm:ss").getParser(),
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss").getParser(),
                    DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss zzz").getParser(),
                    DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z Z").getParser()
                }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);
    
    /**
     * A list of common date formats used in Atom feeds.
     */
    public static final DateTimeFormatter DF_ATOM = new DateTimeFormatterBuilder()
            .append(null, new DateTimeParser[] {
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getParser()
                }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

    /**
     * A list of common date formats used in Dublin Core.
     */
    public static final DateTimeFormatter DF_DC = new DateTimeFormatterBuilder()
            .append(null, new DateTimeParser[] {
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getParser()
                }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

    /**
     * Contents of the current element.
     */
    private String content;

    private Feed feed;
    
    private Article article;
    
    private List<Article> articleList;
    
    private List<AtomLink> atomLinkList;
    
    private List<AtomLink> atomArticleLinkList;

    private int fatalErrorCount;

    private final static String URI_XML = "http://www.w3.org/XML/1998/namespace";
    
    private final static String URI_ATOM = "http://www.w3.org/2005/Atom";
    
    private final static String URI_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    private final static String URI_SLASH = "http://purl.org/rss/1.0/modules/slash/";
    
    private final static String URI_DC = "http://purl.org/dc/elements/1.1/";
    
    private final static String URI_CONTENT = "http://purl.org/rss/1.0/modules/content/";
    
    private final static String URI_THREAD = "http://purl.org/syndication/thread/1.0";
    
    private enum FeedType {
        RSS,
        
        ATOM,
        
        RDF
    }
    
    private enum Element {
        UNKNOWN,
        
        // RSS elements
        RSS,
        
        RSS_CHANNEL,
        
        RSS_TITLE,
        
        RSS_LINK,
        
        RSS_DESCRIPTION,
       
        RSS_LANGUAGE,

        ITEM,

        ITEM_TITLE,

        ITEM_GUID,

        ITEM_LINK,

        ITEM_COMMENTS,

        ITEM_SLASH_COMMENTS,

        ITEM_DESCRIPTION,
        
        ITEM_DC_CREATOR,
        
        ITEM_DC_DATE,
        
        ITEM_PUB_DATE,
        
        ITEM_CONTENT_ENCODED,
        
        ITEM_ENCLOSURE,

        // ATOM elements
        FEED,

        ATOM_TITLE,

        ATOM_SUBTITLE,

        ATOM_LINK,

        ATOM_ID,

        ATOM_UPDATED,

        ENTRY,

        ENTRY_TITLE,

        ENTRY_LINK,

        ENTRY_UPDATED,

        ENTRY_ID,

        ENTRY_SUMMARY,
        
        ENTRY_CONTENT,
        
        ENTRY_AUTHOR,
        
        AUTHOR_NAME,

        // RDF elements
        RDF,

    }

    private static final int FATAL_ERROR_MAX = 100;
    
    private Element currentElement;

    private Stack<Element> elementStack;

    private FeedType feedType;
    
    /**
     * Constructor of RssReader.
     * 
     */
    public RssReader() {
        elementStack = new Stack<Element>();
    }

    /**
     * Reads an RSS / Atom feed into feed and articles.
     * 
     * @param is Input stream
     */
    public void readRssFeed(InputStream is) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
        SAXParser parser = factory.newSAXParser();

        // Pass a character stream to the parser for it to pick-up the correct encoding.
        // See http://stackoverflow.com/questions/3482494/
        Reader reader = new XmlReader(StreamUtil.detectGzip(is), "UTF-8");
        InputSource source = new InputSource(reader);
        
        try {
            parser.parse(source, this);
        } catch (InternalError e) {
            // Fix for Oracle code throwing java.lang.InternalError disgracefully
            throw new Exception(e);
        }
    
        if (feedType == FeedType.ATOM) {
            AtomUrlGuesserStrategy strategy = new AtomUrlGuesserStrategy();
            String siteUrl = strategy.guessSiteUrl(atomLinkList);
            feed.setUrl(siteUrl);

            if (feed.getBaseUri() == null) {
                String feedBaseUri = strategy.guessFeedUrl(atomLinkList);
                if (feedBaseUri != null) {
                    try {
                        feed.setBaseUri(UrlUtil.getBaseUri(feedBaseUri));
                    } catch (MalformedURLException e) {
                        // NOP
                    }
                }
            }
        }
        validateFeed();
        fixGuid();
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("rss".equalsIgnoreCase(localName)) {
            initFeed();
            pushElement(Element.RSS);
            feedType = FeedType.RSS;
            return;
        } else if ("feed".equalsIgnoreCase(localName)) {
            initFeed();
            pushElement(Element.FEED);
            feedType = FeedType.ATOM;

            String lang = StringUtils.trimToNull(attributes.getValue(URI_XML, "lang"));
            feed.setLanguage(lang);
            String xmlBase = StringUtils.trimToNull(attributes.getValue(URI_XML, "base"));
            feed.setBaseUri(xmlBase);
            return;
        } else if ("RDF".equalsIgnoreCase(localName)) {
            initFeed();
            pushElement(Element.RDF);
            feedType = FeedType.RDF;
            return;
        }
        if (feed == null) {
            throw new SAXException("Root element doesn't designate an RSS/Atom/RDF feed, encountered: " + localName);
        }
        
        if (((feedType == FeedType.RSS && currentElement == Element.RSS) || (feedType == FeedType.RDF && currentElement == Element.RDF)) &&
                "channel".equalsIgnoreCase(localName)) {
            pushElement(Element.RSS_CHANNEL);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.RSS_CHANNEL &&
                "title".equalsIgnoreCase(localName)) {
            pushElement(Element.RSS_TITLE);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.RSS_CHANNEL
                && "description".equalsIgnoreCase(localName)) {
            pushElement(Element.RSS_DESCRIPTION);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.RSS_CHANNEL
                && "link".equalsIgnoreCase(localName) && !URI_ATOM.equals(uri)) {
            pushElement(Element.RSS_LINK);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.RSS_CHANNEL &&
                "language".equals(localName)) {
            pushElement(Element.RSS_LANGUAGE);
        } else if (((feedType == FeedType.RSS && currentElement == Element.RSS_CHANNEL) || (feedType == FeedType.RDF && currentElement == Element.RDF)) &&
                "item".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM);
            article = new Article();
            articleList.add(article);
            
            if (feedType == FeedType.RDF) {
                String about = StringUtils.trim(attributes.getValue(URI_RDF, "about"));
                if (!StringUtils.isBlank(about)) {
                    article.setGuid(about);
                }
            }
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.ITEM &&
                "title".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_TITLE);
        } else if (feedType == FeedType.RSS && currentElement == Element.ITEM && "guid".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_GUID);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.ITEM &&
                "link".equalsIgnoreCase(localName) && !URI_ATOM.equals(uri)) {
            pushElement(Element.ITEM_LINK);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.ITEM &&
                "comments".equalsIgnoreCase(localName) && !URI_SLASH.equals(uri)) {
            pushElement(Element.ITEM_COMMENTS);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.ITEM &&
                "comments".equalsIgnoreCase(localName) && URI_SLASH.equalsIgnoreCase(uri)) {
            pushElement(Element.ITEM_SLASH_COMMENTS);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.ITEM &&
                "description".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_DESCRIPTION);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.ITEM &&
                "creator".equalsIgnoreCase(localName) && URI_DC.equalsIgnoreCase(uri)) {
            pushElement(Element.ITEM_DC_CREATOR);
        } else if (feedType == FeedType.RDF && currentElement == Element.ITEM &&
                "date".equalsIgnoreCase(localName) && URI_DC.equalsIgnoreCase(uri)) {
            pushElement(Element.ITEM_DC_DATE);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.ITEM &&
                "encoded".equalsIgnoreCase(localName) && URI_CONTENT.equalsIgnoreCase(uri)) {
            pushElement(Element.ITEM_CONTENT_ENCODED);
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.ITEM &&
                "enclosure".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_ENCLOSURE);
            String enclosureUrl = StringUtils.trim(attributes.getValue("url"));
            if (!StringUtils.isBlank(enclosureUrl)) {
                article.setEnclosureUrl(enclosureUrl);
                String length = attributes.getValue("length");
                if (!StringUtils.isBlank(length)) {
                    Integer enclosureLength = null;
                    try {
                        enclosureLength = Integer.valueOf(length);
                    } catch (Exception e) {
                        // NOP
                    }
                    article.setEnclosureLength(enclosureLength);
                }
                article.setEnclosureType(StringUtils.trim(attributes.getValue("type")));
            }
        } else if ((feedType == FeedType.RSS || feedType == FeedType.RDF) && currentElement == Element.ITEM &&
                "pubDate".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_PUB_DATE);
        } else if (feedType == FeedType.ATOM && currentElement == Element.FEED && "title".equalsIgnoreCase(localName)) {
            pushElement(Element.ATOM_TITLE);
        } else if (feedType == FeedType.ATOM && currentElement == Element.FEED && "subtitle".equalsIgnoreCase(localName)) {
            pushElement(Element.ATOM_SUBTITLE);
        } else if (feedType == FeedType.ATOM && currentElement == Element.FEED && "id".equalsIgnoreCase(localName)) {
            pushElement(Element.ATOM_ID);
        } else if (feedType == FeedType.ATOM && currentElement == Element.FEED && "link".equalsIgnoreCase(localName)) {
            String rel = StringUtils.trimToNull(attributes.getValue("rel"));
            String type = StringUtils.trimToNull(attributes.getValue("type"));
            String href = StringUtils.trimToNull(attributes.getValue("href"));
            atomLinkList.add(new AtomLink(rel, type, href));
            pushElement(Element.ATOM_LINK);
        } else if (feedType == FeedType.ATOM && currentElement == Element.FEED && "updated".equalsIgnoreCase(localName)) {
            pushElement(Element.ATOM_UPDATED);
        } else if (feedType == FeedType.ATOM && currentElement == Element.FEED && "entry".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY);
            article = new Article();
            articleList.add(article);

            atomArticleLinkList = new ArrayList<AtomLink>();
            String xmlBase = StringUtils.trimToNull(attributes.getValue(URI_XML, "base"));
            if (xmlBase != null) {
                atomArticleLinkList.add(new AtomLink(null, null, xmlBase));
                article.setBaseUri(xmlBase);
            }
        } else if (feedType == FeedType.ATOM && currentElement == Element.ENTRY && "title".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_TITLE);
        } else if (feedType == FeedType.ATOM && currentElement == Element.ENTRY && "link".equalsIgnoreCase(localName)) {
            String rel = StringUtils.trimToNull(attributes.getValue("rel"));
            String type = StringUtils.trimToNull(attributes.getValue("type"));
            String href = StringUtils.trimToNull(attributes.getValue("href"));
            if (href != null) {
                atomArticleLinkList.add(new AtomLink(rel, type, href));
            }
            String commentCountAsString = StringUtils.trimToNull(attributes.getValue(URI_THREAD, "count"));
            if (commentCountAsString != null) {
                try {
                    article.setCommentCount(Integer.parseInt(commentCountAsString));
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Error parsing comment count: " + commentCountAsString);
                    }
                }
            }

            pushElement(Element.ENTRY_LINK);
        } else if (feedType == FeedType.ATOM && currentElement == Element.ENTRY && "updated".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_UPDATED);
        } else if (feedType == FeedType.ATOM && currentElement == Element.ENTRY && "id".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_ID);
        } else if (feedType == FeedType.ATOM && currentElement == Element.ENTRY && "summary".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_SUMMARY);
        } else if (feedType == FeedType.ATOM && currentElement == Element.ENTRY && "content".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_CONTENT);
            String xmlBase = StringUtils.trimToNull(attributes.getValue(URI_XML, "base"));
            if (xmlBase != null) {
                // Overrides entry's xml:base
                article.setBaseUri(xmlBase);
            }
        } else if (feedType == FeedType.ATOM && currentElement == Element.ENTRY && "author".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_AUTHOR);
        } else if (feedType == FeedType.ATOM && currentElement == Element.ENTRY_AUTHOR && "name".equalsIgnoreCase(localName)) {
            pushElement(Element.AUTHOR_NAME);
        } else {
            pushElement(Element.UNKNOWN);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("title".equalsIgnoreCase(localName) && currentElement == Element.RSS_TITLE) {
            feed.setTitle(getContent());
        } else if ("link".equalsIgnoreCase(localName) && currentElement == Element.RSS_LINK) {
            feed.setUrl(getContent());
        } else if ("description".equalsIgnoreCase(localName) && currentElement == Element.RSS_DESCRIPTION) {
            feed.setDescription(getContent());
        } else if ("language".equalsIgnoreCase(localName) && currentElement == Element.RSS_LANGUAGE) {
            feed.setLanguage(getContent());
        } else if ("title".equalsIgnoreCase(localName) && currentElement == Element.ITEM_TITLE) {
            article.setTitle(getContent());
        } else if ("guid".equalsIgnoreCase(localName) && currentElement == Element.ITEM_GUID) {
            article.setGuid(getContent());
        } else if ("link".equalsIgnoreCase(localName) && currentElement == Element.ITEM_LINK) {
            article.setUrl(getContent());
        } else if ("comments".equalsIgnoreCase(localName) && currentElement == Element.ITEM_COMMENTS && !URI_SLASH.equals(uri)) {
            article.setCommentUrl(getContent());
        } else if ("comments".equalsIgnoreCase(localName) && currentElement == Element.ITEM_SLASH_COMMENTS && URI_SLASH.equals(uri)) {
            String commentCountAsString = getContent();
            try {
                article.setCommentCount(Integer.parseInt(commentCountAsString));
            } catch (NumberFormatException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Error parsing comment count: " + commentCountAsString);
                }
            }
        } else if ("description".equalsIgnoreCase(localName) && currentElement == Element.ITEM_DESCRIPTION) {
            if (article.getDescription() == null) {
                // Use encoded:content (full content) if available
                article.setDescription(getContent());
            }
        } else if ("creator".equalsIgnoreCase(localName) && currentElement == Element.ITEM_DC_CREATOR && URI_DC.equals(uri)) {
            article.setCreator(getContent());
        } else if ("date".equalsIgnoreCase(localName) && currentElement == Element.ITEM_DC_DATE && URI_DC.equals(uri)) {
            Date publicationDate = DateUtil.parseDate(getContent(), DF_DC);
            article.setPublicationDate(publicationDate);
        } else if ("pubDate".equalsIgnoreCase(localName) && currentElement == Element.ITEM_PUB_DATE) {
            Date publicationDate = DateUtil.parseDate(getContent(), DF_RSS);
            article.setPublicationDate(publicationDate);
        } else if ("encoded".equalsIgnoreCase(localName) && currentElement == Element.ITEM_CONTENT_ENCODED && URI_CONTENT.equals(uri)) {
            article.setDescription(getContent());
        } else if ("entry".equalsIgnoreCase(localName) && currentElement == Element.ENTRY) {
            String url = new AtomArticleUrlGuesserStrategy().guess(atomArticleLinkList);
            article.setUrl(url);
            String commentUrl = new AtomArticleCommentUrlGuesserStrategy().guess(atomArticleLinkList);
            article.setCommentUrl(commentUrl);
        } else if ("title".equalsIgnoreCase(localName) && currentElement == Element.ATOM_TITLE) {
            feed.setTitle(getContent());
        } else if ("subtitle".equalsIgnoreCase(localName) && currentElement == Element.ATOM_SUBTITLE) {
            feed.setDescription(getContent());
        } else if ("updated".equalsIgnoreCase(localName) && currentElement == Element.ATOM_UPDATED) {
            // TODO updated
        } else if ("title".equalsIgnoreCase(localName) && currentElement == Element.ENTRY_TITLE) {
            article.setTitle(getContent());
        } else if ("updated".equalsIgnoreCase(localName) && currentElement == Element.ENTRY_UPDATED) {
            article.setPublicationDate(DateUtil.parseDate(getContent(), DF_ATOM));
        } else if ("id".equalsIgnoreCase(localName) && currentElement == Element.ENTRY_ID) {
            article.setGuid(getContent());
        } else if ("summary".equalsIgnoreCase(localName) && currentElement == Element.ENTRY_SUMMARY) {
            if (article.getDescription() == null) {
                // Use content (full content) if available
                article.setDescription(getContent());
            }
        } else if ("content".equalsIgnoreCase(localName) && currentElement == Element.ENTRY_CONTENT) {
            article.setDescription(getContent());
        } else if ("name".equalsIgnoreCase(localName) && currentElement == Element.AUTHOR_NAME) {
            article.setCreator(getContent());
        }
        content = null;
        popElement();
    }

    /**
     * Creates a new Feed.
     */
    private void initFeed() {
        feed = new Feed();
        articleList = new ArrayList<Article>();
        atomLinkList = new ArrayList<AtomLink>();
    }

    /**
     * Push an element in the element stack.
     * 
     * @param newElement Element
     */
    private void pushElement(Element newElement) {
        if (currentElement != null) {
            elementStack.push(currentElement);
        }
        currentElement = newElement;
        if (log.isTraceEnabled()) {
            log.trace(">> " + newElement);
        }
    }

    /**
     * Pop an element from the element stack.
     * 
     */
    private void popElement() {
        if (!elementStack.isEmpty()) {
            currentElement = elementStack.pop();
        } else {
            currentElement = null;
        }
        if (log.isTraceEnabled()) {
            log.trace("<< " + currentElement);
        }
    }
    
    /**
     * Validate feed data.
     * 
     */
    private void validateFeed() throws Exception {
        if (feed == null) {
            throw new Exception("No feed found");
        }
    }

    /**
     * Try to guess a value for GUID element values in RSS feeds.
     */
    private void fixGuid() {
        if (articleList != null) {
            for (Article article: articleList) {
                GuidFixer.fixGuid(article);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String newContent = new String(ch, start, length);
        if (content == null) {
            content = newContent;
        } else {
            content += newContent;
        }
    }
    
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        log.warn("Fatal SAX parse error encountered, trying to resume parsing...", e);
        fatalErrorCount++;
        if (fatalErrorCount >= FATAL_ERROR_MAX) {
            throw new SAXException("Tried to recover too many times (" + FATAL_ERROR_MAX + "), giving up.");
        }
    }
    
    /**
     * Returns the content of the current element.
     * 
     * @return Content
     */
    private String getContent() {
        String content = StringUtils.trim(this.content);
        this.content = null;
        return content;
    }

    /**
     * Getter of feed.
     *
     * @return feed
     */
    public Feed getFeed() {
        return feed;
    }

    /**
     * Getter of articleList.
     *
     * @return articleList
     */
    public List<Article> getArticleList() {
        return articleList;
    }
}
