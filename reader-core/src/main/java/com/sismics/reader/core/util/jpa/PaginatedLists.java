package com.sismics.reader.core.util.jpa;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.sismics.util.context.ThreadLocalContext;

/**
 * Utilities for paginated lists.
 * 
 * @author jtremeaux
 */
public class PaginatedLists {
    /**
     * Default size of a page.
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Maximum size of a page.
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Constructs a paginated list.
     * 
     * @param pageSize Size of the page
     * @param offset Offset of the page
     * @return Paginated list
     */
    public static <E> PaginatedList<E> create(Integer pageSize, Integer offset) {
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (offset == null) {
            offset = 0;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        return new PaginatedList<E>(pageSize, offset);
    }
    
    /**
     * Constructs a paginated list with default parameters.
     * 
     * @return Paginated list
     */
    public static <E> PaginatedList<E> create() {
        return create(null, null);
    }
    
    /**
     * Executes a native count(*) request to count the number of results.
     * 
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryString Query string
     * @param parameterMap Request parameters
     */
    private static <E> void executeCountQuery(PaginatedList<E> paginatedList, String queryString, Map<String, Object> parameterMap) {
        StringBuilder sb = new StringBuilder("select count(*) as result_count from (");
        sb.append(queryString);
        sb.append(") as t1");
        
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        for (Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        
        Number resultCount = (Number) q.getSingleResult();
        paginatedList.setResultCount(resultCount.intValue());
    }

    /**
     * Executes a query and returns the data of the currunt page.
     * 
     * @param em EntityManager
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryString Query string
     * @param parameterMap Request parameters
     * @return List of results
     */
    @SuppressWarnings("unchecked")
    private static <E> List<Object[]> executeResultQuery(PaginatedList<E> paginatedList, String queryString, Map<String, Object> parameterMap) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(queryString);
        for (Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        
        q.setFirstResult(paginatedList.getOffset());
        q.setMaxResults(paginatedList.getLimit());
        return q.getResultList();
    }
    
    /**
     * Executes a paginated request with 2 native queries (one to count the number of results, and one to return the page).
     * 
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryString Query string
     * @param parameterMap Request parameters
     * @return List of results
     */
    public static <E> List<Object[]> executePaginatedQuery(PaginatedList<E> paginatedList, String queryString, Map<String, Object> parameterMap) {
        executeCountQuery(paginatedList, queryString, parameterMap);
        return executeResultQuery(paginatedList, queryString, parameterMap);
    }

    /**
     * Executes a paginated request with 2 native queries (one to count the number of results, and one to return the page).
     * 
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryString Query string
     * @param parameterMap Request parameters
     * @param sortCriteria Sort criteria
     * @return List of results
     */
    public static <E> List<Object[]> executePaginatedQuery(PaginatedList<E> paginatedList, String queryString, Map<String, Object> parameterMap, SortCriteria sortCriteria) {
        StringBuilder sb = new StringBuilder(queryString);
        sb.append(" order by c");
        sb.append(sortCriteria.getColumn());
        sb.append(sortCriteria.isAsc() ? " asc" : " desc");
        String sortedQueryString = sb.toString();
        
        executeCountQuery(paginatedList, sortedQueryString, parameterMap);
        return executeResultQuery(paginatedList, sortedQueryString, parameterMap);
    }
}
