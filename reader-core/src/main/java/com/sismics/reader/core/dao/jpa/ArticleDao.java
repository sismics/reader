package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.mapper.ArticleMapper;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.DialectUtil;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * Article DAO.
 * 
 * @author jtremeaux
 */
public class ArticleDao extends BaseDao<ArticleDto, ArticleCriteria> {

    @Override
    protected QueryParam getQueryParam(ArticleCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select a.ART_ID_C, a.ART_URL_C, a.ART_GUID_C, a.ART_TITLE_C, a.ART_CREATOR_C, a.ART_DESCRIPTION_C, a.ART_COMMENTURL_C, a.ART_COMMENTCOUNT_N, a.ART_ENCLOSUREURL_C, a.ART_ENCLOSURELENGTH_N, a.ART_ENCLOSURETYPE_C, a.ART_PUBLICATIONDATE_D, a.ART_CREATEDATE_D, a.ART_IDFEED_C ")
                .append("  from T_ARTICLE a ");

        // Adds search criteria
        criteriaList.add("a.ART_DELETEDATE_D is null");
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
        if (criteria.getPublicationDateMin() != null) {
            criteriaList.add("a.ART_PUBLICATIONDATE_D > :publicationDateMax");
            parameterMap.put("publicationDateMax", criteria.getPublicationDateMin());
        }
        if (criteria.getFeedId() != null) {
            criteriaList.add("a.ART_IDFEED_C = :feedId");
            parameterMap.put("feedId", criteria.getFeedId());
        }

        SortCriteria sortCriteria = new SortCriteria("  order by a.ART_CREATEDATE_D asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new ArticleMapper());
    }

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
        Query q = em.createNativeQuery("insert into T_ARTICLE(ART_ID_C, ART_IDFEED_C, ART_URL_C, ART_BASEURI_C, ART_GUID_C, ART_TITLE_C, ART_CREATOR_C, ART_DESCRIPTION_C, ART_COMMENTURL_C, ART_COMMENTCOUNT_N, ART_ENCLOSUREURL_C, ART_ENCLOSURELENGTH_N, ART_ENCLOSURETYPE_C, ART_PUBLICATIONDATE_D, ART_CREATEDATE_D)" +
                "  values (:id, :feedId, :url, :baseUri, :guid, :title, :creator, :description, :commentUrl, " + DialectUtil.getNullParameter(":commentCount", article.getCommentCount())+ ", :enclosureUrl, " + DialectUtil.getNullParameter(":enclosureLength", article.getEnclosureLength())+ ", :enclosureType, :publicationDate, :createDate)")
                .setParameter("id", article.getId())
                .setParameter("feedId", article.getFeedId())
                .setParameter("url", article.getUrl())
                .setParameter("baseUri", article.getBaseUri())
                .setParameter("guid", article.getGuid())
                .setParameter("title", article.getTitle())
                .setParameter("creator", article.getCreator())
                .setParameter("description", article.getDescription())
                .setParameter("commentUrl", article.getCommentUrl())
                .setParameter("enclosureUrl", article.getEnclosureUrl())
                .setParameter("enclosureType", article.getEnclosureType())
                .setParameter("publicationDate", article.getPublicationDate())
                .setParameter("createDate", article.getCreateDate());
        if (article.getCommentCount() != null) {
            q.setParameter("commentCount", article.getCommentCount());
        }
        if (article.getEnclosureLength() != null) {
            q.setParameter("enclosureLength", article.getEnclosureLength());
        }
        q.executeUpdate();

        return article.getId();
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
        Query q = em.createNativeQuery("update T_ARTICLE set" +
                "  ART_URL_C = :url," +
                "  ART_TITLE_C = :title," +
                "  ART_CREATOR_C = :creator," +
                "  ART_DESCRIPTION_C = :description," +
                "  ART_COMMENTURL_C = :commentUrl," +
                "  ART_COMMENTCOUNT_N = " + DialectUtil.getNullParameter(":commentCount", article.getCommentCount())+ "," +
                "  ART_ENCLOSUREURL_C = :enclosureUrl," +
                "  ART_ENCLOSURELENGTH_N = " + DialectUtil.getNullParameter(":enclosureLength", article.getEnclosureLength())+ "," +
                "  ART_ENCLOSURETYPE_C = :enclosureType" +
                "  where ART_ID_C = :id and ART_DELETEDATE_D is null")
                .setParameter("url", article.getUrl())
                .setParameter("title", article.getTitle())
                .setParameter("creator", article.getCreator())
                .setParameter("description", article.getDescription())
                .setParameter("commentUrl", article.getCommentUrl())
                .setParameter("enclosureUrl", article.getEnclosureUrl())
                .setParameter("enclosureType", article.getEnclosureType())
                .setParameter("id", article.getId());
        if (article.getCommentCount() != null) {
            q.setParameter("commentCount", article.getCommentCount());
        }
        if (article.getEnclosureLength() != null) {
            q.setParameter("enclosureLength", article.getEnclosureLength());
        }
        q.executeUpdate();

        return article;
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
        Date deleteDate = new Date();
        em.createNativeQuery("update T_ARTICLE set ART_DELETEDATE_D = :deleteDate where ART_ID_C = :id and ART_DELETEDATE_D is null")
                .setParameter("deleteDate", deleteDate)
                .setParameter("id", id)
                .executeUpdate();
        em.createNativeQuery("update T_USER_ARTICLE set USA_DELETEDATE_D = :deleteDate where USA_IDARTICLE_C = :articleId and USA_DELETEDATE_D is null")
                .setParameter("deleteDate", deleteDate)
                .setParameter("articleId", id)
                .executeUpdate();
    }
}
