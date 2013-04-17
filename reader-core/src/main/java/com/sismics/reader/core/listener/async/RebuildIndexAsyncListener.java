package com.sismics.reader.core.listener.async;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.dao.lucene.ArticleDao;
import com.sismics.reader.core.event.RebuildIndexAsyncEvent;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.util.TransactionUtil;

/**
 * Listener on rebuild index.
 * 
 * @author bgamard
 */
public class RebuildIndexAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RebuildIndexAsyncListener.class);

    /**
     * Rebuild articles index.
     * 
     * @param rebuildIndexAsyncEvent Index rebuild event
     * @throws Exception
     */
    @Subscribe
    public void onArticleCreated(final RebuildIndexAsyncEvent rebuildIndexAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Rebuild index event: " + rebuildIndexAsyncEvent.toString());
        }
        
        // Fetch all articles
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                com.sismics.reader.core.dao.jpa.ArticleDao jpaArticleDao = new com.sismics.reader.core.dao.jpa.ArticleDao();
                List<Article> articleList = jpaArticleDao.findAll();
                
                // Rebuild index
                ArticleDao articleDao = new ArticleDao();
                articleDao.rebuildIndex(articleList);
            }
        });
    }
}
