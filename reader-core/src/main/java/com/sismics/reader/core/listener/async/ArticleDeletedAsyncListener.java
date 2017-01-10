package com.sismics.reader.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.dao.lucene.ArticleDao;
import com.sismics.reader.core.event.ArticleDeletedAsyncEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Listener on deleted articles.
 * 
 * @author bgamard
 */
public class ArticleDeletedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ArticleDeletedAsyncListener.class);

    /**
     * Process deleted articles.
     * 
     * @param articlesDeletedAsyncEvent Deleted articles event
     */
    @Subscribe
    public void onArticleDeleted(final ArticleDeletedAsyncEvent articlesDeletedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Deleted article event: " + articlesDeletedAsyncEvent.toString());
        }
        long startTime = System.currentTimeMillis();
        
        // Delete index
        ArticleDao articleDao = new ArticleDao();
        articleDao.delete(articlesDeletedAsyncEvent.getArticleList());

        long endTime = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Index deleted in {0}ms", endTime - startTime));
        }
    }
}
