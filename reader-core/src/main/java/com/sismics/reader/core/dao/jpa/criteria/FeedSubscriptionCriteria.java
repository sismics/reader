package com.sismics.reader.core.dao.jpa.criteria;

/**
 * Feed subscription criteria.
 *
 * @author jtremeaux 
 */
public class FeedSubscriptionCriteria {
    /**
     * Feed subscription id.
     */
    private String id;
    
    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Feed ID.
     */
    private String feedId;
    
    /**
     * Feed URL.
     */
    private String feedUrl;

    /**
     * Returns only subscriptions having unread articles.
     */
    private boolean unread;

    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
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

    /**
     * Getter of unread.
     *
     * @return unread
     */
    public boolean isUnread() {
        return unread;
    }

    /**
     * Setter of unread.
     *
     * @param unread unread
     */
    public void setUnread(boolean unread) {
        this.unread = unread;
    }
}
