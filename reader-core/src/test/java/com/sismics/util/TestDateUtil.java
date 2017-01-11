package com.sismics.util;

import com.sismics.reader.core.dao.file.rss.RssReader;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Test of the date utilities.
 * 
 * @author jtremeaux
 */
public class TestDateUtil {

    @Test
    public void guessTimezoneCodeTest() throws Exception {
        Assert.assertEquals("Thu, 04 APR 2013 20:37:27 +10", DateUtil.guessTimezoneOffset("Thu, 04 APR 2013 20:37:27 AEST"));
    }

    @Test
    public void parseDateTest() throws Exception {
        Assert.assertNotNull(DateUtil.parseDate("Fri Jan 06 2017 16:13:28 GMT+0900 (JST)", RssReader.DF_RSS));
    }
}
