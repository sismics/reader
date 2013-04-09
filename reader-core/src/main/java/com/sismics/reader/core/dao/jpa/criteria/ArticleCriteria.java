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
}
