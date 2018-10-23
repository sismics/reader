package com.sismics.reader.core.dao.file.opml;

import com.google.common.io.Closer;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

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
            assertEquals(3, rootOutlineList.size());
            Outline feed = rootOutlineList.get(0);
            assertEquals("rss", feed.getType());
            Outline category = rootOutlineList.get(1);
            assertEquals(null, category.getType());
            assertEquals("Comics", category.getTitle());
            assertEquals("Comics", category.getText());
            assertEquals(1, category.getOutlineList().size());
            category = category.getOutlineList().get(0);
            assertEquals(null, category.getType());
            assertEquals("Sub", category.getTitle());
            assertEquals(2, category.getOutlineList().size());
            Outline outline = category.getOutlineList().get(0);
            assertEquals("rss", outline.getType());
            assertEquals("Explosm.net, Comics", outline.getText());
            assertEquals("Explosm.net, Comics", outline.getTitle());
            assertEquals("http://pipes.yahoo.com/pipes/pipe.run?_id=1009ffe75092cb1845efb5cb901c2886&_render=rss", outline.getXmlUrl());
            assertEquals("http://pipes.yahoo.com/pipes/pipe.info?_id=1009ffe75092cb1845efb5cb901c2886", outline.getHtmlUrl());
            assertEquals(0, outline.getOutlineList().size());
            
            Map<String, List<Outline>> outlineMap = OpmlFlattener.flatten(rootOutlineList);
            assertEquals(3, outlineMap.size());
            List<Outline> rootList = outlineMap.get(null);
            assertEquals(1, rootList.size());
            List<Outline> comicsSub = outlineMap.get("Comics / Sub");
            assertEquals(2, comicsSub.size());
            List<Outline> dev = outlineMap.get("Dev");
            assertEquals(1, dev.size());
        } finally {
            closer.close();
        }
    }
    
    /**
     * Related to #87.
     * 
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
            assertEquals(1, rootOutlineList.size());
            Outline category = rootOutlineList.get(0);
            assertEquals("folder", category.getType());
            assertEquals("MyFeeds", category.getTitle());
            assertEquals("MyFeeds", category.getText());
            assertEquals(6, category.getOutlineList().size());
            Outline outline = category.getOutlineList().get(0);
            assertEquals("rss", outline.getType());
            assertEquals("Web Design Ledger", outline.getText());
            assertEquals("Web Design Ledger", outline.getTitle());
            assertEquals("http://feeds.feedburner.com/WebDesignLedger", outline.getXmlUrl());
            assertEquals("http://webdesignledger.com", outline.getHtmlUrl());
            assertEquals(0, outline.getOutlineList().size());
            
            Map<String, List<Outline>> outlineMap = OpmlFlattener.flatten(rootOutlineList);
            assertEquals(1, outlineMap.size());
            List<Outline> rootList = outlineMap.get("MyFeeds");
            assertEquals(6, rootList.size());
        } finally {
            closer.close();
        }
    }
    
    /**
     * Related to #95.
     * 
     */
    @Test
    public void ttrssOpmlReaderTest() throws Exception {
        Closer closer = Closer.create();
        InputStream is = null;
        try {
            is = closer.register(getClass().getResourceAsStream("/opml/ttrss_subscriptions.xml"));
            OpmlReader opmlReader = new OpmlReader();
            opmlReader.read(is);
            List<Outline> rootOutlineList = opmlReader.getOutlineList();
            assertEquals(8, rootOutlineList.size());
            Outline category = rootOutlineList.get(0);
            assertEquals(null, category.getType());
            assertEquals(null, category.getTitle());
            assertEquals("test", category.getText());
            assertEquals(1, category.getOutlineList().size());
            Outline outline = category.getOutlineList().get(0);
            assertEquals(null, outline.getType());
            assertEquals(null, outline.getTitle());
            assertEquals("Fefes Blog", outline.getText());
            assertEquals("http://blog.fefe.de/rss.xml?html", outline.getXmlUrl());
            assertEquals("http://blog.fefe.de/", outline.getHtmlUrl());
            assertEquals(0, outline.getOutlineList().size());
            outline = rootOutlineList.get(1);
            assertEquals(null, outline.getType());
            assertEquals(null, outline.getTitle());
            assertEquals("Boing Boing", outline.getText());
            assertEquals(0, outline.getOutlineList().size());
            
            
            Map<String, List<Outline>> outlineMap = OpmlFlattener.flatten(rootOutlineList);
            assertEquals(2, outlineMap.size());
            List<Outline> rootList = outlineMap.get("test");
            assertEquals(1, rootList.size());
            rootList = outlineMap.get(null);
            assertEquals(7, rootList.size());
        } finally {
            closer.close();
        }
    }
}
