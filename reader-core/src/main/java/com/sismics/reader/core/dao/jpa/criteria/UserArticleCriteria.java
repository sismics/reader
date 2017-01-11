package com.sismics.reader.core.dao.jpa.criteria;

import java.util.Date;
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
     * The article is visible to the user (he has a UserArticle).
     */
    private boolean visible;
    
    /**
     * Returns the article only if the user is subscribed to the feed.
     */
    private boolean subscribed;
    
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
     * Article ID.
     */
    private String articleId;

    /**
     * Article ID (inclusive).
     */
    private List<String> articleIdIn;

    /**
     * User article ID.
     */
    private String userArticleId;

    /**
     * Return only articles before this publication date.
     */
    private Date articlePublicationDateMax;

    /**
     * Return only articles before this starred date.
     */
    private Date userArticleStarredDateMax;

    /**
     * Return only articles before this ID.
     */
    private String articleIdMax;

    /**
     * Return only user articles before this ID.
     */
    private String userArticleIdMax;

    /**
     * Fetch all subscriptions (returns subscriptions * articles rows)
     */
    private boolean fetchAllFeedSubscription;

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
    public UserArticleCriteria setUserId(String userId) {
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
    public UserArticleCriteria setFeedId(String feedId) {
        this.feedId = feedId;
        return this;
    }

    /**
     * Getter of visible.
     *
     * @return visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Setter of visible.
     *
     * @param visible visible
     */
    public UserArticleCriteria setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    /**
     * Getter of subscribed.
     *
     * @return subscribed
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Setter of subscribed.
     *
     * @param subscribed subscribed
     */
    public UserArticleCriteria setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
        return this;
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
    public UserArticleCriteria setFeedSubscriptionId(String feedSubscriptionId) {
        this.feedSubscriptionId = feedSubscriptionId;
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
    public UserArticleCriteria setUnread(boolean unread) {
        this.unread = unread;
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
    public UserArticleCriteria setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        return this;
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
    public UserArticleCriteria setStarred(boolean starred) {
        this.starred = starred;
        return this;
    }

    /**
     * Getter of articleId.
     *
     * @return articleId
     */
    public String getArticleId() {
        return articleId;
    }

    /**
     * Setter of articleId.
     *
     * @param articleId articleId
     */
    public UserArticleCriteria setArticleId(String articleId) {
        this.articleId = articleId;
        return this;
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
    public UserArticleCriteria setArticleIdIn(List<String> articleIdIn) {
        this.articleIdIn = articleIdIn;
        return this;
    }

    /**
     * Getter of userArticleId.
     *
     * @return userArticleId
     */
    public String getUserArticleId() {
        return userArticleId;
    }

    /**
     * Setter of userArticleId.
     *
     * @param userArticleId userArticleId
     */
    public UserArticleCriteria setUserArticleId(String userArticleId) {
        this.userArticleId = userArticleId;
        return this;
    }

    /**
     * Getter of articlePublicationDateMax.
     *
     * @return articlePublicationDateMax
     */
    public Date getArticlePublicationDateMax() {
        return articlePublicationDateMax;
    }

    /**
     * Setter of articlePublicationDateMax.
     *
     * @param articlePublicationDateMax articlePublicationDateMax
     */
    public UserArticleCriteria setArticlePublicationDateMax(Date articlePublicationDateMax) {
        this.articlePublicationDateMax = articlePublicationDateMax;
        return this;
    }

    /**
     * Getter of userArticleStarredDateMax.
     *
     * @return userArticleStarredDateMax
     */
    public Date getUserArticleStarredDateMax() {
        return userArticleStarredDateMax;
    }

    /**
     * Setter of userArticleStarredDateMax.
     *
     * @param userArticleStarredDateMax userArticleStarredDateMax
     */
    public UserArticleCriteria setUserArticleStarredDateMax(Date userArticleStarredDateMax) {
        this.userArticleStarredDateMax = userArticleStarredDateMax;
        return this;
    }

    /**
     * Getter of articleIdMax.
     *
     * @return articleIdMax
     */
    public String getArticleIdMax() {
        return articleIdMax;
    }

    /**
     * Setter of articleIdMax.
     *
     * @param articleIdMax articleIdMax
     */
    public UserArticleCriteria setArticleIdMax(String articleIdMax) {
        this.articleIdMax = articleIdMax;
        return this;
    }

    /**
     * Getter of userArticleIdMax.
     *
     * @return userArticleIdMax
     */
    public String getUserArticleIdMax() {
        return userArticleIdMax;
    }

    /**
     * Setter of userArticleIdMax.
     *
     * @param userArticleIdMax userArticleIdMax
     */
    public UserArticleCriteria setUserArticleIdMax(String userArticleIdMax) {
        this.userArticleIdMax = userArticleIdMax;
        return this;
    }

    public boolean isFetchAllFeedSubscription() {
        return fetchAllFeedSubscription;
    }

    public UserArticleCriteria setFetchAllFeedSubscription(boolean fetchAllFeedSubscription) {
        this.fetchAllFeedSubscription = fetchAllFeedSubscription;
        return this;
    }
}
