package com.sismics.util;

/**
 * Environment properties utilities.
 *
 * @author jtremeaux 
 */
public class EnvironmentUtil {

    private static String OS_NAME = System.getProperty("os.name").toLowerCase();
    
    private static String OS_VERSION = System.getProperty("os.version");
    
    private static String WINDOWS_APPDATA = System.getenv("APPDATA");

    private static String MAC_OS_USER_HOME = System.getProperty("user.home");
    
    private static String READER_HOME = System.getProperty("reader.home");

    /**
     * Returns true if running under Microsoft Windows.
     * 
     * @return Running under Microsoft Windows
     */
    public static boolean isWindows() {
        return OS_NAME.indexOf("win") >= 0;
    }

    /**
     * Returns true if running under Mac OS_NAME.
     * 
     * @return Running under Mac OS_NAME
     */
    public static boolean isMacOs() {
        return OS_NAME.indexOf("mac") >= 0;
    }

    /**
     * Returns true if running under UNIX.
     * 
     * @return Running under UNIX
     */
    public static boolean isUnix() {
        return OS_NAME.indexOf("nix") >= 0 || OS_NAME.indexOf("nux") >= 0 || OS_NAME.indexOf("aix") > 0;
    }
    
    /**
     * Returns the OS version.
     * 
     * @return OS version
     */
    public static String getOsVersion() {
        return OS_VERSION;
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
     * Returns the Mac OS_NAME home directory of this user.
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
}
