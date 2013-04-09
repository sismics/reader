package com.sismics.reader.core.dao.jpa.criteria;

import java.util.List;

/**
 * User article subscriptions criteria.
 *
 * @author jtremeaux 
 */
public class UserArticleCriteria {
    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Feed ID.
     */
    private String feedId;
    
    /**
     * Feed subscription ID.
     */
    private String feedSubscriptionId;
    
    /**
     * Category ID.
     */
    private String categoryId;
    
    /**
     * Returns only unread articles.
     */
    private boolean unread;
    
    /**
     * Returns only starred articles.
     */
    private boolean starred;

    /**
     * Article ID (inclusive).
     */
    List<String> articleIdIn;
    
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
     * Getter of feedSubscriptionId.
     *
     * @return feedSubscriptionId
     */
    public String getFeedSubscriptionId() {
        return feedSubscriptionId;
    }

    /**
     * Setter of feedSubscriptionId.
     *
     * @param feedSubscriptionId feedSubscriptionId
     */
    public void setFeedSubscriptionId(String feedSubscriptionId) {
        this.feedSubscriptionId = feedSubscriptionId;
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
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Getter of starred.
     *
     * @return starred
     */
    public boolean isStarred() {
        return starred;
    }

    /**
     * Setter of starred.
     *
     * @param starred starred
     */
    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    /**
     * Getter of articleIdIn.
     *
     * @return articleIdIn
     */
    public List<String> getArticleIdIn() {
        return articleIdIn;
    }

    /**
     * Setter of articleIdIn.
     *
     * @param articleIdIn articleIdIn
     */
    public void setArticleIdIn(List<String> articleIdIn) {
        this.articleIdIn = articleIdIn;
    }
}
