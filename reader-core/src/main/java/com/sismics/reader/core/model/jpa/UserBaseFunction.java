package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * User base function.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_USER_BASE_FUNCTION")
public class UserBaseFunction {
    /**
     * Subscription ID.
     */
    @Id
    @Column(name = "UBF_ID_C", length = 36)
    private String id;
    
    /**
     * User ID.
     */
    @Column(name = "UBF_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Base function ID.
     */
    @Column(name = "UBF_IDBASEFUNCTION_C", nullable = false, length = 36)
    private String baseFunctionId;
    
    /**
     * Creation date.
     */
    @Column(name = "UBF_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Date the user starred this article.
     */
    @Column(name = "UBF_STARREDDATE_D")
    private Date starredDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "UBF_DELETEDATE_D")
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
     * Getter of baseFunctionId.
     *
     * @return baseFunctionId
     */
    public String getBaseFunctionId() {
        return baseFunctionId;
    }

    /**
     * Setter of baseFunctionId.
     *
     * @param baseFunctionId baseFunctionId
     */
    public void setBaseFunctionId(String baseFunctionId) {
        this.baseFunctionId = baseFunctionId;
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
                .add("baseFunctionId", baseFunctionId)
                .toString();
    }
}
