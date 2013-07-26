package com.sismics.reader.core.dao.jpa.dto;

/**
 * Job DTO.
 *
 * @author jtremeaux 
 */
public class JobDto {
    /**
     * Job ID.
     */
    private String id;

    /**
     * Job name.
     */
    private String name;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Creation date.
     */
    private Long createTimestamp;

    /**
     * Start date.
     */
    private Long startTimestamp;

    /**
     * End date.
     */
    private Long endTimestamp;


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
     * Getter of createTimestamp.
     *
     * @return createTimestamp
     */
    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    /**
     * Setter of createTimestamp.
     *
     * @param createTimestamp createTimestamp
     */
    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    /**
     * Getter of startTimestamp.
     *
     * @return startTimestamp
     */
    public Long getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Setter of startTimestamp.
     *
     * @param startTimestamp startTimestamp
     */
    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    /**
     * Getter of endTimestamp.
     *
     * @return endTimestamp
     */
    public Long getEndTimestamp() {
        return endTimestamp;
    }

    /**
     * Setter of endTimestamp.
     *
     * @param endTimestamp endTimestamp
     */
    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }
}
