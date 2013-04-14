package com.sismics.reader.core.dao.file.opml;

import java.util.ArrayList;
import java.util.List;

/**
 * OPML outline.
 * 
 * @author jtremeaux
 */
public class Outline {
    private String text;
    
    private String title;
    
    private String type;
    
    private String xmlUrl;
    
    private String htmlUrl;
    
    private List<Outline> outlineList;
    
    public Outline() {
        outlineList = new ArrayList<Outline>();
    }

    /**
     * Getter of text.
     *
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * Setter of text.
     *
     * @param text text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Getter of title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter of title.
     *
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter of type.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter of type.
     *
     * @param type type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter of xmlUrl.
     *
     * @return xmlUrl
     */
    public String getXmlUrl() {
        return xmlUrl;
    }

    /**
     * Setter of xmlUrl.
     *
     * @param xmlUrl xmlUrl
     */
    public void setXmlUrl(String xmlUrl) {
        this.xmlUrl = xmlUrl;
    }

    /**
     * Getter of htmlUrl.
     *
     * @return htmlUrl
     */
    public String getHtmlUrl() {
        return htmlUrl;
    }

    /**
     * Setter of htmlUrl.
     *
     * @param htmlUrl htmlUrl
     */
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    /**
     * Getter of outlineList.
     *
     * @return outlineList
     */
    public List<Outline> getOutlineList() {
        return outlineList;
    }
}
