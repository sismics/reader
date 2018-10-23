package com.sismics.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test of the URL utilities.
 * 
 * @author jtremeaux
 */
public class TestUrlUtil {

    @Test
    public void getBaseUriTest() throws Exception {
        assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com"));
        assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com/"));
        assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com/asset"));
        assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com/asset/"));
        assertEquals("http://somehost.com", UrlUtil.getBaseUri("http://somehost.com/asset/img"));
        assertEquals("https://somehost.com", UrlUtil.getBaseUri("https://somehost.com/asset/img"));
    }

    @Test
    public void completeUrlTest() throws Exception {
        assertEquals("http://linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "http://linuxfr.org/img/test.png"));
        assertEquals("http://linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "/img/test.png"));
        assertEquals("http://linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "img/test.png"));
        assertEquals("http://img.linuxfr.org/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/", "//img.linuxfr.org/img/test.png"));
        // TODO use the querypart of baseUrl
        // assertEquals("http://linuxfr.org/test/img/test.png", UrlUtil.completeUrl("http://linuxfr.org/test/", "//img/test.png"));
    }
}
