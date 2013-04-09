package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Category entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_CATEGORY")
public class Category {
    /**
     * Category ID.
     */
    @Id
    @Column(name = "CAT_ID_C", length = 36)
    private String id;
    
    /**
     * User ID.
     */
    @Column(name = "CAT_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Parent category ID.
     */
    @Column(name = "CAT_IDPARENT_C", length = 36)
    private String parentId;
    
    /**
     * Category name.
     */
    @Column(name = "CAT_NAME_C", length = 100)
    private String name;

    /**
     * Display order of this category.
     */
    @Column(name = "CAT_ORDER_N", nullable = false)
    private Integer order;

    /**
     * True if this category is folded in the subscriptions tree.
     */
    @Column(name = "CAT_FOLDED_B", nullable = false)
    private boolean folded;
 
    /**
     * Creation date.
     */
    @Column(name = "CAT_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "CAT_DELETEDATE_D")
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
     * Getter of parentId.
     *
     * @return parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Setter of parentId.
     *
     * @param parentId parentId
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Getter of name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter of name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
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
     * Getter of folded.
     *
     * @return folded
     */
    public boolean isFolded() {
        return folded;
    }

    /**
     * Setter of folded.
     *
     * @param folded folded
     */
    public void setFolded(boolean folded) {
        this.folded = folded;
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
                .add("name", name)
                .toString();
    }
}
