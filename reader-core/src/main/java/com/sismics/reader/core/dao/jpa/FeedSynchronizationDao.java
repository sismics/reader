package com.sismics.reader.core.dao.jpa;

import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;

import com.sismics.reader.core.model.jpa.FeedSynchronization;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Feed synchronization DAO.
 * 
 * @author bgamard
 */
public class FeedSynchronizationDao {
    /**
     * Creates a new feed synchronization.
     * 
     * @param feedSynchronization Feed synchronization to create
     * @return New ID
     */
    public String create(FeedSynchronization feedSynchronization) {
        // Create the UUID
        feedSynchronization.setId(UUID.randomUUID().toString());
        
        // Create the feed synchronization
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        feedSynchronization.setCreateDate(new Date());
        em.persist(feedSynchronization);
        
        return feedSynchronization.getId();
    }
}
