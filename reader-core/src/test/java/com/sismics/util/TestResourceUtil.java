package com.sismics.util;

import junit.framework.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Test of the resource utils.
 *
 * @author jtremeaux 
 */
public class TestResourceUtil {

    @Test
    public void listFilesTest() throws Exception {
        List<String> fileList = ResourceUtil.list(Test.class, "/junit/framework");
        Assert.assertTrue(fileList.contains("Test.class"));

        fileList = ResourceUtil.list(Test.class, "/junit/framework/");
        Assert.assertTrue(fileList.contains("Test.class"));

        fileList = ResourceUtil.list(Test.class, "junit/framework/");
        Assert.assertTrue(fileList.contains("Test.class"));

        fileList = ResourceUtil.list(Test.class, "junit/framework/");
        Assert.assertTrue(fileList.contains("Test.class"));
    }

    @Test
    public void loadResourceFileTest() {
        Map<Object, Object> properties = ResourceUtil.loadPropertiesFromUrl(TestResourceUtil.class.getResource("/config.properties"));
        Assert.assertNotNull(properties);
        Assert.assertTrue(properties.size() > 0);
    }
}
