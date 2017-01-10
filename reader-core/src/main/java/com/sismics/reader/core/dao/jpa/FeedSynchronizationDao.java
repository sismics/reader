package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.model.jpa.FeedSynchronization;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.DialectUtil;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    
    /**
     * Deletes old feed synchronization rows.
     * 
     * @param feedId Feed ID
     * @param minutes All entries before NOW() - [minutes] will be deleted
     */
    public void deleteOldFeedSynchronization(String feedId, int minutes) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery("delete from T_FEED_SYNCHRONIZATION fs where FSY_IDFEED_C = :feedId and FSY_CREATEDATE_D < " +
                DialectUtil.getDateDiff("NOW()", String.valueOf(minutes), "MINUTE"))
                .setParameter("feedId", feedId)
                .executeUpdate();
    }

    /**
     * Find feed synchronizations by feed ID.
     * 
     * @param feedId Feed ID
     * @return List of feed synchronizations
     */
    @SuppressWarnings("unchecked")
    public List<FeedSynchronization> findByFeedId(String feedId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.createQuery("select fs from FeedSynchronization fs where fs.feedId = :feedId order by fs.createDate desc")
                .setParameter("feedId", feedId)
                .getResultList();
    }
}
