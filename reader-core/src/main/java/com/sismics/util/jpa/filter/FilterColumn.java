package com.sismics.util.jpa.filter;

/**
 * Filter to apply on a column.
 *
 * @author jtremeaux
 */
public abstract class FilterColumn {
    /**
     * Column to filter.
     */
    protected String column;

    /**
     * Filter expression to apply.
     */
    protected String filter;

    public FilterColumn(String column, String filter) {
        this.column = column;
        this.filter = filter;
    }

    public String getColumn() {
        return column;
    }

    public String getFilter() {
        return filter;
    }

    public abstract String getPredicate();

    public abstract Object getParamValue();

    public String getParamName() {
        return "filtercolumn_" + column;
    }

    public boolean hasParam() {
        return column != null;
    }
}
