package com.sismics.reader.core.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.constant.Constants;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.dao.lucene.ArticleDao;
import com.sismics.reader.core.event.RebuildIndexAsyncEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.reader.core.model.jpa.UserArticle;
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
        ConfigDao configDao = new ConfigDao();
        Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTOY_STORAGE);
        
        // RAM directory storage by default
        if (luceneStorageConfig == null || luceneStorageConfig.getValue().equals(Constants.LUCENE_DIRECTORY_STORAGE_RAM)) {
            directory = new RAMDirectory();
        } else if (luceneStorageConfig.getValue().equals(Constants.LUCENE_DIRECTORY_STORAGE_FILE)) {
            File luceneDirectory = DirectoryUtil.getLuceneDirectory();
            try {
                directory = new SimpleFSDirectory(luceneDirectory, new SimpleFSLockFactory());
            } catch (IOException e) {
                log.error("Error initializing Lucene index", e);
            }
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
        Map<String, Article> articleMap = null;
        if (feedList.size() > 0) {
            articleMap = articleDao.search(paginatedList, feedList, searchQuery);
        }
        
        if (articleMap != null && articleMap.size() > 0) {
            // Get linked UserArticle from database
            UserArticleCriteria userArticleCriteria = new UserArticleCriteria();
            userArticleCriteria.setUserId(userId);
            userArticleCriteria.setVisible(false);
            userArticleCriteria.setArticleIdIn(Lists.newArrayList(articleMap.keySet()));
            
            UserArticleDao userArticleDao = new UserArticleDao();
            PaginatedList<UserArticleDto> userArticledList = PaginatedLists.create(paginatedList.getLimit(), 0);
            userArticleDao.findByCriteria(userArticleCriteria, userArticledList);
            paginatedList.setResultList(userArticledList.getResultList());
            
            for (UserArticleDto userArticleDto : paginatedList.getResultList()) {
                Article article = articleMap.get(userArticleDto.getArticleId());
                if (article.getTitle() != null) {
                    userArticleDto.setArticleTitle(article.getTitle());
                }
                if (article.getDescription() != null) {
                    userArticleDto.setArticleDescription(article.getDescription());
                }
                
                // Create UserArticle if it does not exists
                if (userArticleDto.getId() == null) {
                    UserArticle userArticle = new UserArticle();
                    userArticle.setArticleId(userArticleDto.getArticleId());
                    userArticle.setUserId(userId);
                    userArticle.setReadDate(new Date());
                    String userArticleId = userArticleDao.create(userArticle);
                    userArticleDto.setId(userArticleId);
                    userArticleDto.setReadTimestamp(userArticle.getReadDate().getTime());
                }
            }
        } else {
            paginatedList.setResultList(new ArrayList<UserArticleDto>());
        }
        
        return paginatedList;
    }
    
    /**
     * Destroy and rebuild Lucene index.
     * 
     * @throws Exception 
     */
    public void rebuildIndex() throws Exception {
        RebuildIndexAsyncEvent rebuildIndexAsyncEvent = new RebuildIndexAsyncEvent();
        AppContext.getInstance().getAsyncEventBus().post(rebuildIndexAsyncEvent);
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
