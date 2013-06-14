package com.sismics.reader.core.dao.file.rss;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.DateUtil;

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
    private static final List<DateTimeFormatter> DF_RSS = ImmutableList.of(
            DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").withOffsetParsed().withLocale(Locale.ENGLISH),
            DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz").withOffsetParsed().withLocale(Locale.ENGLISH),
            DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss Z").withOffsetParsed().withLocale(Locale.ENGLISH),
            DateTimeFormat.forPattern("yyyy-mm-dd HH:mm:ss").withOffsetParsed().withLocale(Locale.ENGLISH));
    
    /**
     * A list of common date formats used in Atom feeds.
     */
    private static final List<DateTimeFormatter> DF_ATOM = ImmutableList.of(
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").withOffsetParsed().withLocale(Locale.ENGLISH),
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withOffsetParsed().withLocale(Locale.ENGLISH));

    /**
     * Contents of the current element.
     */
    private String content;

    private Feed feed;
    
    private Article article;
    
    private List<Article> articleList;
    
    private List<AtomLink> atomLinkList;
    
    private String URI_ATOM = "http://www.w3.org/2005/Atom";
    
    private String URI_SLASH = "http://purl.org/rss/1.0/modules/slash/";
    
    private String URI_DC = "http://purl.org/dc/elements/1.1/";
    
    private String URI_CONTENT = "http://purl.org/rss/1.0/modules/content/";
    
    private enum Element {
        UNKNOWN,
        
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
        
        ITEM_PUB_DATE,
        
        ITEM_CONTENT_ENCODED,
        
        ITEM_ENCLOSURE,

        FEED,

        ATOM_TITLE,

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
    }
    
    private Element currentElement;

    private Stack<Element> elementStack;

    private boolean rss;
    
    private boolean atom;
    
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
     * @throws Exception
     */
    public void readRssFeed(InputStream is) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
        SAXParser parser = factory.newSAXParser();

        // Pass a character stream to the parser for it to pick-up the correct encoding.
        // See http://stackoverflow.com/questions/3482494/
        String xml = CharStreams.toString(new InputStreamReader(is));
        xml = xml.replaceFirst("^([\\W]+)<","<");  //$NON-NLS-1$ //$NON-NLS-2$
        
        try {
            InputSource source = new InputSource(new StringReader(xml));
            parser.parse(source, this);
        } catch (InternalError e) {
            // Fix for Oracle code throwing java.lang.InternalError disgracefully
            throw new Exception(e);
        }
    
        if (atom) {
            String url = new AtomUrlGuesserStrategy().guess(atomLinkList);
            feed.setUrl(url);
        }
        validateFeed();
        fixGuid();
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("rss".equalsIgnoreCase(localName)) {
            initFeed();
            pushElement(Element.RSS);
            rss = true;
            return;
        } else if ("feed".equalsIgnoreCase(localName)) {
            initFeed();
            pushElement(Element.FEED);
            atom = true;
            return;
        }
        if (feed == null) {
            throw new SAXException("Root element doesn't designate an RSS/Atom feed, encountered: " + localName);
        }
        
        if (rss && currentElement == Element.RSS && "channel".equalsIgnoreCase(localName)) {
            pushElement(Element.RSS_CHANNEL);
        } else if (rss && currentElement == Element.RSS_CHANNEL && "title".equalsIgnoreCase(localName)) {
            pushElement(Element.RSS_TITLE);
        } else if (rss && currentElement == Element.RSS_CHANNEL && "description".equalsIgnoreCase(localName)) {
            pushElement(Element.RSS_DESCRIPTION);
        } else if (rss && currentElement == Element.RSS_CHANNEL && "link".equalsIgnoreCase(localName) && !URI_ATOM.equals(uri)) {
            pushElement(Element.RSS_LINK);
        } else if (rss && currentElement == Element.RSS_CHANNEL && "language".equals(localName)) {
            pushElement(Element.RSS_LANGUAGE);
        } else if (rss && currentElement == Element.RSS_CHANNEL && "item".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM);
            article = new Article();
            articleList.add(article);
        } else if (rss && currentElement == Element.ITEM && "title".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_TITLE);
        } else if (rss && currentElement == Element.ITEM && "guid".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_GUID);
        } else if (rss && currentElement == Element.ITEM && "link".equalsIgnoreCase(localName) && !URI_ATOM.equals(uri)) {
            pushElement(Element.ITEM_LINK);
        } else if (rss && currentElement == Element.ITEM && "comments".equalsIgnoreCase(localName) && !URI_SLASH.equals(uri)) {
            pushElement(Element.ITEM_COMMENTS);
        } else if (rss && currentElement == Element.ITEM && "comments".equalsIgnoreCase(localName) && URI_SLASH.equalsIgnoreCase(uri)) {
            pushElement(Element.ITEM_SLASH_COMMENTS);
        } else if (rss && currentElement == Element.ITEM && "description".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_DESCRIPTION);
        } else if (rss && currentElement == Element.ITEM && "creator".equalsIgnoreCase(localName) && URI_DC.equalsIgnoreCase(uri)) {
            pushElement(Element.ITEM_DC_CREATOR);
        } else if (rss && currentElement == Element.ITEM && "encoded".equalsIgnoreCase(localName) && URI_CONTENT.equalsIgnoreCase(uri)) {
            pushElement(Element.ITEM_CONTENT_ENCODED);
        } else if (rss && currentElement == Element.ITEM && "enclosure".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_ENCLOSURE);
            String enclosureUrl = StringUtils.trim(attributes.getValue("url"));
            if (!StringUtils.isBlank(enclosureUrl)) {
                article.setEnclosureUrl(enclosureUrl);
                Integer enclosureLength = null;
                try {
                    enclosureLength = Integer.valueOf(attributes.getValue("length"));
                } catch (Exception e) {
                    log.error("Error parsing enclosure length", e);
                }
                article.setEnclosureLength(enclosureLength);
                article.setEnclosureType(StringUtils.trim(attributes.getValue("type")));
            }
        } else if (rss && currentElement == Element.ITEM && "pubDate".equalsIgnoreCase(localName)) {
            pushElement(Element.ITEM_PUB_DATE);
        } else if (atom && currentElement == Element.FEED && "title".equalsIgnoreCase(localName)) {
            pushElement(Element.ATOM_TITLE);
        } else if (atom && currentElement == Element.FEED && "id".equalsIgnoreCase(localName)) {
            pushElement(Element.ATOM_ID);
        } else if (atom && currentElement == Element.FEED && "link".equalsIgnoreCase(localName)) {
            String rel = StringUtils.trim(attributes.getValue("rel"));
            String href = StringUtils.trim(attributes.getValue("href"));
            if (!"self".equals(rel)) {
                atomLinkList.add(new AtomLink(rel, href));
            }
            pushElement(Element.ATOM_LINK);
        } else if (atom && currentElement == Element.FEED && "updated".equalsIgnoreCase(localName)) {
            pushElement(Element.ATOM_UPDATED);
        } else if (atom && currentElement == Element.FEED && "entry".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY);
            article = new Article();
            articleList.add(article);
        } else if (atom && currentElement == Element.ENTRY && "title".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_TITLE);
        } else if (atom && currentElement == Element.ENTRY && "link".equalsIgnoreCase(localName)) {
            article.setUrl(StringUtils.trim(attributes.getValue("href")));
            pushElement(Element.ENTRY_LINK);
        } else if (atom && currentElement == Element.ENTRY && "updated".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_UPDATED);
        } else if (atom && currentElement == Element.ENTRY && "id".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_ID);
        } else if (atom && currentElement == Element.ENTRY && "summary".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_SUMMARY);
        } else if (atom && currentElement == Element.ENTRY && "content".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_CONTENT);
        } else if (atom && currentElement == Element.ENTRY && "author".equalsIgnoreCase(localName)) {
            pushElement(Element.ENTRY_AUTHOR);
        } else if (atom && currentElement == Element.ENTRY_AUTHOR && "name".equalsIgnoreCase(localName)) {
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
            int commentCount = 0;
            String commentCountAsString = getContent();
            try {
                commentCount = Integer.parseInt(commentCountAsString);
            } catch (NumberFormatException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Error parsing comment count: " + commentCountAsString);
                }
            }
            article.setCommentCount(commentCount);
        } else if ("description".equalsIgnoreCase(localName) && currentElement == Element.ITEM_DESCRIPTION) {
            if (article.getDescription() == null) {
                // Use encoded:content (full content) if available
                article.setDescription(getContent());
            }
        } else if ("creator".equalsIgnoreCase(localName) && currentElement == Element.ITEM_DC_CREATOR && URI_DC.equals(uri)) {
            article.setCreator(getContent());
        } else if ("pubDate".equalsIgnoreCase(localName) && currentElement == Element.ITEM_PUB_DATE) {
            Date publicationDate = parseDate(DF_RSS);
            article.setPublicationDate(publicationDate);
        } else if ("encoded".equalsIgnoreCase(localName) && currentElement == Element.ITEM_CONTENT_ENCODED && URI_CONTENT.equals(uri)) {
            article.setDescription(getContent());
        } else if ("title".equalsIgnoreCase(localName) && currentElement == Element.ATOM_TITLE) {
            feed.setTitle(getContent());
        } else if ("updated".equalsIgnoreCase(localName) && currentElement == Element.ATOM_UPDATED) {
            // TODO updated
        } else if ("title".equalsIgnoreCase(localName) && currentElement == Element.ENTRY_TITLE) {
            article.setTitle(getContent());
        } else if ("updated".equalsIgnoreCase(localName) && currentElement == Element.ENTRY_UPDATED) {
            article.setPublicationDate(parseDate(DF_ATOM));
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
     * Extract a date from a string format.
     * 
     * @param dateTimeFormatterList List of DateTimeFormatter
     * @return Date or null is the date is unparsable
     */
    private Date parseDate(List<DateTimeFormatter> dateTimeFormatterList) {
        String dateAsString = getContent();
        if (StringUtils.isBlank(dateAsString)) {
            return null;
        }
        Date publicationDate = null;
        for (DateTimeFormatter df : dateTimeFormatterList) {
            try {
                publicationDate = df.parseDateTime(dateAsString).toDate();
                return publicationDate;
            } catch (IllegalArgumentException e) {
                // NOP
            }
        }
        String dateWithOffset= DateUtil.guessTimezoneOffset(dateAsString);
        if (!dateWithOffset.equals(dateAsString)) {
            for (DateTimeFormatter df : dateTimeFormatterList) {
                try {
                    publicationDate = df.parseDateTime(dateWithOffset).toDate();
                    return publicationDate;
                } catch (IllegalArgumentException e) {
                    // NOP
                }
            }
        }
        
        if (log.isWarnEnabled()) {
            log.warn(MessageFormat.format("Error parsing comment date: {0}", dateAsString));
        }
        return publicationDate;
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
     * @throws Exception
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
        log.error("Fatal SAX parse error encountered, trying to resume parsing...", e);
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
