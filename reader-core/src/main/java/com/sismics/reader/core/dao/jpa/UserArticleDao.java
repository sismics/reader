package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.dao.jpa.mapper.UserArticleMapper;
import com.sismics.reader.core.model.jpa.UserArticle;
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
 * User article DAO.
 * 
 * @author jtremeaux
 */
public class UserArticleDao extends BaseDao<UserArticleDto, UserArticleCriteria> {

    @Override
    protected QueryParam getQueryParam(UserArticleCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        StringBuilder sb = new StringBuilder("select ua.USA_ID_C, ua.USA_READDATE_D, ua.USA_STARREDDATE_D, f.FED_TITLE_C, fs.FES_ID_C, fs.FES_TITLE_C, a.ART_ID_C, a.ART_URL_C, a.ART_GUID_C, a.ART_TITLE_C, a.ART_CREATOR_C, a.ART_DESCRIPTION_C, a.ART_COMMENTURL_C, a.ART_COMMENTCOUNT_N, a.ART_ENCLOSUREURL_C, a.ART_ENCLOSURELENGTH_N, a.ART_ENCLOSURETYPE_C, a.ART_PUBLICATIONDATE_D");
        if (criteria.isVisible()) {
            if (criteria.isUnread() || criteria.isStarred()) {
                sb.append("  from T_USER_ARTICLE ua ");
                sb.append("  join T_ARTICLE a on(a.ART_ID_C = ua.USA_IDARTICLE_C) ");
            } else {
                sb.append("  from T_ARTICLE a ");
                sb.append("  join T_USER_ARTICLE ua on(a.ART_ID_C = ua.USA_IDARTICLE_C) ");
            }
            criteriaList.add("ua.USA_IDUSER_C = :userId and ua.USA_DELETEDATE_D is null");
        } else if (criteria.getUserId() != null) {
            sb.append("  from T_ARTICLE a ");
            sb.append("  left join T_USER_ARTICLE ua on(a.ART_ID_C = ua.USA_IDARTICLE_C and ua.USA_IDUSER_C = :userId and ua.USA_DELETEDATE_D is null) ");
        } else {
            sb.append("  from T_ARTICLE a ");
            sb.append("  left join T_USER_ARTICLE ua on(a.ART_ID_C = ua.USA_IDARTICLE_C and ua.USA_DELETEDATE_D is null) ");
        }
        sb.append("  join T_FEED f on(f.FED_ID_C = a.ART_IDFEED_C and f.FED_DELETEDATE_D is null) ");
        if (criteria.isFetchAllFeedSubscription()) {
            sb.append("  left join T_FEED_SUBSCRIPTION fs on(fs.FES_IDFEED_C = f.FED_ID_C and fs.FES_DELETEDATE_D is null) ");
        } else {
            sb.append("  left join T_FEED_SUBSCRIPTION fs on(fs.FES_IDFEED_C = f.FED_ID_C and fs.FES_IDUSER_C = :userId and fs.FES_DELETEDATE_D is null) ");
        }

        // Adds search criteria
        criteriaList.add("a.ART_DELETEDATE_D is null");
        if (criteria.getUserId() != null) {
            parameterMap.put("userId", criteria.getUserId());
        }
        if (criteria.getFeedId() != null) {
            criteriaList.add("a.ART_IDFEED_C = :feedId");
            parameterMap.put("feedId", criteria.getFeedId());
        }
        if (criteria.getArticleId() != null) {
            criteriaList.add("a.ART_ID_C = :articleId");
            parameterMap.put("articleId", criteria.getArticleId());
        }
        if (criteria.getArticleIdIn() != null) {
            criteriaList.add("a.ART_ID_C IN (:articleIdIn)");
            parameterMap.put("articleIdIn", criteria.getArticleIdIn());
        }
        if (criteria.getUserArticleId() != null) {
            criteriaList.add("ua.USA_ID_C = :userArticleId");
            parameterMap.put("userArticleId", criteria.getUserArticleId());
        }
        if (criteria.isSubscribed()) {
            criteriaList.add("fs.FES_ID_C is not null");
        }
        if (criteria.getCategoryId() != null) {
            criteriaList.add("fs.FES_IDCATEGORY_C = :categoryId");
            parameterMap.put("categoryId", criteria.getCategoryId());
        }
        if (criteria.isUnread()) {
            criteriaList.add("(ua.USA_READDATE_D is null and ua.USA_ID_C is not null)");
        }
        if (criteria.isStarred()) {
            criteriaList.add("ua.USA_STARREDDATE_D is not null");
        }
        if (criteria.getArticlePublicationDateMax() != null && criteria.getArticleIdMax() != null) {
            // Start the page after this article
            criteriaList.add("(a.ART_PUBLICATIONDATE_D < :articlePublicationDateMax or " +
                    "  a.ART_PUBLICATIONDATE_D = :articlePublicationDateMax and a.ART_ID_C < :articleIdMax" +
                    ")");
            parameterMap.put("articlePublicationDateMax", criteria.getArticlePublicationDateMax());
            parameterMap.put("articleIdMax", criteria.getArticleIdMax());
        }
        if (criteria.getUserArticleStarredDateMax() != null && criteria.getUserArticleIdMax() != null) {
            // Start the page this starred article
            criteriaList.add("(ua.USA_STARREDDATE_D < :userArticleStarredDateMax or " +
                    "  ua.USA_STARREDDATE_D = :userArticleStarredDateMax and ua.USA_ID_C < :userArticleIdMax" +
                    ")");
            parameterMap.put("userArticleStarredDateMax", criteria.getUserArticleStarredDateMax());
            parameterMap.put("userArticleIdMax", criteria.getUserArticleIdMax());
        }

        SortCriteria sortCriteria;
        if (criteria.isStarred()) {
            sortCriteria = new SortCriteria(" order by ua.USA_STARREDDATE_D desc, ua.USA_ID_C desc");
        } else {
            sortCriteria = new SortCriteria(" order by a.ART_PUBLICATIONDATE_D desc, ua.USA_ID_C desc");
        }

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new UserArticleMapper());
    }

    /**
     * Creates a new user article.
     * 
     * @param userArticle User article to create
     * @return New ID
     */
    public String create(UserArticle userArticle) {
        // Create the UUID
        userArticle.setId(UUID.randomUUID().toString());
        
        // Create the user article
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        userArticle.setCreateDate(new Date());
        em.persist(userArticle);
        
        return userArticle.getId();
    }
    
    /**
     * Updates a user subscription to an article.
     * 
     * @param userArticle UserArticle
     * @return Updated userArticle
     */
    public UserArticle update(UserArticle userArticle) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the subscription
        Query q = em.createQuery("select ua from UserArticle ua where ua.id = :id and ua.deleteDate is null")
                .setParameter("id", userArticle.getId());
        UserArticle userArticleFromDb = (UserArticle) q.getSingleResult();

        // Update the subscription
        userArticleFromDb.setReadDate(userArticle.getReadDate());
        userArticleFromDb.setStarredDate(userArticle.getStarredDate());
        
        return userArticle;
    }
    
    /**
     * Marks all articles in a category as read.
     * 
     * @param criteria Deletion criteria
     */
    public void markAsRead(UserArticleCriteria criteria) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        StringBuilder sb = new StringBuilder("update T_USER_ARTICLE as ua set USA_READDATE_D = :readDate where ua.USA_ID_C in (");
        sb.append("  select ua2.USA_ID_C from T_USER_ARTICLE ua2 ");
        sb.append("  join T_ARTICLE a on a.ART_ID_C = ua2.USA_IDARTICLE_C ");
        if (criteria.getFeedSubscriptionId() != null || criteria.getCategoryId() != null) {
            sb.append("  join T_FEED f on (f.FED_ID_C = a.ART_IDFEED_C and f.FED_DELETEDATE_D is null)");
            sb.append("  join T_FEED_SUBSCRIPTION fs on (fs.FES_IDFEED_C = f.FED_ID_C and fs.FES_DELETEDATE_D is null) ");
        }
        sb.append("  where a.ART_ID_C = ua2.USA_IDARTICLE_C and a.ART_DELETEDATE_D is null ");
        if (criteria.getFeedSubscriptionId() != null) {
            sb.append("    and fs.FES_ID_C = :feedSubscriptionId ");
        }
        if (criteria.getCategoryId() != null) {
            sb.append("    and fs.FES_IDCATEGORY_C = :categoryId ");
        }
        sb.append(" and ua2.USA_IDUSER_C = :userId and ua2.USA_DELETEDATE_D is null and ua2.USA_READDATE_D is null) ");
        Query q = em.createNativeQuery(sb.toString())
                .setParameter("userId", criteria.getUserId())
                .setParameter("readDate", new Date());
        if (criteria.getFeedSubscriptionId() != null) {
            q.setParameter("feedSubscriptionId", criteria.getFeedSubscriptionId());
        }
        if (criteria.getCategoryId() != null) {
            q.setParameter("categoryId", criteria.getCategoryId());
        }
        q.executeUpdate();
    }
    
    /**
     * Deletes a subscription.
     * 
     * @param id Subscription ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the subscription
        Query q = em.createQuery("select ua from UserArticle ua where ua.id = :id and ua.deleteDate is null")
                .setParameter("id", id);
        UserArticle userArticleFromDb = (UserArticle) q.getSingleResult();

        // Delete the subscription
        userArticleFromDb.setDeleteDate(new Date());
    }
    
    /**
     * Returns an active subscription.
     * 
     * @param id Subscription ID
     * @param userId User ID
     * @return User article
     */
    public UserArticle getUserArticle(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select ua from UserArticle ua where ua.id = :id and ua.userId = :userId and ua.deleteDate is null")
                .setParameter("id", id)
                .setParameter("userId", userId);
        try {
            return (UserArticle) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
