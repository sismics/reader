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
     * Category ID.
     */
    private String categoryId;
    
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
    public FeedSubscriptionCriteria setId(String id) {
        this.id = id;
        return this;
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
    public FeedSubscriptionCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
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
    public FeedSubscriptionCriteria setFeedId(String feedId) {
        this.feedId = feedId;
        return this;
    }

    /**
     * Getter of categoryId.
     *
     * @return categoryId
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Setter of categoryId.
     *
     * @param categoryId categoryId
     */
    public FeedSubscriptionCriteria setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        return this;
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
    public FeedSubscriptionCriteria setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
        return this;
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
    public FeedSubscriptionCriteria setUnread(boolean unread) {
        this.unread = unread;
        return this;
    }
}
