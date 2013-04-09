package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Subscription from a user to an article.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_USER_ARTICLE")
public class UserArticle {
    /**
     * Subscription ID.
     */
    @Id
    @Column(name = "USA_ID_C", length = 36)
    private String id;
    
    /**
     * User ID.
     */
    @Column(name = "USA_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Article ID.
     */
    @Column(name = "USA_IDARTICLE_C", nullable = false, length = 36)
    private String articleId;
    
    /**
     * Creation date.
     */
    @Column(name = "USA_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Read date of the article.
     */
    @Column(name = "USA_READDATE_D")
    private Date readDate;
    
    /**
     * Date the user starred this article.
     */
    @Column(name = "USA_STARREDDATE_D")
    private Date starredDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "USA_DELETEDATE_D")
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
     * Getter of readDate.
     *
     * @return readDate
     */
    public Date getReadDate() {
        return readDate;
    }

    /**
     * Setter of readDate.
     *
     * @param readDate readDate
     */
    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }

    /**
     * Getter of starredDate.
     *
     * @return starredDate
     */
    public Date getStarredDate() {
        return starredDate;
    }

    /**
     * Setter of starredDate.
     *
     * @param starredDate starredDate
     */
    public void setStarredDate(Date starredDate) {
        this.starredDate = starredDate;
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
                .add("articleId", articleId)
                .toString();
    }
}
