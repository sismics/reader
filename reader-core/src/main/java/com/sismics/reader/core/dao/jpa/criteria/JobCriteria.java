package com.sismics.reader.core.dao.jpa.criteria;

/**
 * Job criteria.
 *
 * @author jtremeaux 
 */
public class JobCriteria {
    /**
     * User ID.
     */
    private String userId;
    
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
    public JobCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }
}
