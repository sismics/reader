package com.sismics.util;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test of the resource utils.
 *
 * @author jtremeaux 
 */
public class TestResourceUtil {

    @Test
    public void listFilesTest() throws Exception {
        List<String> fileList = ResourceUtil.list(Test.class, "/junit/framework");
        assertTrue(fileList.contains("Test.class"));

        fileList = ResourceUtil.list(Test.class, "/junit/framework/");
        assertTrue(fileList.contains("Test.class"));

        fileList = ResourceUtil.list(Test.class, "junit/framework/");
        assertTrue(fileList.contains("Test.class"));

        fileList = ResourceUtil.list(Test.class, "junit/framework/");
        assertTrue(fileList.contains("Test.class"));
    }

    @Test
    public void loadResourceFileTest() {
        Map<Object, Object> properties = ResourceUtil.loadPropertiesFromUrl(TestResourceUtil.class.getResource("/config.properties"));
        assertNotNull(properties);
        assertTrue(properties.size() > 0);
    }
}
