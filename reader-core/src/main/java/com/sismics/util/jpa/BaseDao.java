package com.sismics.util.jpa;

import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.jpa.filter.FilterCriteria;

import java.util.List;

/**
 * Base DAO.
 * 
 * @author jtremeaux
 */
public abstract class BaseDao<T, C> {
    /**
     * Search items by criteria.
     *
     * @param list Paginated list (updated by side effects)
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @param filterCriteria Filter criteria
     */
    public void findByCriteria(PaginatedList<T> list, C criteria, SortCriteria sortCriteria, FilterCriteria filterCriteria) {
        PaginatedLists.executePaginatedQuery(list, getQueryParam(criteria, filterCriteria), sortCriteria);
    }

    /**
     * Search items by criteria.
     *
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @param filterCriteria Filter criteria
     */
    public List<T> findByCriteria(C criteria, SortCriteria sortCriteria, FilterCriteria filterCriteria) {
        QueryParam queryParam = getQueryParam(criteria, filterCriteria);
        if (sortCriteria != null) {
            queryParam.setSortCriteria(sortCriteria);
        }
        return PaginatedLists.executeQuery(queryParam);
    }

    /**
     * Search items by criteria.
     *
     * @param criteria Search criteria
     */
    public List<T> findByCriteria(C criteria) {
        return findByCriteria(criteria, null, null);
    }

    /**
     * Search items by criteria.
     *
     * @param criteria Search criteria
     */
    public T findFirstByCriteria(C criteria) {
        List<T> list = PaginatedLists.executeQuery(getQueryParam(criteria, null));
        return !list.isEmpty() ? list.iterator().next() : null;
    }

    protected abstract QueryParam getQueryParam(C criteria, FilterCriteria filterCriteria);
}
