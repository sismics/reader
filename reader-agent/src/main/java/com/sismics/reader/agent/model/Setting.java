package com.sismics.reader.agent.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.google.common.io.Closer;
import com.sismics.util.EnvironmentUtil;
import com.sismics.util.MessageUtil;

/**
 * Reader settings.
 *
 * @author jtremeaux 
 */
public class Setting {
    /**
     * Reader properties file.
     */
    private static final String READER_AGENT_PROPERTIES_FILE = "reader-agent.properties";

    /**
     * Default host.
     */
    public static final String DEFAULT_HOST = "0.0.0.0";

    /**
     * Default port.
     */
    public static final int DEFAULT_PORT = 4001;
    
    /**
     * Default context path.
     */
    public static final String DEFAULT_CONTEXT_PATH = "/";
    
    /**
     * Default autostart.
     */
    public static final boolean DEFAULT_AUTO_START = true;
    
    /**
     * Host name.
     */
    private String host;
    
    /**
     * Port.
     */
    private int port;
    
    /**
     * Context path.
     */
    private String contextPath;
    
    /**
     * Reader home.
     */
    private String readerHome;
    
    /**
     * Start server automatically.
     */
    private boolean autoStart;
    
    /**
     * Constructor of Setting.
     */
    public Setting() {
        host = DEFAULT_HOST;
        port = DEFAULT_PORT;
        contextPath = DEFAULT_CONTEXT_PATH;
        autoStart = DEFAULT_AUTO_START;
        
        if (EnvironmentUtil.isWindows()) {
            readerHome = EnvironmentUtil.getWindowsAppData() + "\\Sismics\\Reader";
        } else if (EnvironmentUtil.isMacOs()) {
            readerHome = EnvironmentUtil.getMacOsUserHome() + "/Library/Sismics/Reader";
        } else {
            readerHome = ".";
        }
    }
    
    /**
     * Read settings from the properties file.
     */
    public void read() {
        Closer closer = Closer.create();
        try {
            Properties properties = new Properties();
            File file = new File(READER_AGENT_PROPERTIES_FILE);
            if (file.exists()) {
                InputStream is = closer.register(new FileInputStream(file));
                properties.load(is);
                
                host = properties.getProperty("reader.host", DEFAULT_HOST);
                port = Integer.valueOf(properties.getProperty("reader.port", String.valueOf(DEFAULT_PORT)));
                contextPath = properties.getProperty("reader.context_path", DEFAULT_CONTEXT_PATH);
                autoStart = Boolean.valueOf(properties.getProperty("reader.auto_start", String.valueOf(DEFAULT_AUTO_START)));
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                // NOP
            }
        }
    }

    /**
     * Save settings to the properties file.
     * 
     * @throws Exception
     */
    public void save() throws Exception {
        Closer closer = Closer.create();
        try {
            Properties properties = new Properties();
            properties.setProperty("reader.host", host);
            properties.setProperty("reader.port", String.valueOf(port));
            properties.setProperty("reader.context_path", contextPath);
            properties.setProperty("reader.auto_start", String.valueOf(autoStart));
            
            File file = new File(READER_AGENT_PROPERTIES_FILE);
            OutputStream os = closer.register(new FileOutputStream(file));
            properties.store(os, "Reader settings");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new Exception(MessageUtil.getMessage("agent.setting.error", READER_AGENT_PROPERTIES_FILE));
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                // NOP
            }
        }
    }

    /**
     * Getter of host.
     *
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Setter of host.
     *
     * @param host host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Getter of port.
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Setter of port.
     *
     * @param port port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Getter of contextPath.
     *
     * @return contextPath
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Setter of contextPath.
     *
     * @param contextPath contextPath
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Getter of readerHome.
     *
     * @return readerHome
     */
    public String getReaderHome() {
        return readerHome;
    }

    /**
     * Setter of readerHome.
     *
     * @param readerHome readerHome
     */
    public void setReaderHome(String readerHome) {
        this.readerHome = readerHome;
    }

    /**
     * Getter of autoStart.
     *
     * @return autoStart
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Setter of autoStart.
     *
     * @param autoStart autoStart
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
}
