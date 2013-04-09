package com.sismics.reader.core.listener.async;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.dao.lucene.ArticleDao;
import com.sismics.reader.core.event.ArticleCreatedAsyncEvent;
import com.sismics.reader.core.model.jpa.Article;

/**
 * Listener on newly created articles.
 * 
 * @author bgamard
 */
public class ArticleCreatedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ArticleCreatedAsyncListener.class);

    /**
     * Process new articles.
     * 
     * @param articlesCreatedAsyncEvent New articles created event
     * @throws Exception
     */
    @Subscribe
    public void onArticleCreated(final ArticleCreatedAsyncEvent articlesCreatedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Articles created event: " + articlesCreatedAsyncEvent.toString());
        }
        
        final List<Article> articleList = articlesCreatedAsyncEvent.getArticleList();
        
        // Index new articles
        ArticleDao articleDao = new ArticleDao();
        articleDao.create(articleList);
    }
}
