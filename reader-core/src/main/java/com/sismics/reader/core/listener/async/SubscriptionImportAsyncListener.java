package com.sismics.reader.core.listener.async;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.sismics.reader.core.dao.file.json.StarredArticleImportedEvent;
import com.sismics.reader.core.dao.file.json.StarredArticleImportedListener;
import com.sismics.reader.core.dao.file.json.StarredReader;
import com.sismics.reader.core.dao.file.opml.OpmlFlattener;
import com.sismics.reader.core.dao.file.opml.OpmlReader;
import com.sismics.reader.core.dao.file.opml.Outline;
import com.sismics.reader.core.dao.file.rss.GuidFixer;
import com.sismics.reader.core.dao.jpa.ArticleDao;
import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.FeedDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.event.SubscriptionImportedEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Category;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.util.EntityManagerUtil;
import com.sismics.reader.core.util.TransactionUtil;
import com.sismics.util.mime.MimeType;
import com.sismics.util.mime.MimeTypeUtil;

/**
 * Listener on subscriptions import request.
 * 
 * @author jtremeaux
 */
public class SubscriptionImportAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SubscriptionImportAsyncListener.class);

    /**
     * Process the event.
     * 
     * @param subscriptionImportedEvent OPML imported event
     * @throws Exception
     */
    @Subscribe
    public void onSubscriptionImport(final SubscriptionImportedEvent subscriptionImportedEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("OPML import requested event: {0}", subscriptionImportedEvent.toString()));
        }
        
        final User user = subscriptionImportedEvent.getUser();
        final File importFile = subscriptionImportedEvent.getImportFile();
        
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                processImportFile(user, importFile);
            }
        });
    }

    /**
     * Process the import file.
     * 
     * @param user User
     * @param importFile File to import
     */
    private void processImportFile(final User user, File importFile) {
        List<Outline> outlineList = null;
        Map<String, List<Article>> articleMap = null;
        Closer closer = Closer.create();
        try {
            // Guess the file type
            String mimeType = MimeTypeUtil.guessMimeType(importFile);
            if (MimeType.APPLICATION_ZIP.equals(mimeType)) {
                // Assume the file is a Google Takeout ZIP archive
                ZipArchiveInputStream archiveInputStream = null;
                archiveInputStream = closer.register(new ZipArchiveInputStream(new FileInputStream(importFile), Charsets.ISO_8859_1.name()));
                ArchiveEntry archiveEntry = archiveInputStream.getNextEntry();
                while (archiveEntry != null) {
                    File outputFile = null;
                    try {
                        if (archiveEntry.getName().endsWith("subscriptions.xml")) {
                            outputFile = File.createTempFile("subscriptions", "xml");
                            ByteStreams.copy(archiveInputStream, new FileOutputStream(outputFile));
    
                            // Read the OPML file
                            OpmlReader opmlReader = new OpmlReader();
                            opmlReader.read(new FileInputStream(outputFile));
                            outlineList = opmlReader.getOutlineList();
                        } else if (archiveEntry.getName().endsWith("starred.json")) {
                            outputFile = File.createTempFile("starred", "json");
                            ByteStreams.copy(archiveInputStream, new FileOutputStream(outputFile));

                            // Read the starred file
                            StarredReader starredReader = new StarredReader();
                            starredReader.setStarredArticleListener(new StarredArticleImportedListener() {
                                
                                @Override
                                public void onStarredArticleImported(StarredArticleImportedEvent event) {
                                    if (log.isInfoEnabled()) {
                                        // TODO report progress
                                        log.info(MessageFormat.format("Importing a starred article for user {0}''s import", user.getId()));
                                    }
                                    
                                    EntityManagerUtil.flush();
                                    TransactionUtil.commit();
                                    try {
                                        importFeedFromStarred(user, event.getFeed(), event.getArticle());
                                    } catch (Exception e) {
                                        if (log.isErrorEnabled()) {
                                            log.error(MessageFormat.format("Error importing article {0} from feed {1} for user {2}", event.getArticle(), event.getFeed(), user.getId()), e);
                                        }
                                    }
                                }
                            });
                            starredReader.read(new FileInputStream(outputFile));
                        }
                    } finally {
                        if (outputFile != null) {
                            try {
                                outputFile.delete();
                            } catch (Exception e) {
                                // NOP
                            }
                        }
                    }

                    archiveEntry = archiveInputStream.getNextEntry();
                }
            } else {
                // Assume the file is an OPML file
                InputStream is = closer.register(new FileInputStream(importFile));
                OpmlReader opmlReader = new OpmlReader();
                opmlReader.read(is);
                outlineList = opmlReader.getOutlineList();
            }
            
            // Raise an asynchronous import event
            if (outlineList != null || articleMap != null) {
                try {
                    importOutline(user, outlineList);
                } catch (Exception e) {
                    log.error("Unknown error during outline import", e);
                }
            }
        } catch (Exception e) {
            log.error(MessageFormat.format("Error processing import file {0}", importFile), e);
        } finally {
            try { 
                closer.close();
            } catch (IOException e) {
                // NOP
            }
            if (importFile != null) {
                importFile.delete();
            }
        }
    }

    /**
     * Import the categories and feeds.
     * 
     * @param user User
     * @param outlineList Outlines to import
     */
    private void importOutline(final User user, final List<Outline> outlineList) {
        // Flatten the OPML tree
        Map<String, List<Outline>> outlineMap = OpmlFlattener.flatten(outlineList);

        // Find all user categories
        CategoryDao categoryDao = new CategoryDao();
        List<Category> categoryList = categoryDao.findAllCategory(user.getId());
        Map<String, Category> categoryMap = new HashMap<String, Category>();
        for (Category category : categoryList) {
            categoryMap.put(category.getName(), category);
        }
        Category rootCategory = categoryMap.get(null);
        if (rootCategory == null) {
            throw new RuntimeException("Root category not found");
        }
        int categoryDisplayOrder = categoryMap.size() - 1;
        
        // Count the total number of feeds
        long feedCount = 0;
        for (List<Outline> categoryOutlineList : outlineMap.values()) {
            feedCount += categoryOutlineList.size();
        }
        
        // Create new subscriptions
        int i = 0;
        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        for (Entry<String, List<Outline>> entry : outlineMap.entrySet()) {
            String categoryName = entry.getKey();
            List<Outline> categoryOutlineList = entry.getValue();
            
            // Create a new category if necessary 
            Category category = categoryMap.get(categoryName);
            Integer feedDisplayOrder = 0;
            if (category == null) {
                category = new Category();
                category.setUserId(user.getId());
                category.setParentId(rootCategory.getId());
                category.setName(categoryName);
                category.setOrder(categoryDisplayOrder);
                categoryDao.create(category);
                
                categoryMap.put(categoryName, category);
                categoryDisplayOrder++;
            } else {
                feedDisplayOrder = feedSubscriptionDao.getCategoryCount(category.getId(), user.getId());
            }
            
            // Create the subscriptions
            EntityManagerUtil.flush();
            TransactionUtil.commit();
            for (int j = 0; j < categoryOutlineList.size(); j++) {
                if (log.isInfoEnabled()) {
                    log.info(MessageFormat.format("Importing outline {0}/{1}", i + j + 1, feedCount));
                }
                Outline outline = categoryOutlineList.get(j);
                String feedTitle = !Strings.isNullOrEmpty(outline.getText()) ? outline.getText() : outline.getTitle();
                String feedUrl = outline.getXmlUrl();

                // Check if the user is already subscribed to this feed
                FeedSubscriptionCriteria feedSubscriptionCriteria = new FeedSubscriptionCriteria();
                feedSubscriptionCriteria.setUserId(user.getId());
                feedSubscriptionCriteria.setFeedUrl(feedUrl);

                List<FeedSubscriptionDto> feedSubscriptionList = feedSubscriptionDao.findByCriteria(feedSubscriptionCriteria);
                if (!feedSubscriptionList.isEmpty()) {
                    if (log.isInfoEnabled()) {
                        log.info(MessageFormat.format("User {0} is already subscribed to the feed at URL {1}", user.getId(), feedUrl));
                    }
                    continue;
                }

                // Synchronize feed and articles
                Feed feed = null;
                final FeedService feedService = AppContext.getInstance().getFeedService();
                try {
                    feed = feedService.synchronize(feedUrl);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(MessageFormat.format("Error importing the feed at URL {0} for user {1}", feedUrl, user.getId()), e);
                    }
                    continue;
                }

                // Create the subscription if the feed can be synchronized
                try {
                    FeedSubscription feedSubscription = new FeedSubscription();
                    feedSubscription.setUserId(user.getId());
                    feedSubscription.setFeedId(feed.getId());
                    feedSubscription.setCategoryId(category.getId());
                    feedSubscription.setOrder(feedDisplayOrder);
                    feedSubscription.setTitle(feedTitle);
                    feedSubscriptionDao.create(feedSubscription);
                    
                    feedDisplayOrder++;

                    // Create the initial article subscriptions for this user
                    EntityManagerUtil.flush();
                    TransactionUtil.commit();
                    feedService.createInitialUserArticle(user.getId(), feedSubscription);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(MessageFormat.format("Error creating the subscription to the feed at URL {0} for user {1}", feedUrl, user.getId()), e);
                    }
                }
            }
            i += categoryOutlineList.size();
        }
    }
    
    /**
     * Create the feeds referenced from starred articles.
     * If some feed cannot be downloaded, a record is still created from the export data only.
     * 
     * @param user User
     * @param feed Feed to import
     */
    private void importFeedFromStarred(User user, Feed feed, Article article) {
        // Synchronize the feed
        String rssUrl = feed.getRssUrl();
        FeedDao feedDao = new FeedDao();
        Feed feedFromDb = feedDao.getByRssUrl(rssUrl.toString());
        final FeedService feedService = AppContext.getInstance().getFeedService();
        if (feedFromDb == null) {
            try {
                feedFromDb = feedService.synchronize(rssUrl);
            } catch (Exception e) {
                // Add the feed with the data from the export if it is not valid anymore
                if (log.isInfoEnabled()) {
                    log.info(MessageFormat.format("Error importing the feed at URL {0} for user {1}''s stared articles. Maybe it doens't exist anymore?", rssUrl, user.getId()), e);
                }
                feedFromDb = new Feed();
                feedFromDb.setUrl(feed.getUrl());
                feedFromDb.setRssUrl(rssUrl);
                feedFromDb.setTitle(feed.getTitle());
                feedDao.create(feedFromDb);
            }
        }
        
        // Check if the article already exists
        String title = article.getTitle();
        String url = article.getUrl();
        if (StringUtils.isBlank(title) && StringUtils.isBlank(url)) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Cannot import starred article with an empty title and url for feed {0}", rssUrl));
            }
        }
        ArticleCriteria articleCriteria = new ArticleCriteria();
        articleCriteria.setTitle(title);
        articleCriteria.setUrl(url);
        articleCriteria.setFeedId(feedFromDb.getId());
        
        ArticleDao articleDao = new ArticleDao();
        List<ArticleDto> currentArticleList = articleDao.findByCriteria(articleCriteria);
        if (!currentArticleList.isEmpty()) {
            String articleId = currentArticleList.iterator().next().getId();
            article.setId(articleId);
        } else {
            // Create the article if needed
            article.setFeedId(feedFromDb.getId());
            GuidFixer.fixGuid(article);
            articleDao.create(article);
        }
        
        // Check if the user is already subscribed to this article
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria();
        userArticleCriteria.setUserId(user.getId());
        userArticleCriteria.setArticleId(article.getId());
        
        UserArticleDao userArticleDao = new UserArticleDao();
        List<UserArticleDto> userArticleList = userArticleDao.findByCriteria(userArticleCriteria);
        UserArticleDto currentUserArticle = null;
        if (userArticleList.size() > 0) {
            currentUserArticle = userArticleList.iterator().next();
        }
        if (currentUserArticle == null || currentUserArticle.getId() == null) {
            // Subscribe the user to this article
            UserArticle userArticle = new UserArticle();
            userArticle.setUserId(user.getId());
            userArticle.setArticleId(article.getId());
            userArticle.setStarredDate(article.getPublicationDate());
            userArticle.setReadDate(article.getPublicationDate());
            userArticleDao.create(userArticle);
        } else if (currentUserArticle.getId() != null && currentUserArticle.getStarTimestamp() == null) {
            // Mark the user article as starred
            UserArticle userArticle = new UserArticle();
            userArticle.setId(currentUserArticle.getId());
            userArticle.setStarredDate(article.getPublicationDate());
            userArticle.setReadDate(article.getPublicationDate());
            userArticleDao.update(userArticle);
        }
    }
}
