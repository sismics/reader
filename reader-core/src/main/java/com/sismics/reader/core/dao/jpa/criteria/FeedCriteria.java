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
     * Returns only feed having user subscriptions.
     */
    private boolean withUserSubscription;

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
    public FeedCriteria setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
        return this;
    }

    /**
     * Getter of withUserSubscription.
     *
     * @return withUserSubscription
     */
    public boolean isWithUserSubscription() {
        return withUserSubscription;
    }

    /**
     * Setter of withUserSubscription.
     *
     * @param withUserSubscription withUserSubscription
     */
    public FeedCriteria setWithUserSubscription(boolean withUserSubscription) {
        this.withUserSubscription = withUserSubscription;
        return this;
    }

}
