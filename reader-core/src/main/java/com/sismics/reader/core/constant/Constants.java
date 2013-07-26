package com.sismics.reader.core.constant;

/**
 * Application constants.
 * 
 * @author jtremeaux
 */
public class Constants {
    /**
     * Default locale.
     */
    public static final String DEFAULT_LOCALE_ID = "en";

    /**
     * Default timezone ID.
     */
    public static final String DEFAULT_TIMEZONE_ID = "Europe/London";
    
    /**
     * Default theme ID.
     */
    public static final String DEFAULT_THEME_ID = "default.less";
    
    /**
     * Administrator's default password ("admin").
     */
    public static final String DEFAULT_ADMIN_PASSWORD = "$2a$05$6Ny3TjrW3aVAL1or2SlcR.fhuDgPKp5jp.P9fBXwVNePgeLqb4i3C";

    /**
     * RAM Lucene directory storage.
     */
    public static final String LUCENE_DIRECTORY_STORAGE_RAM = "RAM";
    
    /**
     * File Lucene directory storage.
     */
    public static final String LUCENE_DIRECTORY_STORAGE_FILE = "FILE";
    
    /**
     * Default generic user role.
     */
    public static final String DEFAULT_USER_ROLE = "user";
    
    /**
     * Import Job.
     */
    public static final String JOB_IMPORT = "import";
    
    /**
     * Import Job : number of feeds event.
     */
    public static final String JOB_EVENT_FEED_COUNT = "import.feed_count";
    
    /**
     * Import Job : number of starred articles event.
     */
    public static final String JOB_EVENT_STARRED_ARTICLED_COUNT = "import.starred_article_count";
    
    /**
     * Import Job : feed import success.
     */
    public static final String JOB_EVENT_FEED_IMPORT_SUCCESS = "import.feed_import_success";
    
    /**
     * Import Job : feed import failure.
     */
    public static final String JOB_EVENT_FEED_IMPORT_FAILURE = "import.feed_import_failure";
    
    /**
     * Import Job : starred article import success.
     */
    public static final String JOB_EVENT_STARRED_ARTICLE_IMPORT_SUCCESS = "import.starred_article_import_success";
    
    /**
     * Import Job : starred article failure.
     */
    public static final String JOB_EVENT_STARRED_ARTICLE_IMPORT_FAILURE = "import.starred_article_import_failure";
}
