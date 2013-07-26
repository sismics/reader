package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Job.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_JOB")
public class Job {
    /**
     * Job ID.
     */
    @Id
    @Column(name = "JOB_ID_C", length = 36)
    private String id;
    
    /**
     * User ID.
     */
    @Column(name = "JOB_IDUSER_C", length = 36)
    private String userId;
    
    /**
     * Job name.
     */
    @Column(name = "JOB_NAME_C", length = 50, nullable = false)
    private String name;
    
    /**
     * Creation date.
     */
    @Column(name = "JOB_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Start date.
     */
    @Column(name = "JOB_STARTDATE_D")
    private Date startDate;
    
    /**
     * End date.
     */
    @Column(name = "JOB_ENDDATE_D")
    private Date endDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "JOB_DELETEDATE_D")
    private Date deleteDate;
    
    /**
     * Default constructor.
     */
    public Job() {
    }
    
    /**
     * Job constructor.
     * 
     * @param userId User ID
     * @param name Job name
     */
    public Job(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }
    
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
     * Getter of startDate.
     *
     * @return startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Setter of startDate.
     *
     * @param startDate startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter of endDate.
     *
     * @return endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Setter of endDate.
     *
     * @param endDate endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
