package com.sismics.reader.core.dao.file.opml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.io.Closer;

/**
 * Test of the OPML reader.
 * 
 * @author jtremeaux
 */
public class TestOpmlReader {
    @Test
    public void googleOpmlReaderTest() throws Exception {
        Closer closer = Closer.create();
        InputStream is = null;
        try {
            is = closer.register(getClass().getResourceAsStream("/opml/greader_subscriptions.xml"));
            OpmlReader opmlReader = new OpmlReader();
            opmlReader.read(is);
            List<Outline> rootOutlineList = opmlReader.getOutlineList();
            Assert.assertEquals(3, rootOutlineList.size());
            Outline feed = rootOutlineList.get(0);
            Assert.assertEquals("rss", feed.getType());
            Outline category = rootOutlineList.get(1);
            Assert.assertEquals(null, category.getType());
            Assert.assertEquals("Comics", category.getTitle());
            Assert.assertEquals("Comics", category.getText());
            Assert.assertEquals(1, category.getOutlineList().size());
            category = category.getOutlineList().get(0);
            Assert.assertEquals(null, category.getType());
            Assert.assertEquals("Sub", category.getTitle());
            Assert.assertEquals(2, category.getOutlineList().size());
            Outline outline = category.getOutlineList().get(0);
            Assert.assertEquals("rss", outline.getType());
            Assert.assertEquals("Explosm.net, Comics", outline.getText());
            Assert.assertEquals("Explosm.net, Comics", outline.getTitle());
            Assert.assertEquals("http://pipes.yahoo.com/pipes/pipe.run?_id=1009ffe75092cb1845efb5cb901c2886&_render=rss", outline.getXmlUrl());
            Assert.assertEquals("http://pipes.yahoo.com/pipes/pipe.info?_id=1009ffe75092cb1845efb5cb901c2886", outline.getHtmlUrl());
            Assert.assertEquals(0, outline.getOutlineList().size());
            
            Map<String, List<Outline>> outlineMap = OpmlFlattener.flatten(rootOutlineList);
            Assert.assertEquals(3, outlineMap.size());
            List<Outline> rootList = outlineMap.get(null);
            Assert.assertEquals(1, rootList.size());
            List<Outline> comicsSub = outlineMap.get("Comics / Sub");
            Assert.assertEquals(2, comicsSub.size());
            List<Outline> dev = outlineMap.get("Dev");
            Assert.assertEquals(1, dev.size());
        } finally {
            closer.close();
        }
    }
    
    /**
     * Related to #87.
     * 
     * @throws Exception
     */
    @Test
    public void lifereaOpmlReaderTest() throws Exception {
        Closer closer = Closer.create();
        InputStream is = null;
        try {
            is = closer.register(getClass().getResourceAsStream("/opml/liferea_subscriptions.xml"));
            OpmlReader opmlReader = new OpmlReader();
            opmlReader.read(is);
            List<Outline> rootOutlineList = opmlReader.getOutlineList();
            Assert.assertEquals(1, rootOutlineList.size());
            Outline category = rootOutlineList.get(0);
            Assert.assertEquals(null, category.getType());
            Assert.assertEquals("MyFeeds", category.getTitle());
            Assert.assertEquals("MyFeeds", category.getText());
            Assert.assertEquals(6, category.getOutlineList().size());
            Outline outline = category.getOutlineList().get(0);
            Assert.assertEquals("rss", outline.getType());
            Assert.assertEquals("Web Design Ledger", outline.getText());
            Assert.assertEquals("Web Design Ledger", outline.getTitle());
            Assert.assertEquals("http://feeds.feedburner.com/WebDesignLedger", outline.getXmlUrl());
            Assert.assertEquals("http://webdesignledger.com", outline.getHtmlUrl());
            Assert.assertEquals(0, outline.getOutlineList().size());
            
            Map<String, List<Outline>> outlineMap = OpmlFlattener.flatten(rootOutlineList);
            Assert.assertEquals(1, outlineMap.size());
            List<Outline> rootList = outlineMap.get("MyFeeds");
            Assert.assertEquals(6, rootList.size());
        } finally {
            closer.close();
        }
    }
}
