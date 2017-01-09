package com.sismics.reader.core.dao.jpa;

import com.google.common.base.Joiner;
import com.sismics.reader.core.dao.jpa.criteria.FeedCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.reader.core.dao.jpa.mapper.FeedMapper;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;
import java.util.Map.Entry;

/**
 * Feed DAO.
 * 
 * @author jtremeaux
 */
public class FeedDao {
    /**
     * Creates a new feed.
     * 
     * @param feed Feed to create
     * @return New ID
     */
    public String create(Feed feed) {
        // Create the UUID
        feed.setId(UUID.randomUUID().toString());
        
        // Create the feed
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        feed.setCreateDate(new Date());
        em.persist(feed);
        
        return feed.getId();
    }
    
    /**
     * Deletes a feed.
     * 
     * @param id Feed ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the feed
        Query q = em.createQuery("select f from Feed f where f.id = :id and f.deleteDate is null");
        q.setParameter("id", id);
        Feed feedFromDb = (Feed) q.getSingleResult();

        // Delete the feed
        feedFromDb.setDeleteDate(new Date());
    }
    
    /**
     * Get an active feed by its URL.
     * 
     * @param rssUrl RSS URL
     */
    public Feed getByRssUrl(String rssUrl) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the feed
        Query q = em.createQuery("select f from Feed f where f.rssUrl = :rssUrl and f.deleteDate is null");
        q.setParameter("rssUrl", rssUrl);
        try {
            return (Feed) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Searches feeds by criteria.
     * 
     * @param criteria Search criteria
     * @return List of feeds
     */
    @SuppressWarnings("unchecked")
    public List<FeedDto> findByCriteria(FeedCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        StringBuilder sb = new StringBuilder("select f.FED_ID_C as id, f.FED_RSSURL_C ");
        if (criteria.isWithUserSubscription()) {
            sb.append(", (select count(fs.FES_ID_C)");
            sb.append("     from T_FEED_SUBSCRIPTION fs");
            sb.append("     where fs.FES_IDFEED_C = f.FED_ID_C and fs.FES_DELETEDATE_D is null)");
            sb.append("  as feedSubscriptionCount");
        }
        sb.append(" from T_FEED f ");
        
        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getFeedUrl() != null) {
            criteriaList.add("f.FED_URL_C = :feedUrl");
            parameterMap.put("feedUrl", criteria.getFeedUrl());
        }
        if (criteria.isWithUserSubscription()) {
            criteriaList.add("(select count(fs.FES_ID_C)" +
            		" from T_FEED_SUBSCRIPTION fs" +
            		" where fs.FES_IDFEED_C = f.FED_ID_C and fs.FES_DELETEDATE_D is null) > 0");
        }
        criteriaList.add("f.FED_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        sb.append(" order by f.FED_CREATEDATE_D asc");
        
        // Search
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        for (Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> resultList = q.getResultList();
        
        // Map results
        return new FeedMapper().map(resultList);
    }

    /**
     * Updates a feed.
     * 
     * @param feed Feed to update
     * @return Updated feed
     */
    public Feed update(Feed feed) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the feed
        Query q = em.createQuery("select f from Feed f where f.id = :id and f.deleteDate is null");
        q.setParameter("id", feed.getId());
        Feed feedFromDb = (Feed) q.getSingleResult();

        // Update the feed
        feedFromDb.setUrl(feed.getUrl());
        feedFromDb.setBaseUri(feed.getBaseUri());
        feedFromDb.setTitle(feed.getTitle());
        feedFromDb.setLanguage(feed.getLanguage());
        feedFromDb.setDescription(feed.getDescription());
        feedFromDb.setLastFetchDate(feed.getLastFetchDate());
        
        return feed;
    }

    /**
     * Updates a feed RSS URL.
     *
     * @param feed Feed to update
     * @return Updated feed
     */
    public Feed updateRssUrl(Feed feed) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Update the feed
        em.createNativeQuery("update t_feed set fed_rssurl_c = :rssUrl where fed_id_c = :id")
            .setParameter("rssUrl", feed.getRssUrl())
            .setParameter("id", feed.getId())
            .executeUpdate();

        return feed;
    }
}
