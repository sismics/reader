package com.sismics.reader.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.dao.lucene.ArticleDao;
import com.sismics.reader.core.event.ArticleUpdatedAsyncEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Listener on updated articles.
 * 
 * @author bgamard
 */
public class ArticleUpdatedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ArticleUpdatedAsyncListener.class);

    /**
     * Process updated articles.
     * 
     * @param articlesUpdatedAsyncEvent Updated articles event
     */
    @Subscribe
    public void onArticleUpdated(final ArticleUpdatedAsyncEvent articlesUpdatedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Updated article event: " + articlesUpdatedAsyncEvent.toString());
        }
        long startTime = System.currentTimeMillis();
        
        // Update index
        ArticleDao articleDao = new ArticleDao();
        articleDao.update(articlesUpdatedAsyncEvent.getArticleList());

        long endTime = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Index updated in {0}ms", endTime - startTime));
        }
    }
}
