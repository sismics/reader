package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.BaseTransactionalTest;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.reader.core.util.TransactionUtil;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

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
        user.setRoleId("user");
        String id = userDao.create(user);
        
        TransactionUtil.commit();

        // Search a user by his ID
        user = userDao.getById(id);
        assertNotNull(user);
        assertEquals("toto@reader.com", user.getEmail());
    }
}
