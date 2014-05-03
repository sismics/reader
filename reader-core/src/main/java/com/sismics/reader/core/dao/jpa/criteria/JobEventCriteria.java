package com.sismics.reader.core.dao.jpa.criteria;

/**
 * Job event criteria.
 *
 * @author jtremeaux 
 */
public class JobEventCriteria {
    /**
     * Job ID.
     */
    private String jobId;
    
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
    public JobEventCriteria setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }
}
