package com.sismics.util.jpa.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtering criteria of a query.
 *
 * @author jtremeaux
 */
public class FilterCriteria {
    /**
     * Columns to sort.
     */
    private List<FilterColumn> filterColumnList = new ArrayList<FilterColumn>();

    /**
     * Constructor of SortCriteria.
     *
     * @param filterColumnList List of sort columns
     */
    public FilterCriteria(List<FilterColumn> filterColumnList) {
        this.filterColumnList = filterColumnList;
    }

    public List<FilterColumn> getFilterColumnList() {
        return filterColumnList;
    }
}