package com.sismics.reader.core.dao.jpa;

import com.google.common.base.Joiner;
import com.sismics.reader.core.dao.jpa.criteria.JobCriteria;
import com.sismics.reader.core.dao.jpa.dto.JobDto;
import com.sismics.reader.core.dao.jpa.mapper.JobMapper;
import com.sismics.reader.core.model.jpa.Job;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;
import java.util.Map.Entry;

/**
 * Job DAO.
 * 
 * @author jtremeaux
 */
public class JobDao {
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
        Query q = em.createQuery("select j from Job j where j.id = :id and j.deleteDate is null");
        q.setParameter("id", id);
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
        Query q = em.createQuery("select j from Job j where j.id = :id and j.deleteDate is null");
        q.setParameter("id", id);
        Job jobFromDb = (Job) q.getSingleResult();

        // Delete the job
        jobFromDb.setDeleteDate(new Date());
    }
    
    /**
     * Searches jobs by criteria.
     * 
     * @param criteria Search criteria
     * @return List of jobs
     */
    @SuppressWarnings("unchecked")
    public List<JobDto> findByCriteria(JobCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        StringBuilder sb = new StringBuilder("select j.JOB_ID_C as id, j.JOB_NAME_C, j.JOB_IDUSER_C, j.JOB_CREATEDATE_D, j.JOB_STARTDATE_D, j.JOB_ENDDATE_D ");
        sb.append(" from T_JOB j ");
        
        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getUserId() != null) {
            criteriaList.add("j.JOB_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        criteriaList.add("j.JOB_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        sb.append(" order by j.JOB_CREATEDATE_D asc");
        
        // Search
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        for (Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> resultList = q.getResultList();
        
        // Map results
        return new JobMapper().map(resultList);
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
        Query q = em.createQuery("select j from Job j where j.id = :id and j.deleteDate is null");
        q.setParameter("id", job.getId());
        Job jobFromDb = (Job) q.getSingleResult();

        // Update the job
        jobFromDb.setStartDate(job.getStartDate());
        jobFromDb.setEndDate(job.getEndDate());
        
        return job;
    }
}
