package com.sismics.reader.core.dao.file.opml;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Stack;

/**
 * OPML parser.
 *
 * @author jtremeaux
 */
public class OpmlReader extends DefaultHandler {
    private static final Logger log = LoggerFactory.getLogger(OpmlReader.class);

    /**
     * Contents of the current element.
     */
    private String content;

    /**
     * Root outline.
     */
    private Outline rootOutline;
    
    /**
     * Reference to the current outline (root, or a sub category).
     */
    private Outline currentOutline;
    
    private enum Element {
        UNKNOWN,
        
        OPML,
        
        BODY,
        
        OUTLINE,
    }
    
    private Element currentElement;

    private Stack<Element> elementStack;

    private Stack<Outline> outlineStack;

    /**
     * Constructor of OpmlReader.
     * 
     */
    public OpmlReader() throws MalformedURLException {
        elementStack = new Stack<Element>();
        outlineStack = new Stack<Outline>();
    }

    /**
     * Reads an OPML into a tree structure.
     * 
     * @param is Input stream
     */
    public void read(InputStream is) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        SAXParser parser = factory.newSAXParser();

        parser.parse(is, this);
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (currentElement == null && !"opml".equalsIgnoreCase(localName)) {
            throw new SAXException("Root element must be opml, encountered: " + localName);
        }
        
        if (currentElement == null && "opml".equalsIgnoreCase(localName)) {
            pushElement(Element.OPML);
            rootOutline = new Outline();
            rootOutline.setType("root");
            currentOutline = rootOutline;
            outlineStack.push(currentOutline);
        } else if (currentElement == Element.OPML && "body".equalsIgnoreCase(localName)) {
            pushElement(Element.BODY);
        } else if ((currentElement == Element.BODY || currentElement == Element.OUTLINE) && "outline".equalsIgnoreCase(localName)) {
            pushElement(Element.OUTLINE);
            Outline outline = new Outline();
            outline.setText(StringUtils.trim(attributes.getValue("text")));
            outline.setTitle(StringUtils.trim(attributes.getValue("title")));
            outline.setXmlUrl(StringUtils.trim(attributes.getValue("xmlUrl")));
            outline.setHtmlUrl(StringUtils.trim(attributes.getValue("htmlUrl")));
            outline.setType(StringUtils.trim(attributes.getValue("type")));
            
            if (StringUtils.isBlank(outline.getType()) || "folder".equals(outline.getType()) || "rss".equals(outline.getType())) {
                currentOutline.getOutlineList().add(outline);
                currentOutline = outline;
            } else {
                log.warn(MessageFormat.format("Ignoring unknown outline of type {0}", outline.getType()));
            }
            
            outlineStack.push(currentOutline);
        } else {
            pushElement(Element.UNKNOWN);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("outline".equalsIgnoreCase(localName) && currentElement == Element.OUTLINE) {
            outlineStack.pop();
            currentOutline = outlineStack.peek();
        }
        content = null;
        popElement();
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
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String newContent = new String(ch, start, length);
        if (content == null) {
            content = newContent;
        } else {
            content += newContent;
        }
    }
    
    /**
     * Getter of outlineList.
     *
     * @return outlineList
     */
    public List<Outline> getOutlineList() {
        return rootOutline != null ? rootOutline.getOutlineList() : null;
    }
}
