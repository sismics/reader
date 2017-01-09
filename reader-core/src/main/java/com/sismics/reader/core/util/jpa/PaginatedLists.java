package com.sismics.reader.core.util.jpa;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterColumn;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

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
        if (pageSize == 0) {
            pageSize = 1; // Page size of zero counterintuitively returns all rows, we don't want to kill the database
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
     * Executes a non paginated query.
     *
     * @param queryParam Query parameters
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> executeQuery(QueryParam queryParam) {
        StringBuilder sb = new StringBuilder(getQueryString(queryParam));
        if (queryParam.getSortCriteria() != null) {
            sb.append(getOrderByClause(queryParam.getSortCriteria()));
        }

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery(sb.toString());
        mapQueryParam(query, queryParam);
        mapFilterColumn(query, queryParam);

        List<E> resultList = query.getResultList();
        if (queryParam.getResultMapper() != null) {
            return queryParam.getResultMapper().map(resultList);
        } else {
            return resultList;
        }
    }

    private static String getQueryString(QueryParam queryParam) {
        StringBuilder sb = new StringBuilder(queryParam.getQueryString());

        List<String> whereList = Lists.newLinkedList(queryParam.getCriteriaList());
        if (queryParam.getFilterCriteria() != null && !queryParam.getFilterCriteria().getFilterColumnList().isEmpty()) {
            for (FilterColumn filterColumn : queryParam.getFilterCriteria().getFilterColumnList()) {
                whereList.add(filterColumn.getPredicate());
            }
        }
        if (!whereList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(whereList));
        }
        if (queryParam.getGroupByList() != null && !queryParam.getGroupByList().isEmpty()) {
            sb.append(" group by ");
            sb.append(Joiner.on(", ").join(queryParam.getGroupByList()));
        }

        return sb.toString();
    }

    /**
     * Executes a native count(*) request to count the number of results.
     * 
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam Query parameters
     */
    private static <E> void executeCountQuery(PaginatedList<E> paginatedList, QueryParam queryParam) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery(getNativeCountQuery(queryParam));
        mapQueryParam(query, queryParam);
        mapFilterColumn(query, queryParam);
        Number resultCount = (Number) query.getSingleResult();
        paginatedList.setResultCount(resultCount.intValue());
    }

    /**
     * Returns the native query to count the number of records.
     * The initial query must be of the form "select xx from yy".
     *
     * @param queryParam Query parameters
     * @return Count query
     */
    private static String getNativeCountQuery(QueryParam queryParam) {
        return "select count(*) as result_count from (" +
                getQueryString(queryParam) +
                ") as t1";
    }

    /**
     * Executes a query and returns the data of the current page.
     * 
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam Query parameters
     */
    @SuppressWarnings("unchecked")
    private static <E> void executeResultQuery(PaginatedList<E> paginatedList, QueryParam queryParam) {
        StringBuilder sb = new StringBuilder(getQueryString(queryParam));
        if (queryParam.getSortCriteria() != null) {
            sb.append(getOrderByClause(queryParam.getSortCriteria()));
        }

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery(sb.toString());
        mapQueryParam(query, queryParam);
        mapFilterColumn(query, queryParam);

        query.setFirstResult(paginatedList.getOffset());
        query.setMaxResults(paginatedList.getLimit());

        List<E> resultList = query.getResultList();
        if (queryParam.getResultMapper() != null) {
            paginatedList.setResultList(queryParam.getResultMapper().map(resultList));
        } else {
            paginatedList.setResultList(resultList);
        }
    }
    
    /**
     * Executes a paginated request with 2 native queries (one to count the number of results, and one to return the page).
     *
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam Query parameters
     * @param sortCriteria Sort criteria
     */
    public static <E> void executePaginatedQuery(PaginatedList<E> paginatedList, QueryParam queryParam, SortCriteria sortCriteria) {
        if (sortCriteria != null) {
            queryParam.setSortCriteria(sortCriteria);
        }
        executeCountQuery(paginatedList, queryParam);
        executeResultQuery(paginatedList, queryParam);
    }

    /**
     * Get the order by clause from the sort criteria.
     *
     * @param sortCriteria Sort criteria
     * @return Order by clause
     */
    private static String getOrderByClause(SortCriteria sortCriteria) {
        String sortQuery = sortCriteria.getSortQuery();
        if (sortQuery != null) {
            return sortQuery;
        } else {
            return " order by c" +
                    sortCriteria.getColumn() +
                    (sortCriteria.isAsc() ? " asc" : " desc");
        }
    }

    private static void mapQueryParam(Query query, QueryParam queryParam) {
        for (Map.Entry<String, Object> parameter : queryParam.getParameterMap().entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
    }

    private static void mapFilterColumn(Query query, QueryParam queryParam) {
        if (queryParam.getFilterCriteria() != null) {
            for (FilterColumn filterColumn : queryParam.getFilterCriteria().getFilterColumnList()) {
                if (filterColumn.hasParam()) {
                    query.setParameter(filterColumn.getParamName(), filterColumn.getParamValue());
                }
            }
        }
    }
}
