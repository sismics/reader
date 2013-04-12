package com.sismics.util.jpa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.reader.core.util.DirectoryUtil;
import com.sismics.util.EnvironmentUtil;

/**
 * Entity manager factory.
 * 
 * @author jtremeaux
 */
public final class EMF {
    private static final Logger log = LoggerFactory.getLogger(EMF.class);

    private static EntityManagerFactory emfInstance;

    static {
        try {
            emfInstance = Persistence.createEntityManagerFactory("transactions-optional", getEntityManagerProperties());
        } catch (Throwable t) {
            log.error("Error creating EMF", t);
        }
    }
    
    private static Map<Object, Object> getEntityManagerProperties() {
        // Use properties file if exists
        try {
            URL hibernatePropertiesUrl = EMF.class.getResource("/hibernate.properties");
            InputStream is = hibernatePropertiesUrl.openStream();
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        } catch (IOException e) {
            log.error("Error reading hibernate.properties", e);
        }
        
        // Use environment parameters
        Map<Object, Object> props = new HashMap<>();
        props.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        File dbDirectory = DirectoryUtil.getDbDirectory();
        String dbFile = dbDirectory.getAbsoluteFile() + File.separator + "reader";
        props.put("hibernate.connection.url", "jdbc:hsqldb:file:" + dbFile + ";hsqldb.write_delay=false");
        props.put("hibernate.connection.username", "sa");
        if (EnvironmentUtil.isDev()) {
            props.put("hibernate.hbm2ddl.auto", "create-drop");
        } else {
            props.put("hibernate.hbm2ddl.auto", "validate");
        }
        props.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        if (EnvironmentUtil.isDev()) {
            props.put("hibernate.show_sql", "true");
        }
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.max_fetch_depth", "5");
        props.put("hibernate.cache.use_second_level_cache", "false");
        return props;
    }
    
    /**
     * Private constructor.
     */
    private EMF() {
    }

    /**
     * Returns an instance of EMF.
     * 
     * @return Instance of EMF
     */
    public static EntityManagerFactory get() {
        return emfInstance;
    }
}