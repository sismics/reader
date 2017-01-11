package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.JobEventCriteria;
import com.sismics.reader.core.dao.jpa.dto.JobEventDto;
import com.sismics.reader.core.dao.jpa.mapper.JobEventMapper;
import com.sismics.reader.core.model.jpa.JobEvent;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * Job event DAO.
 * 
 * @author jtremeaux
 */
public class JobEventDao extends BaseDao<JobEventDto, JobEventCriteria> {

    @Override
    protected QueryParam getQueryParam(JobEventCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select e.JOE_ID_C, e.JOE_NAME_C, e.JOE_VALUE_C ")
                .append(" from T_JOB_EVENT e ");

        // Adds search criteria
        criteriaList.add("e.JOE_DELETEDATE_D is null");
        if (criteria.getJobId() != null) {
            criteriaList.add("e.JOE_IDJOB_C = :jobId");
            parameterMap.put("jobId", criteria.getJobId());
        }

        SortCriteria sortCriteria = new SortCriteria("  order by e.JOE_CREATEDATE_D asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new JobEventMapper());
    }

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
        Query q = em.createQuery("select e from JobEvent e where e.id = :id and e.deleteDate is null")
                .setParameter("id", id);
        JobEvent jobEventFromDb = (JobEvent) q.getSingleResult();

        // Delete the jobEvent
        jobEventFromDb.setDeleteDate(new Date());
    }
}
