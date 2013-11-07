package com.sismics.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Test of the URL utilities.
 * 
 * @author jtremeaux
 */
public class TestUrlUtil {

    @Test
    public void getBaseUriTest() throws Exception {
        Assert.assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com"));
        Assert.assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com/"));
        Assert.assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com/asset"));
        Assert.assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com/asset/"));
        Assert.assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com/asset/img"));
        Assert.assertEquals("https://somehost.com", UrlUtil.getBaseUri("https://somehost.com/asset/img"));
    }

    @Test
    public void completeUrlTest() throws Exception {
        Assert.assertEquals("http://linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "http://linuxfr.org/img/test.png"));
        Assert.assertEquals("http://linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "/img/test.png"));
        Assert.assertEquals("http://linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "img/test.png"));
        Assert.assertEquals("http://img.linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "//img.linuxfr.org/img/test.png"));
        // TODO use the querypart of baseUrl
        // Assert.assertEquals("http://linuxfr.org/test/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/test/", "//img/test.png"));
    }
}
