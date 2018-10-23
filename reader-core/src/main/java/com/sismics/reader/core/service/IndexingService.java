package com.sismics.reader.core.service;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.reader.core.constant.Constants;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.dao.lucene.ArticleDao;
import com.sismics.reader.core.event.RebuildIndexAsyncEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.util.DirectoryUtil;
import com.sismics.reader.core.util.TransactionUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    
    /**
     * Index reader.
     */
    private DirectoryReader directoryReader;
    
    /**
     * Lucene storage config.
     */
    private String luceneStorageConfig;
    
    public IndexingService(String luceneStorageConfig) {
        this.luceneStorageConfig = luceneStorageConfig;
    }

    @Override
    protected void startUp() {
        // RAM directory storage by default
        if (luceneStorageConfig == null || luceneStorageConfig.equals(Constants.LUCENE_DIRECTORY_STORAGE_RAM)) {
            directory = new RAMDirectory();
            log.info("Using RAM Lucene storage");
        } else if (luceneStorageConfig.equals(Constants.LUCENE_DIRECTORY_STORAGE_FILE)) {
            File luceneDirectory = DirectoryUtil.getLuceneDirectory();
            log.info("Using file Lucene storage: {}", luceneDirectory);
            try {
                directory = new SimpleFSDirectory(luceneDirectory, new SimpleFSLockFactory());
            } catch (IOException e) {
                log.error("Error initializing Lucene index", e);
            }
        }
    }

    @Override
    protected void shutDown() {
        if (directoryReader != null) {
            try {
                directoryReader.close();
            } catch (IOException e) {
                log.error("Error closing the index reader", e);
            }
        }
        if (directory != null) {
            try {
                directory.close();
            } catch (IOException e) {
                log.error("Error closing Lucene index", e);
            }
        }
    }
    
    @Override
    protected void runOneIteration() throws Exception {
        TransactionUtil.handle(() -> {
            // NOP
        });
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.HOURS);
    }
    
    /**
     * Search articles.
     * 
     * @param userId User ID
     * @param searchQuery The query
     * @param offset Offset
     * @param limit Limit
     * @return List of articles
     */
    public PaginatedList<UserArticleDto> searchArticles(String userId, String searchQuery, Integer offset, Integer limit) throws Exception {
        // Search articles
        ArticleDao articleDao = new ArticleDao();
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, offset);
        Map<String, Article> articleMap = null;
        articleMap = articleDao.search(paginatedList, searchQuery);
        
        if (articleMap.size() > 0) {
            // Get linked UserArticle from database
            UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                    .setUserId(userId)
                    .setVisible(false)
                    .setArticleIdIn(Lists.newArrayList(articleMap.keySet()));
            
            UserArticleDao userArticleDao = new UserArticleDao();
            PaginatedList<UserArticleDto> userArticledList = PaginatedLists.create(paginatedList.getLimit(), 0);
            userArticleDao.findByCriteria(userArticledList, userArticleCriteria, null, null);
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
    
    /**
     * Returns a valid directory reader.
     * Take care of reopening the reader if the index has changed
     * and closing the previous one.
     *
     * @return the directoryReader
     */
    public DirectoryReader getDirectoryReader() {
        if (directoryReader == null) {
            if (!DirectoryReader.indexExists(directory)) {
                log.info("Lucene directory not yet created");
                return null;
            }
            try {
                directoryReader = DirectoryReader.open(directory);
            } catch (IOException e) {
                log.error("Error creating the directory reader", e);
            }
        } else {
            try {
                DirectoryReader newReader = DirectoryReader.openIfChanged(directoryReader);
                if (newReader != null) {
                    directoryReader.close();
                    directoryReader = newReader;
                }
            } catch (IOException e) {
                log.error("Error while reopening the directory reader", e);
            }
        }
        return directoryReader;
    }
}
