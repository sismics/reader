package com.sismics.util;

/**
 * Environment properties utilities.
 *
 * @author jtremeaux 
 */
public class EnvironmentUtil {

    private static String OS = System.getProperty("os.name").toLowerCase();
    
    private static String SISMICS_READER_ENV = System.getProperty("sismicsreader_env");

    private static String WINDOWS_APPDATA = System.getenv("APPDATA");

    private static String MAC_OS_USER_HOME = System.getProperty("user.home");

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
     * Returns true if we are in a development environment.
     * 
     * @return Development environment
     */
    public static boolean isDev() {
        return SISMICS_READER_ENV != null && "dev".equals(SISMICS_READER_ENV);
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
}
