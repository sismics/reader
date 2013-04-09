package com.sismics.reader.core.dao.jpa;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.collect.Sets;
import com.sismics.util.context.ThreadLocalContext;

/**
 * User base functions DAO.
 * 
 * @author jtremeaux
 */
public class UserBaseFunctionDao {
    /**
     * Find a user's base functions.
     * 
     * @param userId User ID
     * @return Set of base functions
     */
    @SuppressWarnings("unchecked")
    public Set<String> findByUserId(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery("select ubf.UBF_IDBASEFUNCTION_C from T_USER_BASE_FUNCTION ubf where ubf.UBF_IDUSER_C = :userId and ubf.UBF_DELETEDATE_D is null");
        q.setParameter("userId", userId);
        return Sets.newHashSet(q.getResultList());
    }
}
