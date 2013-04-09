package com.sismics.reader.core.dao.jpa;

import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;

import com.sismics.reader.core.model.jpa.AuthenticationToken;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Authentication token DAO.
 * 
 * @author jtremeaux
 */
public class AuthenticationTokenDao {
    /**
     * Gets an authentication token.
     * 
     * @param token Authentication token ID
     * @return Authentication token
     */
    public AuthenticationToken get(String token) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(AuthenticationToken.class, token);
    }

    /**
     * Creates a new authentication token.
     * 
     * @param authenticationToken Authentication token
     * @return Authentication token ID
     */
    public String create(AuthenticationToken authenticationToken) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        authenticationToken.setToken(UUID.randomUUID().toString());
        authenticationToken.setCreationDate(new Date());
        em.persist(authenticationToken);
        
        return authenticationToken.getToken();
    }

    /**
     * Deletes the authentication token.
     * 
     * @param authenticationTokenId Authentication token ID
     * @throws Exception
     */
    public void delete(String authenticationTokenId) throws Exception {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        AuthenticationToken authenticationToken = em.find(AuthenticationToken.class, authenticationTokenId);
        if (authenticationToken != null) {
            em.remove(authenticationToken);
        } else {
            throw new Exception("Token not found: " + authenticationTokenId);
        }
    }
}
