package com.sismics.reader.core.dao.jpa;

import com.google.common.base.Joiner;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.Map.Entry;

/**
 * Article DAO.
 * 
 * @author jtremeaux
 */
public class ArticleDao {
    /**
     * Creates a new article.
     * 
     * @param article Article to create
     * @return New ID
     */
    public String create(Article article) {
        // Create the UUID
        article.setId(UUID.randomUUID().toString());
        article.setCreateDate(new Date());

        // Create the article
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery("insert into T_ARTICLE(ART_ID_C, ART_IDFEED_C, ART_URL_C, ART_BASEURI_C, ART_GUID_C, ART_TITLE_C, ART_CREATOR_C, ART_DESCRIPTION_C, ART_COMMENTURL_C, ART_COMMENTCOUNT_N, ART_ENCLOSUREURL_C, ART_ENCLOSURELENGTH_N, ART_ENCLOSURETYPE_C, ART_PUBLICATIONDATE_D, ART_CREATEDATE_D)" +
                "  values (:id, :feedId, :url, :baseUri, :guid, :title, :creator, :description, :commentUrl, :commentCount, :enclosureUrl, :enclosureLength, :enclosureType, :publicationDate, :createDate)")
                .setParameter("id", article.getId())
                .setParameter("feedId", article.getFeedId())
                .setParameter("url", article.getUrl())
                .setParameter("baseUri", article.getBaseUri())
                .setParameter("guid", article.getGuid())
                .setParameter("title", article.getTitle())
                .setParameter("creator", article.getCreator())
                .setParameter("description", article.getDescription())
                .setParameter("commentUrl", article.getCommentUrl())
                .setParameter("commentCount", article.getCommentCount())
                .setParameter("enclosureUrl", article.getEnclosureUrl())
                .setParameter("enclosureLength", article.getEnclosureLength())
                .setParameter("enclosureType", article.getEnclosureType())
                .setParameter("publicationDate", article.getPublicationDate())
                .setParameter("createDate", article.getCreateDate())
                .executeUpdate();

        return article.getId();
    }
    
    /**
     * Returns the list of all articles.
     * 
     * @return List of articles
     */
    @SuppressWarnings("unchecked")
    public List<Article> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select a from Article a where a.deleteDate is null order by a.id");
        return q.getResultList();
    }
    
    /**
     * Deletes a article.
     * 
     * @param id Article ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the article
        Query q = em.createQuery("select a from Article a where a.id = :id and a.deleteDate is null");
        q.setParameter("id", id);
        Article articleFromDb = (Article) q.getSingleResult();

        // Delete the article
        articleFromDb.setDeleteDate(new Date());
    }

    /**
     * Searches articles by criteria, and returns the first occurence.
     *
     * @param criteria Search criteria
     * @return Article
     */
    public ArticleDto findFirstByCriteria(ArticleCriteria criteria) {
        List<ArticleDto> articleList = findByCriteria(criteria);
        if (!articleList.isEmpty()) {
            return articleList.iterator().next();
        }
        return null;
    }

    /**
     * Searches articles by criteria.
     * 
     * @param criteria Search criteria
     * @return List of articles
     */
    @SuppressWarnings("unchecked")
    public List<ArticleDto> findByCriteria(ArticleCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        StringBuilder sb = new StringBuilder("select a.ART_ID_C, a.ART_URL_C, a.ART_GUID_C, a.ART_TITLE_C, a.ART_CREATOR_C, a.ART_DESCRIPTION_C, a.ART_COMMENTURL_C, a.ART_COMMENTCOUNT_N, a.ART_ENCLOSUREURL_C, a.ART_ENCLOSURELENGTH_N, a.ART_ENCLOSURETYPE_C, a.ART_PUBLICATIONDATE_D, a.ART_IDFEED_C ");
        sb.append(" from T_ARTICLE a ");
        
        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getId() != null) {
            criteriaList.add("a.ART_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getGuidIn() != null) {
            criteriaList.add("a.ART_GUID_C in :guidIn");
            parameterMap.put("guidIn", criteria.getGuidIn());
        }
        if (criteria.getTitle() != null) {
            criteriaList.add("a.ART_TITLE_C = :title");
            parameterMap.put("title", criteria.getTitle());
        }
        if (criteria.getUrl() != null) {
            criteriaList.add("a.ART_URL_C = :url");
            parameterMap.put("url", criteria.getUrl());
        }
        if (criteria.getFeedId() != null) {
            criteriaList.add("a.ART_IDFEED_C = :feedId");
            parameterMap.put("feedId", criteria.getFeedId());
        }
        criteriaList.add("a.ART_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        sb.append(" order by a.ART_CREATEDATE_D asc");
        
        // Search
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        for (Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> resultList = q.getResultList();
        
        // Assemble results
        List<ArticleDto> articleDtoList = new ArrayList<ArticleDto>();
        for (Object[] o : resultList) {
            int i = 0;
            ArticleDto articleDto = new ArticleDto();
            articleDto.setId((String) o[i++]);
            articleDto.setUrl((String) o[i++]);
            articleDto.setGuid((String) o[i++]);
            articleDto.setTitle((String) o[i++]);
            articleDto.setCreator((String) o[i++]);
            articleDto.setDescription((String) o[i++]);
            articleDto.setCommentUrl((String) o[i++]);
            articleDto.setCommentCount((Integer) o[i++]);
            articleDto.setEnclosureUrl((String) o[i++]);
            articleDto.setEnclosureCount((Integer) o[i++]);
            articleDto.setEnclosureType((String) o[i++]);
            articleDto.setPublicationDate((Date) o[i++]);
            articleDto.setFeedId((String) o[i++]);
            articleDtoList.add(articleDto);
        }
        return articleDtoList;
    }

    /**
     * Updates a article.
     * 
     * @param article Article to update
     * @return Updated article
     */
    public Article update(Article article) {
        // Get the article
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery("update T_ARTICLE set" +
                "  ART_URL_C = :url," +
                "  ART_TITLE_C = :title," +
                "  ART_CREATOR_C = :creator," +
                "  ART_DESCRIPTION_C = :description," +
                "  ART_COMMENTURL_C = :commentUrl," +
                "  ART_COMMENTCOUNT_N = :commentCount," +
                "  ART_ENCLOSUREURL_C = :enclosureUrl," +
                "  ART_ENCLOSURELENGTH_N = :enclosureLength," +
                "  ART_ENCLOSURETYPE_C = :enclosureType" +
                "  where ART_ID_C = :id and ART_DELETEDATE_D is null")
                .setParameter("url", article.getUrl())
                .setParameter("title", article.getTitle())
                .setParameter("creator", article.getCreator())
                .setParameter("description", article.getDescription())
                .setParameter("commentUrl", article.getCommentUrl())
                .setParameter("commentCount", article.getCommentCount())
                .setParameter("enclosureUrl", article.getEnclosureUrl())
                .setParameter("enclosureLength", article.getEnclosureLength())
                .setParameter("enclosureType", article.getEnclosureType())
                .setParameter("id", article.getId())
                .executeUpdate();

        return article;
    }
}
