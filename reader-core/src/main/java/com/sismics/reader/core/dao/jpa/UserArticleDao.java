package com.sismics.reader.core.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.google.common.base.Joiner;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.core.util.jpa.QueryParam;
import com.sismics.reader.core.util.jpa.QueryUtil;
import com.sismics.util.context.ThreadLocalContext;

/**
 * User article DAO.
 * 
 * @author jtremeaux
 */
public class UserArticleDao {
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
        Query q = em.createQuery("select ua from UserArticle ua where ua.id = :id and ua.deleteDate is null");
        q.setParameter("id", userArticle.getId());
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
        
        StringBuilder sb = new StringBuilder("update T_USER_ARTICLE ua set ua.USA_READDATE_D = :readDate where ua.USA_IDARTICLE_C in (");
        sb.append("  select a.ART_ID_C ");
        sb.append("  from T_ARTICLE a ");
        if (criteria.getFeedSubscriptionId() != null || criteria.getCategoryId() != null) {
            sb.append("  join T_FEED f on (f.FED_ID_C = a.ART_IDFEED_C and f.FED_DELETEDATE_D is null)");
            sb.append("  join T_FEED_SUBSCRIPTION fs on (fs.FES_IDFEED_C = f.FED_ID_C and fs.FES_DELETEDATE_D is null) ");
        }
        sb.append("  where a.ART_ID_C = ua.USA_IDARTICLE_C and a.ART_DELETEDATE_D is null ");
        if (criteria.getFeedSubscriptionId() != null) {
            sb.append("    and fs.FES_ID_C = :feedSubscriptionId ");
        }
        if (criteria.getCategoryId() != null) {
            sb.append("    and fs.FES_IDCATEGORY_C = :categoryId ");
        }
        sb.append(") and ua.USA_IDUSER_C = :userId and ua.USA_DELETEDATE_D is null and ua.USA_READDATE_D is null");
        Query q = em.createNativeQuery(sb.toString());
        if (criteria.getFeedSubscriptionId() != null) {
            q.setParameter("feedSubscriptionId", criteria.getFeedSubscriptionId());
        }
        if (criteria.getCategoryId() != null) {
            q.setParameter("categoryId", criteria.getCategoryId());
        }
        q.setParameter("userId", criteria.getUserId());
        q.setParameter("readDate", new Date());
        q.executeUpdate();
    }
    
    /**
     * Deletes a subscription.
     * 
     * @param id Subscription ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the category
        Query q = em.createQuery("select ua from UserArticle ua where ua.id = :id and ua.deleteDate is null");
        q.setParameter("id", id);
        UserArticle userArticleFromDb = (UserArticle) q.getSingleResult();

        // Delete the category
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
        Query q = em.createQuery("select ua from UserArticle ua where ua.id = :id and ua.userId = :userId and ua.deleteDate is null");
        q.setParameter("id", id);
        q.setParameter("userId", userId);
        try {
            return (UserArticle) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Searches user articles by criteria.
     * 
     * @param criteria Search criteria
     * @return List of user articles
     */
    @SuppressWarnings("unchecked")
    public List<UserArticleDto> findByCriteria(UserArticleCriteria criteria) {
        QueryParam queryParam = getQueryParam(criteria);
        Query q = QueryUtil.getNativeQuery(queryParam);
        List<Object[]> l = q.getResultList();
        return assembleResultList(l);
    }
    
    /**
     * Count user articles by criteria.
     * 
     * @param criteria Search criteria
     * @param paginatedList Paginated list (populated by side effects)
     */
    public void countByCriteria(UserArticleCriteria criteria, PaginatedList<UserArticleDto> paginatedList) {
        QueryParam queryParam = getQueryParam(criteria);
        PaginatedLists.executeCountQuery(paginatedList, queryParam);
    }

    /**
     * Searches user articles by criteria.
     * 
     * @param criteria Search criteria
     * @param paginatedList Paginated list (populated by side effects)
     */
    public void findByCriteria(UserArticleCriteria criteria, PaginatedList<UserArticleDto> paginatedList) {
        QueryParam queryParam = getQueryParam(criteria);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam);
        List<UserArticleDto> userArticleDtoList = assembleResultList(l);
        paginatedList.setResultList(userArticleDtoList);
    }

    /**
     * Assemble the query results.
     * 
     * @param l Query results as a table
     * @return Query results as a list of domain objects
     */
    private List<UserArticleDto> assembleResultList(List<Object[]> l) {
        // Assemble results
        List<UserArticleDto> userArticleDtoList = new ArrayList<UserArticleDto>();
        for (Object[] o : l) {
            int i = 0;
            UserArticleDto userArticleDto = new UserArticleDto();
            userArticleDto.setId((String) o[i++]);
            Timestamp readTimestamp = (Timestamp) o[i++];
            if (readTimestamp != null) {
                userArticleDto.setReadTimestamp(readTimestamp.getTime());
            }
            Timestamp starTimestamp = (Timestamp) o[i++];
            if (starTimestamp != null) {
                userArticleDto.setStarTimestamp(starTimestamp.getTime());
            }
            userArticleDto.setFeedTitle((String) o[i++]);
            userArticleDto.setFeedSubscriptionId((String) o[i++]);
            userArticleDto.setFeedSubscriptionTitle((String) o[i++]);
            userArticleDto.setArticleId((String) o[i++]);
            userArticleDto.setArticleUrl((String) o[i++]);
            userArticleDto.setArticleGuid((String) o[i++]);
            userArticleDto.setArticleTitle((String) o[i++]);
            userArticleDto.setArticleCreator((String) o[i++]);
            userArticleDto.setArticleDescription((String) o[i++]);
            userArticleDto.setArticleCommentUrl((String) o[i++]);
            userArticleDto.setArticleCommentCount((Integer) o[i++]);
            userArticleDto.setArticleEnclosureUrl((String) o[i++]);
            userArticleDto.setArticleEnclosureLength((Integer) o[i++]);
            userArticleDto.setArticleEnclosureType((String) o[i++]);
            userArticleDto.setArticlePublicationTimestamp(((Timestamp) o[i++]).getTime());
            userArticleDtoList.add(userArticleDto);
        }
        return userArticleDtoList;
    }
    
    /**
     * Creates the query parameters from the criteria.
     * 
     * @param criteria Search criteria
     * @return Query parameters
     */
    private QueryParam getQueryParam(UserArticleCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        StringBuilder sb = new StringBuilder("select ua.USA_ID_C, ua.USA_READDATE_D, ua.USA_STARREDDATE_D, f.FED_TITLE_C, fs.FES_ID_C, fs.FES_TITLE_C, a.ART_ID_C, a.ART_URL_C, a.ART_GUID_C, a.ART_TITLE_C, a.ART_CREATOR_C, a.ART_DESCRIPTION_C, a.ART_COMMENTURL_C, a.ART_COMMENTCOUNT_N, a.ART_ENCLOSUREURL_C, a.ART_ENCLOSURELENGTH_N, a.ART_ENCLOSURETYPE_C, a.ART_PUBLICATIONDATE_D");
        sb.append(" from T_ARTICLE a ");
        if (criteria.isVisible()) {
            sb.append(" join T_USER_ARTICLE ua on(a.ART_ID_C = ua.USA_IDARTICLE_C and ua.USA_IDUSER_C = :userId and ua.USA_DELETEDATE_D is null) ");
        } else {
            sb.append(" left join T_USER_ARTICLE ua on(a.ART_ID_C = ua.USA_IDARTICLE_C and ua.USA_IDUSER_C = :userId and ua.USA_DELETEDATE_D is null) ");
        }
        if (criteria.getUserId() != null) {
            sb.append(" join T_FEED f on(f.FED_ID_C = a.ART_IDFEED_C and f.FED_DELETEDATE_D is null) ");
        }
        sb.append(" left join T_FEED_SUBSCRIPTION fs on(fs.FES_IDFEED_C = f.FED_ID_C and fs.FES_IDUSER_C = :userId and fs.FES_DELETEDATE_D is null) ");
        
        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getFeedId() != null) {
            criteriaList.add("a.ART_IDFEED_C = :feedId");
            parameterMap.put("feedId", criteria.getFeedId());
        }
        if (criteria.getArticleId() != null) {
            criteriaList.add("a.ART_ID_C = :articleId");
            parameterMap.put("articleId", criteria.getArticleId());
        }
        if (criteria.getArticleIdIn() != null) {
            criteriaList.add("a.ART_ID_C IN :articleIdIn");
            parameterMap.put("articleIdIn", criteria.getArticleIdIn());
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
        parameterMap.put("userId", criteria.getUserId());
        criteriaList.add("a.ART_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        if (criteria.isStarred()) {
            sb.append(" order by ua.USA_STARREDDATE_D desc");
        } else {
            sb.append(" order by a.ART_PUBLICATIONDATE_D desc");
        }
        
        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        return queryParam;
    }
}
