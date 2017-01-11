package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.JobCriteria;
import com.sismics.reader.core.dao.jpa.dto.JobDto;
import com.sismics.reader.core.dao.jpa.mapper.JobMapper;
import com.sismics.reader.core.model.jpa.Job;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

/**
 * Job DAO.
 * 
 * @author jtremeaux
 */
public class JobDao extends BaseDao<JobDto, JobCriteria> {

    @Override
    protected QueryParam getQueryParam(JobCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select j.JOB_ID_C as id, j.JOB_NAME_C, j.JOB_IDUSER_C, j.JOB_CREATEDATE_D, j.JOB_STARTDATE_D, j.JOB_ENDDATE_D")
                .append("  from T_JOB j ");

        // Adds search criteria
        criteriaList.add("j.JOB_DELETEDATE_D is null");
        if (criteria.getUserId() != null) {
            criteriaList.add("j.JOB_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }

        SortCriteria sortCriteria = new SortCriteria("  order by j.JOB_CREATEDATE_D asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new JobMapper());
    }

    /**
     * Creates a new job.
     * 
     * @param job Job to create
     * @return New ID
     */
    public String create(Job job) {
        // Create the UUID
        job.setId(UUID.randomUUID().toString());
        
        // Create the job
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        job.setCreateDate(new Date());
        em.persist(job);
        
        return job.getId();
    }

    /**
     * Returns an active job.
     *
     * @param id Job ID
     * @return Job
     */
    public Job getActiveJob(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select j from Job j where j.id = :id and j.deleteDate is null")
                .setParameter("id", id);
        try {
            return (Job) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Deletes a job.
     * 
     * @param id Job ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the job
        Query q = em.createQuery("select j from Job j where j.id = :id and j.deleteDate is null")
                .setParameter("id", id);
        Job jobFromDb = (Job) q.getSingleResult();

        // Delete the job
        jobFromDb.setDeleteDate(new Date());
    }

    /**
     * Updates a job.
     * 
     * @param job Job to update
     * @return Updated job
     */
    public Job update(Job job) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the job
        Query q = em.createQuery("select j from Job j where j.id = :id and j.deleteDate is null")
                .setParameter("id", job.getId());
        Job jobFromDb = (Job) q.getSingleResult();

        // Update the job
        jobFromDb.setStartDate(job.getStartDate());
        jobFromDb.setEndDate(job.getEndDate());
        
        return job;
    }
}
