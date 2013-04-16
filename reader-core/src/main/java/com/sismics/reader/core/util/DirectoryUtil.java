package com.sismics.reader.core.util;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import com.sismics.util.EnvironmentUtil;

/**
 * Utilities to gain access to the storage directories used by the application.
 * 
 * @author jtremeaux
 */
public class DirectoryUtil {
    /**
     * Returns the base data directory.
     * 
     * @return Base data directory
     */
    public static File getBaseDataDirectory() {
        File baseDataDir = null;
        if (EnvironmentUtil.getWebappRoot() != null) {
            // We are in a webapp environment
            if (StringUtils.isNotBlank(EnvironmentUtil.getReaderHome())) {
                // If the reader.home property is set then use it
                baseDataDir = new File(EnvironmentUtil.getReaderHome());
                if (!baseDataDir.isDirectory()) {
                    baseDataDir.mkdirs();
                }
            } else if (EnvironmentUtil.isDev()) {
                // Use the base of the Webapp directory
                baseDataDir = new File(EnvironmentUtil.getWebappRoot() + File.separator + "sismicsreader");
                if (!baseDataDir.isDirectory()) {
                    baseDataDir.mkdirs();
                }
            } else {
                // Use the OS-dependant app directory
                if (EnvironmentUtil.isWindows()) {
                    baseDataDir = new File(EnvironmentUtil.getWindowsAppData() + File.separator + "Sismics" + File.separator + "Reader");
                    if (!baseDataDir.isDirectory()) {
                        baseDataDir.mkdirs();
                    }
                } else if (EnvironmentUtil.isUnix()) {
                    baseDataDir = new File("/var/reader");
                    if (!baseDataDir.isDirectory()) {
                        baseDataDir.mkdirs();
                    }
                } else if (EnvironmentUtil.isMacOs()) {
                    baseDataDir = new File(EnvironmentUtil.getMacOsUserHome() + "/Library/Application Support/Sismics Reader");
                    if (!baseDataDir.isDirectory()) {
                        baseDataDir.mkdirs();
                    }
                } 
            }
        }
        if (baseDataDir == null) {
            // Or else (for unit testing), use a temporary directory
            baseDataDir = new File(System.getProperty("java.io.tmpdir"));
        }
        
        return baseDataDir;
    }
    
    /**
     * Returns the database directory.
     * 
     * @return Database directory.
     */
    public static File getDbDirectory() {
        return getDataSubDirectory("db");
    }

    /**
     * Returns the favicons directory.
     * 
     * @return Favicons directory.
     */
    public static File getFaviconDirectory() {
        return getDataSubDirectory("favicon");
    }

    /**
     * Returns the lucene indexes directory.
     * 
     * @return Lucene indexes directory.
     */
    public static File getLuceneDirectory() {
        return getDataSubDirectory("lucene");
    }

    /**
     * Returns the themes directory.
     * 
     * @return Theme directory.
     */
    public static File getThemeDirectory() {
        String webappRoot = EnvironmentUtil.getWebappRoot();
        File themeDir = null;
        if (webappRoot != null) {
            themeDir = new File(webappRoot + File.separator + "stylesheets" + File.separator + "theme");
        } else {
            themeDir = new File(DirectoryUtil.class.getResource("/stylesheets/theme").getFile());
        }
        if (themeDir != null && themeDir.isDirectory()) {
            return themeDir;
        }
        return null;
    }

    /**
     * Returns a subdirectory of the base data directory
     * 
     * @return Subdirectory
     */
    private static File getDataSubDirectory(String subdirectory) {
        File baseDataDir = getBaseDataDirectory();
        File faviconDirectory = new File(baseDataDir.getPath() + File.separator + subdirectory);
        if (!faviconDirectory.isDirectory()) {
            faviconDirectory.mkdirs();
        }
        return faviconDirectory;
    }
}
