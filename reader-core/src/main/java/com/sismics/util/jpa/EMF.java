package com.sismics.util.jpa;

import com.sismics.reader.core.util.DirectoryUtil;
import com.sismics.util.EnvironmentUtil;
import com.sismics.util.ResourceUtil;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity manager factory.
 * 
 * @author jtremeaux
 */
public final class EMF {
    private static final Logger log = LoggerFactory.getLogger(EMF.class);

    private static Map<Object, Object> properties;

    private static EntityManagerFactory emfInstance;

    static {
        try {
            properties = getEntityManagerProperties();

            Environment.verifyProperties(properties);
            ConfigurationHelper.resolvePlaceHolders(properties);
            ServiceRegistry reg = new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();

            DbOpenHelper openHelper = new DbOpenHelper(reg) {
                
                @Override
                public void onCreate() throws Exception {
                    executeAllScript(0);
                }

                @Override
                public void onUpgrade(int oldVersion, int newVersion) throws Exception {
                    for (int version = oldVersion + 1; version <= newVersion; version++) {
                        executeAllScript(version);
                    }
                }
            };
            openHelper.open();
            
            emfInstance = Persistence.createEntityManagerFactory("transactions-optional", getEntityManagerProperties());
            
        } catch (Throwable t) {
            log.error("Error creating EMF", t);
        }
    }
    
    private static Map<Object, Object> getEntityManagerProperties() {
        // Use external properties file if it exists
        String propertiesFile = EnvironmentUtil.getHibernateProperties();
        if (propertiesFile != null) {
            log.info("Loading hibernate.properties from location: " + propertiesFile);
            try {
                URL hibernatePropertiesUrl = new URL(propertiesFile);
                return ResourceUtil.loadPropertiesFromUrl(hibernatePropertiesUrl);
            } catch (Exception e) {
                log.error("Error loading external hibernate.properties: " + propertiesFile, e);
            }
        }

        // Use properties file packaged with the app if it exists
        URL hibernatePropertiesUrl = EMF.class.getResource("/hibernate.properties");
        if (hibernatePropertiesUrl != null) {
            log.info("Configuring EntityManager from packaged hibernate.properties: " + hibernatePropertiesUrl);

            return ResourceUtil.loadPropertiesFromUrl(hibernatePropertiesUrl);
        }

        // Otherwise, use environment parameters
        log.info("Configuring EntityManager from environment parameters");
        return getEntityManagerPropertiesFromEnvironment();
    }

    private static Map<Object, Object> getEntityManagerPropertiesFromEnvironment() {
        Map<Object, Object> props = new HashMap<Object, Object>();
        props.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        File dbDirectory = DirectoryUtil.getDbDirectory();
        String dbFile = dbDirectory.getAbsoluteFile() + File.separator + "reader";
        props.put("hibernate.connection.url", "jdbc:hsqldb:file:" + dbFile + ";hsqldb.write_delay=false;shutdown=true");
        props.put("hibernate.connection.username", "sa");
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        props.put("hibernate.show_sql", "false");
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

    public static boolean isDriverHsql() {
        String driver = getDriver();
        return driver.contains("hsqldb");
    }

    public static boolean isDriverPostgresql() {
        String driver = getDriver();
        return driver.contains("postgresql");
    }

    public static String getDriver() {
        return (String) properties.get("hibernate.connection.driver_class");
    }
}