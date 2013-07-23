package com.sismics.reader.core.dao.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.google.common.base.Joiner;
import com.sismics.reader.core.dao.jpa.criteria.FeedCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.context.ThreadLocalContext;

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
            Feed feed = (Feed) q.getSingleResult();
            return feed;
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
        
        // Assemble results
        List<FeedDto> feedDtoList = new ArrayList<FeedDto>();
        for (Object[] o : resultList) {
            int i = 0;
            FeedDto feedDto = new FeedDto();
            feedDto.setId((String) o[i++]);
            feedDto.setRssUrl((String) o[i++]);
            feedDtoList.add(feedDto);
        }
        return feedDtoList;
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
        feedFromDb.setTitle(feed.getTitle());
        feedFromDb.setLanguage(feed.getLanguage());
        feedFromDb.setDescription(feed.getDescription());
        feedFromDb.setLastFetchDate(feed.getLastFetchDate());
        
        return feed;
    }
}
