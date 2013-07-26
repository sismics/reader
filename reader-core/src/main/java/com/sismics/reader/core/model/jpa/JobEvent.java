package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Job event.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_JOB_EVENT")
public class JobEvent {
    /**
     * Job event ID.
     */
    @Id
    @Column(name = "JOE_ID_C", length = 36)
    private String id;
    
    /**
     * Job ID.
     */
    @Column(name = "JOE_IDJOB_C", nullable = false, length = 36)
    private String jobId;
    
    /**
     * Job event name.
     */
    @Column(name = "JOE_NAME_C", length = 50)
    private String name;
    
    /**
     * Job event value.
     */
    @Column(name = "JOE_VALUE_C", length = 250)
    private String value;
    
    /**
     * Creation date.
     */
    @Column(name = "JOE_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "JOE_DELETEDATE_D")
    private Date deleteDate;
    
    /**
     * Default constructor.
     */
    public JobEvent() {
    }
    
    /**
     * Job event constructor.
     * 
     * @param jobId Job ID
     * @param name Job event name
     * @param value Job event value
     */
    public JobEvent(String jobId, String name, String value) {
        this.jobId = jobId;
        this.name = name;
        this.value = value;
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
     * Getter of jobId.
     *
     * @return jobId
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Setter of jobId.
     *
     * @param jobId jobId
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
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
     * Getter of value.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Setter of value.
     *
     * @param value value
     */
    public void setValue(String value) {
        this.value = value;
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
                .add("value", value)
                .toString();
    }
}
