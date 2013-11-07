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
     * Content-type.
     */
    private String type;
    
    /**
     * Reference.
     */
    private String href;

    /**
     * Constructor of AtomLink.
     * 
     * @param type Content type
     * @param rel Relationship type
     * @param href Reference
     */
    public AtomLink(String rel, String type, String href) {
        this.rel = rel;
        this.type = type;
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
     * Setter of href.
     *
     * @param href href
     */
    public void setHref(String href) {
        this.href = href;
    }
}
