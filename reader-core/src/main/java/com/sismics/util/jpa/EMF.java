package com.sismics.util.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            emfInstance = Persistence.createEntityManagerFactory("transactions-optional");
        } catch (Throwable t) {
            log.error("Error creating EMF", t);
        }
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