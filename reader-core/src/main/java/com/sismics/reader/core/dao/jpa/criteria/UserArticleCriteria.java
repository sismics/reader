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
     * The article is visible to the user (he has an UserArticle).
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
    public void setVisible(boolean visible) {
        this.visible = visible;
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
    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
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
    public void setArticleId(String articleId) {
        this.articleId = articleId;
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
    public void setUserArticleId(String userArticleId) {
        this.userArticleId = userArticleId;
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
    public void setArticlePublicationDateMax(Date articlePublicationDateMax) {
        this.articlePublicationDateMax = articlePublicationDateMax;
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
    public void setUserArticleStarredDateMax(Date userArticleStarredDateMax) {
        this.userArticleStarredDateMax = userArticleStarredDateMax;
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
    public void setArticleIdMax(String articleIdMax) {
        this.articleIdMax = articleIdMax;
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
    public void setUserArticleIdMax(String userArticleIdMax) {
        this.userArticleIdMax = userArticleIdMax;
    }
}
