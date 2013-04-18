package com.sismics.reader.core.dao.file.json;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.io.Closer;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;

/**
 * Test of the starred JSON reader.
 * 
 * @author jtremeaux
 */
public class TestStarredReader {
    @Test
    public void starredReaderTest() throws Exception {
        Closer closer = Closer.create();
        InputStream is = null;
        try {
            is = closer.register(getClass().getResourceAsStream("/google_takeout/starred.json"));
            StarredReader starredReader = new StarredReader();
            starredReader.read(is);
            
            Map<String, List<Article>> articleMap = starredReader.getArticleMap();
            Map<String, Feed> feedMap = starredReader.getFeedMap();
            Assert.assertEquals(2, articleMap.size());
            Assert.assertEquals(2, feedMap.size());
        } finally {
            closer.close();
        }
    }
}
