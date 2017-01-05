package com.sismics.reader.core.dao.jpa;

import com.google.common.base.Joiner;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.mapper.FeedSubscriptionMapper;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;
import java.util.Map.Entry;

/**
 * Feed subscription DAO.
 * 
 * @author jtremeaux
 */
public class FeedSubscriptionDao {
    /**
     * Creates a new feed subscription.
     * 
     * @param feedSubscription Feed subscription to create
     * @return New ID
     */
    public String create(FeedSubscription feedSubscription) {
        // Create the UUID
        feedSubscription.setId(UUID.randomUUID().toString());
        
        // Create the feed subscription
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        feedSubscription.setCreateDate(new Date());
        em.persist(feedSubscription);
        
        return feedSubscription.getId();
    }
    
    /**
     * Updates a feedSubscription.
     * 
     * @param feedSubscription FeedSubscription
     * @return Updated feedSubscription
     */
    public FeedSubscription update(FeedSubscription feedSubscription) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the subscription
        Query q = em.createQuery("select fs from FeedSubscription fs where fs.id = :id and fs.deleteDate is null");
        q.setParameter("id", feedSubscription.getId());
        FeedSubscription feedSubscriptionFromDb = (FeedSubscription) q.getSingleResult();

        // Update the subscription
        feedSubscriptionFromDb.setTitle(feedSubscription.getTitle());
        feedSubscriptionFromDb.setCategoryId(feedSubscription.getCategoryId());
        feedSubscriptionFromDb.setOrder(feedSubscription.getOrder());

        return feedSubscription;
    }

    /**
     * Update the number of unread articles in a user subscription
     *
     * @param id User subscription ID
     * @param unreadCount Number of unread articles
     */
    public void updateUnreadCount(String id, Integer unreadCount) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery("update T_FEED_SUBSCRIPTION set FES_UNREADCOUNT_N = :unreadCount where FES_ID_C = :id");
        q.setParameter("id", id);
        q.setParameter("unreadCount", unreadCount);
        q.executeUpdate();
    }

    /**
     * Moves the subscription to the specified display order, and reorders adjacent subscription.
     * 
     * @param feedSubscription Subscription to move
     * @param order New display order
     */
    @SuppressWarnings("unchecked")
    public void reorder(FeedSubscription feedSubscription, int order) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Find categories with the same parent
        Query q = em.createQuery("select fs from FeedSubscription fs where fs.categoryId = :categoryId and fs.userId = :userId and fs.deleteDate is null order by fs.order");
        q.setParameter("categoryId", feedSubscription.getCategoryId());
        q.setParameter("userId", feedSubscription.getUserId());
        List<FeedSubscription> feedSubscriptionList = (List<FeedSubscription>) q.getResultList();
        for (int i = 0; i < feedSubscriptionList.size(); i++) {
            FeedSubscription currentFeedSubscription = feedSubscriptionList.get(i);
            if (currentFeedSubscription.getId().equals(feedSubscription.getId())) {
                feedSubscriptionList.remove(i);
            }
        }
        feedSubscriptionList.add(order > feedSubscriptionList.size() ? feedSubscriptionList.size() : order, feedSubscription);
        for (int i = 0; i < feedSubscriptionList.size(); i++) {
            FeedSubscription currentFeedSubscription = feedSubscriptionList.get(i);
            currentFeedSubscription.setOrder(i);
        }
    }
    
    /**
     * Deletes a subscription.
     * 
     * @param id Subscription ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the subscription
        Query q = em.createQuery("select fs from FeedSubscription fs where fs.id = :id and fs.deleteDate is null");
        q.setParameter("id", id);
        FeedSubscription feedSubscriptionFromDb = (FeedSubscription) q.getSingleResult();

        // Delete the subscription
        feedSubscriptionFromDb.setDeleteDate(new Date());
    }
    
    /**
     * Returns an active subscription.
     * 
     * @param id Subscription ID
     * @param userId User ID
     * @return Feed subscription
     */
    public FeedSubscription getFeedSubscription(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select fs from FeedSubscription fs where fs.id = :id and fs.userId = :userId and fs.deleteDate is null");
        q.setParameter("id", id);
        q.setParameter("userId", userId);
        try {
            return (FeedSubscription) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns active subscriptions in a category.
     * 
     * @param categoryId Category ID
     * @return Feed subscription
     */
    @SuppressWarnings("unchecked")
    public List<FeedSubscription> findByCategory(String categoryId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select fs from FeedSubscription fs where fs.categoryId = :categoryId and fs.deleteDate is null");
        q.setParameter("categoryId", categoryId);
        return q.getResultList();
    }
    
    /**
     * Returns the number of feed subscriptions in a category.
     * 
     * @param categoryId Category ID
     * @param userId User ID
     * @return Category
     */
    public int getCategoryCount(String categoryId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select count(fs.id) from FeedSubscription fs where fs.categoryId = :categoryId and fs.userId = :userId and fs.deleteDate is null");
        q.setParameter("categoryId", categoryId);
        q.setParameter("userId", userId);
        return ((Long) q.getSingleResult()).intValue();
    }

    /**
     * Searches feed subscriptions by criteria.
     * 
     * @param criteria Search criteria
     * @return List of feed subscriptions
     */
    @SuppressWarnings("unchecked")
    public List<FeedSubscriptionDto> findByCriteria(FeedSubscriptionCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        StringBuilder sb = new StringBuilder("select fs.FES_ID_C, fs.FES_TITLE_C, fs.FES_UNREADCOUNT_N, fs.FES_CREATEDATE_D, fs.FES_IDUSER_C, f.FED_ID_C, f.FED_TITLE_C, f.FED_RSSURL_C, f.FED_URL_C, f.FED_DESCRIPTION_C, c.CAT_ID_C, c.CAT_IDPARENT_C, c.CAT_NAME_C, c.CAT_FOLDED_B,");
        sb.append(" (select count(fsy.FSY_ID_C) from (select * from T_FEED_SYNCHRONIZATION fsy where fsy.FSY_IDFEED_C = f.FED_ID_C order by fsy.FSY_CREATEDATE_D desc limit 5) fsy where fsy.FSY_SUCCESS_B = false) ");
        sb.append(" from T_FEED_SUBSCRIPTION fs ");
        sb.append(" join T_FEED f on(f.FED_ID_C = fs.FES_IDFEED_C and f.FED_DELETEDATE_D is null) ");
        sb.append(" join T_CATEGORY c on(c.CAT_ID_C = fs.FES_IDCATEGORY_C and c.CAT_DELETEDATE_D is null) ");

        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getId() != null) {
            criteriaList.add("fs.FES_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getUserId() != null) {
            criteriaList.add("fs.FES_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        if (criteria.getFeedId() != null) {
            criteriaList.add("fs.FES_IDFEED_C = :feedId");
            parameterMap.put("feedId", criteria.getFeedId());
        }
        if (criteria.getCategoryId() != null) {
            criteriaList.add("fs.FES_IDCATEGORY_C = :categoryId");
            parameterMap.put("categoryId", criteria.getCategoryId());
        }
        if (criteria.getFeedUrl() != null) {
            criteriaList.add("f.FED_RSSURL_C = :feedUrl");
            parameterMap.put("feedUrl", criteria.getFeedUrl());
        }
        if (criteria.isUnread()) {
            criteriaList.add("fs.FES_UNREADCOUNT_N > 0");
        }
        criteriaList.add("fs.FES_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        sb.append(" order by c.CAT_IDPARENT_C asc, c.CAT_ORDER_N asc, fs.FES_ORDER_N asc");
        
        // Search
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        for (Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> resultList = q.getResultList();
        
        // Map results
        return new FeedSubscriptionMapper().map(resultList);
    }
}
