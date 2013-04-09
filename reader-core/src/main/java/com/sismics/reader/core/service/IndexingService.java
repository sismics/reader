package com.sismics.reader.core.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.dao.lucene.ArticleDao;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.util.DirectoryUtil;
import com.sismics.reader.core.util.TransactionUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;

/**
 * Indexing service.
 *
 * @author bgamard
 */
public class IndexingService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);

    /**
     * Lucene directory.
     */
    private Directory directory;
    
    @Override
    protected void startUp() {
        File luceneDirectory = DirectoryUtil.getLuceneDirectory();
        
        try {
            directory = new SimpleFSDirectory(luceneDirectory, new SimpleFSLockFactory());
        } catch (IOException e) {
            log.error("Error initializing Lucene index", e);
        }
    }

    @Override
    protected void shutDown() {
        Directory luceneIndex = AppContext.getInstance().getLuceneDirectory();
        if (luceneIndex != null) {
            try {
                luceneIndex.close();
            } catch (IOException e) {
                log.error("Error closing Lucene index", e);
            }
        }
    }
    
    @Override
    protected void runOneIteration() throws Exception {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                // NOP
            }
        });
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.HOURS);
    }
    
    /**
     * Search articles.
     * 
     * @param query Search query
     * @return List of articles
     * @throws Exception 
     */
    public PaginatedList<UserArticleDto> searchArticles(String userId, List<String> feedList, String searchQuery, Integer offset, Integer limit) throws Exception {
        // Search articles
        ArticleDao articleDao = new ArticleDao();
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, offset);
        List<String> articleIdList = articleDao.search(paginatedList, feedList, searchQuery);
        
        if (articleIdList.size() > 0) {
            // Get linked UserArticle from database
            UserArticleCriteria userArticleCriteria = new UserArticleCriteria();
            userArticleCriteria.setUserId(userId);
            userArticleCriteria.setArticleIdIn(articleIdList);
            
            UserArticleDao userArticleDao = new UserArticleDao();
            PaginatedList<UserArticleDto> userArticledList = PaginatedLists.create(paginatedList.getLimit(), 0);
            userArticleDao.findByCriteria(userArticleCriteria, userArticledList);
            paginatedList.setResultList(userArticledList.getResultList());
        } else {
            paginatedList.setResultList(new ArrayList<UserArticleDto>());
        }
        
        return  paginatedList;
    }

    /**
     * Getter of directory.
     *
     * @return the directory
     */
    public Directory getDirectory() {
        return directory;
    }
}
