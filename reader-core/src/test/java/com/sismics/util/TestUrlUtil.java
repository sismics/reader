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
    public void completeUrlTest() throws Exception {
        Assert.assertEquals("http://linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "http://linuxfr.org/img/test.png"));
        Assert.assertEquals("http://linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "/img/test.png"));
        Assert.assertEquals("http://linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "img/test.png"));
        Assert.assertEquals("http://img.linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "//img.linuxfr.org/img/test.png"));
        // TODO use the querypart of baseUrl
        // Assert.assertEquals("http://linuxfr.org/test/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/test/", "//img/test.png"));
    }
}
