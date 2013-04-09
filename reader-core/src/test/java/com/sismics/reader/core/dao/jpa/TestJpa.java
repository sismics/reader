package com.sismics.reader.core.dao.jpa;

import junit.framework.Assert;

import org.junit.Test;

import com.sismics.reader.BaseTransactionalTest;
import com.sismics.reader.core.dao.jpa.UserDao;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.reader.core.util.TransactionUtil;

/**
 * Tests the persistance layer.
 * 
 * @author jtremeaux
 */
public class TestJpa extends BaseTransactionalTest {
    @Test
    public void testJpa() throws Exception {
        // Create a user
        UserDao userDao = new UserDao();
        User user = new User();
        user.setUsername("username");
        user.setEmail("toto@reader.com");
        user.setLocaleId("fr_FR");
        String id = userDao.create(user);
        
        TransactionUtil.commit();

        // Search a user by his ID
        user = userDao.getById(id);
        Assert.assertNotNull(user);
        Assert.assertEquals("toto@reader.com", user.getEmail());
    }
}
