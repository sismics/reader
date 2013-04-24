package com.sismics.reader.core.dao.jpa.criteria;

import java.util.List;

/**
 * Feed criteria.
 *
 * @author jtremeaux 
 */
public class ArticleCriteria {
    /**
     * Article GUID list (inclusive).
     */
    private List<String> guidIn;
    
    /**
     * Article title.
     */
    private String title;

    /**
     * Feed ID.
     */
    private String feedId;
    
    /**
     * Getter of guidIn.
     *
     * @return guidIn
     */
    public List<String> getGuidIn() {
        return guidIn;
    }

    /**
     * Setter of guidIn.
     *
     * @param guidIn guidIn
     */
    public void setGuidIn(List<String> guidIn) {
        this.guidIn = guidIn;
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
     * Getter of feedId.
     *
     * @return feedId
     */
    public String getFeedId() {
        return feedId;
    }

    /**
     * Setter of feedId.
     *
     * @param feedId feedId
     */
    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }
}
