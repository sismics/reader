package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.constant.Constants;
import com.sismics.reader.core.dao.jpa.criteria.UserCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserDto;
import com.sismics.reader.core.dao.jpa.mapper.UserMapper;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

/**
 * User DAO.
 * 
 * @author jtremeaux
 */
public class UserDao extends BaseDao<UserDto, UserCriteria> {

    @Override
    protected QueryParam getQueryParam(UserCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select u.USE_ID_C as c0, u.USE_USERNAME_C as c1, u.USE_EMAIL_C as c2, u.USE_CREATEDATE_D as c3, u.USE_IDLOCALE_C as c4")
                .append(" from T_USER u ");

        // Add search criterias
        criteriaList.add("u.USE_DELETEDATE_D is null");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, null, filterCriteria, new UserMapper());
    }

    /**
     * Authenticates an user.
     * 
     * @param username User login
     * @param password User password
     * @return ID of the authenticated user or null
     */
    public String authenticate(String username, String password) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null")
                .setParameter("username", username);
        try {
            User user = (User) q.getSingleResult();
            if (!BCrypt.checkpw(password, user.getPassword())) {
                return null;
            }
            return user.getId();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Creates a new user.
     * 
     * @param user User to create
     * @return User ID
     */
    public String create(User user) throws Exception {
        // Create the user UUID
        user.setId(UUID.randomUUID().toString());
        
        // Checks for user unicity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null")
                .setParameter("username", user.getUsername());
        List<?> l = q.getResultList();
        if (l.size() > 0) {
            throw new Exception("AlreadyExistingUsername");
        }
        
        user.setCreateDate(new Date());
        user.setPassword(hashPassword(user.getPassword()));
        user.setTheme(Constants.DEFAULT_THEME_ID);
        em.persist(user);
        
        return user.getId();
    }
    
    /**
     * Updates a user.
     * 
     * @param user User to update
     * @return Updated user
     */
    public User update(User user) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the user
        Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null")
                .setParameter("id", user.getId());
        User userFromDb = (User) q.getSingleResult();

        // Update the user
        userFromDb.setLocaleId(user.getLocaleId());
        userFromDb.setEmail(user.getEmail());
        userFromDb.setTheme(user.getTheme());
        userFromDb.setDisplayTitleWeb(user.isDisplayTitleWeb());
        userFromDb.setDisplayTitleMobile(user.isDisplayTitleMobile());
        userFromDb.setDisplayUnreadWeb(user.isDisplayUnreadWeb());
        userFromDb.setDisplayUnreadMobile(user.isDisplayUnreadMobile());
        userFromDb.setFirstConnection(user.isFirstConnection());
        
        return user;
    }
    
    /**
     * Update the user password.
     * 
     * @param user User to update
     * @return Updated user
     */
    public User updatePassword(User user) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the user
        Query q = em.createQuery("select u from User u where u.id = :id and u.deleteDate is null")
                .setParameter("id", user.getId());
        User userFromDb = (User) q.getSingleResult();

        // Update the user
        userFromDb.setPassword(hashPassword(user.getPassword()));
        
        return user;
    }

    /**
     * Gets a user by its ID.
     * 
     * @param id User ID
     * @return User
     */
    public User getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(User.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets an active user by its username.
     * 
     * @param username User's username
     * @return User
     */
    public User getActiveByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null")
                    .setParameter("username", username);
            return (User) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets an active user by its password recovery token.
     * 
     * @param passwordResetKey Password recovery token
     * @return User
     */
    public User getActiveByPasswordResetKey(String passwordResetKey) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select u from User u where u.passwordResetKey = :passwordResetKey and u.deleteDate is null")
                    .setParameter("passwordResetKey", passwordResetKey);
            return (User) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes a user.
     * 
     * @param username User's username
     */
    public void delete(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the user
        User userFromDb = (User) em.createQuery("select u from User u where u.username = :username and u.deleteDate is null")
                .setParameter("username", username)
                .getSingleResult();
        
        // Delete the user
        Date dateNow = new Date();
        userFromDb.setDeleteDate(dateNow);

        // Delete linked data
        em.createQuery("delete from AuthenticationToken at where at.userId = :userId")
                .setParameter("userId", userFromDb.getId())
                .executeUpdate();

        em.createQuery("update UserArticle ua set ua.deleteDate = :dateNow where ua.userId = :userId and ua.deleteDate is null")
                .setParameter("userId", userFromDb.getId())
                .setParameter("dateNow", dateNow)
                .executeUpdate();

        em.createQuery("update FeedSubscription fs set fs.deleteDate = :dateNow where fs.userId = :userId and fs.deleteDate is null")
                .setParameter("userId", userFromDb.getId())
                .setParameter("dateNow", dateNow)
                .executeUpdate();

        em.createQuery("update Category c set c.deleteDate = :dateNow where c.userId = :userId and c.deleteDate is null")
                .setParameter("userId", userFromDb.getId())
                .setParameter("dateNow", dateNow)
                .executeUpdate();
    }

    /**
     * Hash the user's password.
     * 
     * @param password Clear password
     * @return Hashed password
     */
    protected String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
