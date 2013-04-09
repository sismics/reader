package com.sismics.reader.core.dao.file.rss;

/**
 * Atom Link.
 * 
 * @author jtremeaux
 */
public class AtomLink {

    /**
     * Relationship type.
     */
    private String rel;
    
    /**
     * Reference.
     */
    private String href;

    /**
     * Constructor of RssLink.
     * 
     * @param rel Relationship type
     * @param href Reference
     */
    public AtomLink(String rel, String href) {
        this.rel = rel;
        this.href = href;
    }

    /**
     * Getter of rel.
     *
     * @return rel
     */
    public String getRel() {
        return rel;
    }

    /**
     * Setter of rel.
     *
     * @param rel rel
     */
    public void setRel(String rel) {
        this.rel = rel;
    }

    /**
     * Getter of href.
     *
     * @return href
     */
    public String getHref() {
        return href;
    }

    /**
     * Setter of href.
     *
     * @param href href
     */
    public void setHref(String href) {
        this.href = href;
    }
}
