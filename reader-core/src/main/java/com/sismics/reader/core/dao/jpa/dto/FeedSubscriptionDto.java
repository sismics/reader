package com.sismics.reader.core.dao.jpa.dto;

import java.util.Date;

/**
 * Feed subscription DTO.
 *
 * @author jtremeaux 
 */
public class FeedSubscriptionDto {
    /**
     * Feed subscription ID.
     */
    private String id;

    /**
     * Feed subscription title.
     */
    private String feedSubscriptionTitle;
    
    /**
     * Feed title.
     */
    private String feedTitle;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Feed ID.
     */
    private String feedId;

    /**
     * Feed RSS URL.
     */
    private String feedRssUrl;

    /**
     * Feed URL.
     */
    private String feedUrl;

    /**
     * Feed description.
     */
    private String feedDescription;

    /**
     * Number of unread articles by this user in this subscription.
     */
    private Integer unreadUserArticleCount;
    
    /**
     * Number of synchronization fails recently.
     */
    private Integer synchronizationFailCount;
    
    /**
     * Create date.
     */
    private Date createDate;

    /**
     * Category ID.
     */
    private String categoryId;

    /**
     * Category parent Id.
     */
    private String categoryParentId;

    /**
     * Category name.
     */
    private String categoryName;

    /**
     * True if this category is folded in the subscriptions tree.
     */
    private boolean categoryFolded;

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
     * Getter of feedSubscriptionTitle.
     *
     * @return feedSubscriptionTitle
     */
    public String getFeedSubscriptionTitle() {
        return feedSubscriptionTitle;
    }

    /**
     * Setter of feedSubscriptionTitle.
     *
     * @param feedSubscriptionTitle feedSubscriptionTitle
     */
    public void setFeedSubscriptionTitle(String feedSubscriptionTitle) {
        this.feedSubscriptionTitle = feedSubscriptionTitle;
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
     * Getter of feedRssUrl.
     *
     * @return feedRssUrl
     */
    public String getFeedRssUrl() {
        return feedRssUrl;
    }

    /**
     * Setter of feedRssUrl.
     *
     * @param feedRssUrl feedRssUrl
     */
    public void setFeedRssUrl(String feedRssUrl) {
        this.feedRssUrl = feedRssUrl;
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
     * Getter of feedDescription.
     *
     * @return feedDescription
     */
    public String getFeedDescription() {
        return feedDescription;
    }

    /**
     * Setter of feedDescription.
     *
     * @param feedDescription feedDescription
     */
    public void setFeedDescription(String feedDescription) {
        this.feedDescription = feedDescription;
    }

    /**
     * Getter of unreadUserArticleCount.
     *
     * @return unreadUserArticleCount
     */
    public Integer getUnreadUserArticleCount() {
        return unreadUserArticleCount;
    }

    /**
     * Setter of unreadUserArticleCount.
     *
     * @param unreadUserArticleCount unreadUserArticleCount
     */
    public void setUnreadUserArticleCount(Integer unreadUserArticleCount) {
        this.unreadUserArticleCount = unreadUserArticleCount;
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
     * Getter of categoryParentId.
     *
     * @return categoryParentId
     */
    public String getCategoryParentId() {
        return categoryParentId;
    }

    /**
     * Setter of categoryParentId.
     *
     * @param categoryParentId categoryParentId
     */
    public void setCategoryParentId(String categoryParentId) {
        this.categoryParentId = categoryParentId;
    }

    /**
     * Getter of categoryName.
     *
     * @return categoryName
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Setter of categoryName.
     *
     * @param categoryName categoryName
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Getter of categoryFolded.
     *
     * @return categoryFolded
     */
    public boolean isCategoryFolded() {
        return categoryFolded;
    }

    /**
     * Setter of categoryFolded.
     *
     * @param categoryFolded categoryFolded
     */
    public void setCategoryFolded(boolean categoryFolded) {
        this.categoryFolded = categoryFolded;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getSynchronizationFailCount() {
        return synchronizationFailCount;
    }

    public void setSynchronizationFailCount(Integer synchronizationFailCount) {
        this.synchronizationFailCount = synchronizationFailCount;
    }
}
