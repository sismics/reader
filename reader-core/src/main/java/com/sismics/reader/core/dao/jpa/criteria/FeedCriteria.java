package com.sismics.reader.core.dao.jpa.criteria;

/**
 * Feed criteria.
 *
 * @author jtremeaux 
 */
public class FeedCriteria {
    /**
     * Feed URL.
     */
    private String feedUrl;

    /**
     * Getter of feedUrl.
     *
     * @return feedUrl
     */
    public String getFeedUrl() {
        return feedUrl;
    }

    /**
     * Setter of feedUrl.
     *
     * @param feedUrl feedUrl
     */
    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }
}
