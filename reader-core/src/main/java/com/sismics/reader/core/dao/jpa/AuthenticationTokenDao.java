package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.model.jpa.AuthenticationToken;
import com.sismics.util.context.ThreadLocalContext;
import org.joda.time.DateTime;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.UUID;

/**
 * Authentication token DAO.
 * 
 * @author jtremeaux
 */
public class AuthenticationTokenDao {
    /**
     * Gets an authentication token.
     * 
     * @param id Authentication token ID
     * @return Authentication token
     */
    public AuthenticationToken get(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(AuthenticationToken.class, id);
    }

    /**
     * Creates a new authentication token.
     * 
     * @param authenticationToken Authentication token
     * @return Authentication token ID
     */
    public String create(AuthenticationToken authenticationToken) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        authenticationToken.setId(UUID.randomUUID().toString());
        authenticationToken.setCreationDate(new Date());
        em.persist(authenticationToken);
        
        return authenticationToken.getId();
    }

    /**
     * Deletes the authentication token.
     * 
     * @param authenticationTokenId Authentication token ID
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

    /**
     * Deletes old short lived tokens.
     *
     * @param userId User ID
     */
    public void deleteOldSessionToken(String userId) {
        StringBuilder sb = new StringBuilder("delete from T_AUTHENTICATION_TOKEN ");
        sb.append(" where AUT_IDUSER_C = :userId and AUT_LONGLASTED_B = :longLasted");
        sb.append(" and AUT_LASTCONNECTIONDATE_D < :minDate ");

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery(sb.toString())
                .setParameter("userId", userId)
                .setParameter("longLasted", false)
                .setParameter("minDate", DateTime.now().minusDays(1).toDate())
                .executeUpdate();
    }

    /**
     * Deletes old short lived tokens.
     *
     * @param id Token id
     */
    public void updateLastConnectionDate(String id) {
        StringBuilder sb = new StringBuilder("update T_AUTHENTICATION_TOKEN ");
        sb.append(" set AUT_LASTCONNECTIONDATE_D = :currentDate ");
        sb.append(" where AUT_ID_C = :id");

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery(sb.toString())
                .setParameter("currentDate", new Date())
                .setParameter("id", id)
                .executeUpdate();
    }
}
