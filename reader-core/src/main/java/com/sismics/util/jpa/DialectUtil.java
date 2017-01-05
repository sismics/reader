package com.sismics.util.jpa;

/**
 * @author jtremeaux
 */
public class DialectUtil {
    /**
     * Checks if the error from the drivers relates to an object not found.
     *
     * @param message Error message
     * @return Object not found
     */
    public static boolean isObjectNotFound(String message) {
        return EMF.isDriverHsql() && message.contains("object not found") ||
                EMF.isDriverPostgresql() && message.contains("does not exist");
    }


    /**
     * Transform SQL dialect to current dialect.
     *
     * @param sql SQL to transform
     * @return Transformed SQL
     */
    public static String transform(String sql) {
        if (EMF.isDriverPostgresql()) {
            sql = transformToPostgresql(sql);
        }
        return sql;
    }

    /**
     * Transform SQL from HSQLDB dialect to current dialect.
     *
     * @param sql SQL to transform
     * @return Transformed SQL
     */
    public static String transformToPostgresql(String sql) {
        sql = sql.replaceAll("(cached|memory) table", "table");
        sql = sql.replaceAll("datetime", "timestamp");
        sql = sql.replaceAll("longvarchar", "text");
        sql = sql.replaceAll("bit not null", "bool not null");
        sql = sql.replaceAll("bit default 0", "bool default false");
        return sql;
    }

    /**
     * Returns the difference between 2 dates A and B.
     *
     * @param field The first date A
     * @param diff The second date B
     * @param unit The unit (e.g. MINUTE)
     * @return The difference
     */
    public static String getDateDiff(String field, String diff, String unit) {
        if (EMF.isDriverHsql()) {
            return "DATE_SUB(" + field + ", INTERVAL " + diff + " " + unit + ")";
        } else if (EMF.isDriverPostgresql()) {
            return field + " - (" + diff + " * interval '1 " + unit + "')";
        } else {
            throw new RuntimeException("Unknown DB: " + EMF.getDriver());
        }
    }

    /**
     * Return a timestamp in miliseconds.
     *
     * @param value The time
     * @return The timestamp
     */
    public static String getTimeStamp(String value) {
        if (EMF.isDriverHsql()) {
            return "TIMESTAMP(" + value + ")";
        } else if (EMF.isDriverPostgresql()) {
            return "to_char(" + value + " at time zone 'UTC', 'YYYY-MM-DD HH24:MI:SS.MS')";
        } else {
            throw new RuntimeException("Unknown DB: " + EMF.getDriver());
        }
    }

    /**
     * Return the name of bound parameter for JPA, or null.
     * Workaround for http://stackoverflow.com/questions/8211195/postgresql-jdbc-null-string-taken-as-a-bytea
     *
     * @param parameterName Bound parameter name
     * @param value Value
     * @return SQL clause
     */
    public static String getNullParameter(String parameterName, Object value) {
        if (value == null) {
            return "null";
        } else {
            return  parameterName;
        }
    }
}
