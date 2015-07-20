package com.sismics.reader.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Subscription from a user to a feed.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_FEED_SUBSCRIPTION")
public class FeedSubscription {
    /**
     * Subscription ID.
     */
    @Id
    @Column(name = "FES_ID_C", length = 36)
    private String id;
    
    /**
     * User ID.
     */
    @Column(name = "FES_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Feed ID.
     */
    @Column(name = "FES_IDFEED_C", nullable = false, length = 36)
    private String feedId;
    
    /**
     * Category ID.
     */
    @Column(name = "FES_IDCATEGORY_C", nullable = false, length = 36)
    private String categoryId;
    
    /**
     * Subscription title (overrides feed title).
     */
    @Column(name = "FES_TITLE_C", length = 100)
    private String title;

    /**
     * Display order of this feed in the category.
     */
    @Column(name = "FES_ORDER_N", nullable = false)
    private Integer order;

    /**
     * Number of unread articles in this subscription.
     */
    @Column(name = "FES_UNREADCOUNT_N", nullable = false)
    private Integer unreadCount;

    /**
     * Creation date.
     */
    @Column(name = "FES_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "FES_DELETEDATE_D")
    private Date deleteDate;
    
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
     * Getter of order.
     *
     * @return order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Setter of order.
     *
     * @param order order
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * Getter of unreadCount.
     *
     * @return unreadCount
     */
    public Integer getUnreadCount() {
        return unreadCount;
    }

    /**
     * Setter of unreadCount.
     *
     * @param unreadCount unreadCount
     */
    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * Getter of createDate.
     *
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     *
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Getter of deleteDate.
     *
     * @return deleteDate
     */
    public Date getDeleteDate() {
        return deleteDate;
    }

    /**
     * Setter of deleteDate.
     *
     * @param deleteDate deleteDate
     */
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("feedId", feedId)
                .add("title", title)
                .toString();
    }
}
