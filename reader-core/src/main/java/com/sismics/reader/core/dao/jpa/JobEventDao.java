package com.sismics.reader.core.dao.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.base.Joiner;
import com.sismics.reader.core.dao.jpa.criteria.JobEventCriteria;
import com.sismics.reader.core.dao.jpa.dto.JobEventDto;
import com.sismics.reader.core.model.jpa.JobEvent;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Job event DAO.
 * 
 * @author jtremeaux
 */
public class JobEventDao {
    /**
     * Creates a new job event.
     * 
     * @param jobEvent Job event to create
     * @return New ID
     */
    public String create(JobEvent jobEvent) {
        // Create the UUID
        jobEvent.setId(UUID.randomUUID().toString());
        
        // Create the jobEvent
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        jobEvent.setCreateDate(new Date());
        em.persist(jobEvent);
        
        return jobEvent.getId();
    }
    
    /**
     * Deletes a job event.
     * 
     * @param id Job event ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the jobEvent
        Query q = em.createQuery("select e from JobEvent e where e.id = :id and e.deleteDate is null");
        q.setParameter("id", id);
        JobEvent jobEventFromDb = (JobEvent) q.getSingleResult();

        // Delete the jobEvent
        jobEventFromDb.setDeleteDate(new Date());
    }
    
    /**
     * Searches job events by criteria.
     * 
     * @param criteria Search criteria
     * @return List of job events
     */
    @SuppressWarnings("unchecked")
    public List<JobEventDto> findByCriteria(JobEventCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        StringBuilder sb = new StringBuilder("select e.JOE_ID_C, e.JOE_NAME_C, e.JOE_VALUE_C ");
        sb.append(" from T_JOB_EVENT e ");
        
        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getJobId() != null) {
            criteriaList.add("e.JOE_IDJOB_C = :jobId");
            parameterMap.put("jobId", criteria.getJobId());
        }
        criteriaList.add("e.JOE_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        sb.append(" order by e.JOE_CREATEDATE_D asc");
        
        // Search
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        for (Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> resultList = q.getResultList();
        
        // Assemble results
        List<JobEventDto> jobEventDtoList = new ArrayList<JobEventDto>();
        for (Object[] o : resultList) {
            int i = 0;
            JobEventDto jobEventDto = new JobEventDto();
            jobEventDto.setId((String) o[i++]);
            jobEventDto.setName((String) o[i++]);
            jobEventDto.setValue((String) o[i++]);
            jobEventDtoList.add(jobEventDto);
        }
        return jobEventDtoList;
    }
}
