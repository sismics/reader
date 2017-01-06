package com.sismics.util;

/**
 * Environment properties utilities.
 *
 * @author jtremeaux 
 */
public class EnvironmentUtil {

    private static String OS = System.getProperty("os.name").toLowerCase();
    
    private static String TEST_ENV = System.getProperty("test");

    private static String WINDOWS_APPDATA = System.getenv("APPDATA");

    private static String MAC_OS_USER_HOME = System.getProperty("user.home");
    
    private static String READER_HOME = System.getProperty("reader.home");

    private static String APPLICATION_LOG_ENABLED = System.getProperty("application.log.enabled");

    private static String SSL_TRUST_ALL = System.getProperty("ssl.trust.all");

    private static String HIBERNATE_PROPERTIES = System.getProperty("hibernate.properties");

    /**
     * In a web application context.
     */
    private static boolean webappContext;
    
    /**
     * Returns true if running under Microsoft Windows.
     * 
     * @return Running under Microsoft Windows
     */
    public static boolean isWindows() {
        return OS.indexOf("win") >= 0;
    }

    /**
     * Returns true if running under Mac OS.
     * 
     * @return Running under Mac OS
     */
    public static boolean isMacOs() {
        return OS.indexOf("mac") >= 0;
    }

    /**
     * Returns true if running under UNIX.
     * 
     * @return Running under UNIX
     */
    public static boolean isUnix() {
        return OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0;
    }
    
    /**
     * Returns true if we are in a unit testing environment.
     * 
     * @return Unit testing environment
     */
    public static boolean isUnitTest() {
        return !webappContext ||
                TEST_ENV != null && "true".equals(TEST_ENV);
    }

    /**
     * Returns the MS Windows AppData directory of this user.
     * 
     * @return AppData directory
     */
    public static String getWindowsAppData() {
        return WINDOWS_APPDATA;
    }

    /**
     * Returns the Mac OS home directory of this user.
     * 
     * @return Home directory
     */
    public static String getMacOsUserHome() {
        return MAC_OS_USER_HOME;
    }

    /**
     * Returns the home directory of Reader (e.g. /var/reader).
     * 
     * @return Home directory
     */
    public static String getReaderHome() {
        return READER_HOME;
    }

    /**
     * Returns the location of externale hibernate.properties.
     *
     * @return Location of externale hibernate.properties
     */
    public static String getHibernateProperties() {
        return HIBERNATE_PROPERTIES;
    }

    /**
     * Returns true if an additional application log is enabled.
     *
     * @return Condition
     */
    public static boolean isApplicationLogEnabled() {
        return APPLICATION_LOG_ENABLED == null || !"false".equals(APPLICATION_LOG_ENABLED);
    }

    /**
     * Returns true if all SSL certificates are trusted (insecure).
     *
     * @return Condition
     */
    public static boolean isSslTrustAll() {
        return SSL_TRUST_ALL == null || !"false".equals(SSL_TRUST_ALL);
    }

    /**
     * Getter of webappContext.
     *
     * @return webappContext
     */
    public static boolean isWebappContext() {
        return webappContext;
    }

    /**
     * Setter of webappContext.
     *
     * @param webappContext webappContext
     */
    public static void setWebappContext(boolean webappContext) {
        EnvironmentUtil.webappContext = webappContext;
    }
}
