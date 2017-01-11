package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.FeedCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.reader.core.dao.jpa.mapper.FeedMapper;
import com.sismics.reader.core.model.jpa.Feed;
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
 * Feed DAO.
 * 
 * @author jtremeaux
 */
public class FeedDao extends BaseDao<FeedDto, FeedCriteria> {

    @Override
    protected QueryParam getQueryParam(FeedCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
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
        criteriaList.add("f.FED_DELETEDATE_D is null");
        if (criteria.getFeedUrl() != null) {
            criteriaList.add("f.FED_URL_C = :feedUrl");
            parameterMap.put("feedUrl", criteria.getFeedUrl());
        }
        if (criteria.isWithUserSubscription()) {
            criteriaList.add("(select count(fs.FES_ID_C)" +
                    " from T_FEED_SUBSCRIPTION fs" +
                    " where fs.FES_IDFEED_C = f.FED_ID_C and fs.FES_DELETEDATE_D is null) > 0");
        }

        SortCriteria sortCriteria = new SortCriteria("  order by f.FED_CREATEDATE_D asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new FeedMapper());
    }

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
        Query q = em.createQuery("select f from Feed f where f.id = :id and f.deleteDate is null")
                .setParameter("id", id);
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
        Query q = em.createQuery("select f from Feed f where f.rssUrl = :rssUrl and f.deleteDate is null")
                .setParameter("rssUrl", rssUrl);
        try {
            return (Feed) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
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
        Query q = em.createQuery("select f from Feed f where f.id = :id and f.deleteDate is null")
                .setParameter("id", feed.getId());
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
}
