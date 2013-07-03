package com.sismics.reader.core.listener.async;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.dao.file.opml.OpmlFlattener;
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
        final List<Outline> outlineList = subscriptionImportedEvent.getOutlineList();
        final Map<String, List<Article>> articleMap = subscriptionImportedEvent.getArticleMap();
        final List<Feed> feedList = subscriptionImportedEvent.getFeedList();
       
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                try {
                    importOutline(user, outlineList);
                } catch (Exception e) {
                    log.error("Unknown error during outline import", e);
                }
                
                // Create the feeds referenced from starred articles
                try {
                    importFeedFromStarred(user, feedList);
                } catch (Exception e) {
                    log.error("Unknown error during feed import for starred data", e);
                }
                
                // Create the user's starred articles
                try {
                    importStar(user, articleMap);
                } catch (Exception e) {
                    log.error("Unknown error during star import", e);
                }
            }

        });
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
     * @param feedList List of feeds to create
     */
    private void importFeedFromStarred(final User user, final List<Feed> feedList) {
        if (feedList == null) {
            return;
        }
        
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Importing {0} feeds for user {1}''s import", feedList.size(), user.getId()));
        }
        
        EntityManagerUtil.flush();
        int i = 0;
        for (Feed feed : feedList) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Importing feed {0}/{1} for starred articles", i + 1, feedList.size()));
            }
            String rssUrl = feed.getRssUrl();
            
            // Synchronize feed and articles
            final FeedService feedService = AppContext.getInstance().getFeedService();
            try {
                feedService.synchronize(rssUrl);
            } catch (Exception e) {
                // Add the feed with the data from the export if it is not valid anymore
                FeedDao feedDao = new FeedDao();
                Feed feedFromDb = feedDao.getByRssUrl(rssUrl.toString());
                if (feedFromDb == null) {
                    Feed newFeed = new Feed();
                    newFeed.setUrl(feed.getUrl());
                    newFeed.setRssUrl(rssUrl);
                    newFeed.setTitle(feed.getTitle());
                    feedDao.create(newFeed);
                }
                
                if (log.isInfoEnabled()) {
                    log.info(MessageFormat.format("Error importing the feed at URL {0} for user {1}''s stared articles. Maybe it doens't exist anymore?", rssUrl, user.getId()), e);
                }
                continue;
            }
            i++;
        }
    }

    /**
     * Create the articles from the starred data.
     * 
     * @param user User
     * @param articleMap Map of articles to create, indexed by RSS URL
     */
    private void importStar(final User user, final Map<String, List<Article>> articleMap) {
        if (articleMap == null) {
            return;
        }
        
        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        EntityManagerUtil.flush();
        
        // Count the total number of starred articles
        long starCount = 0;
        for (List<Article> articleList : articleMap.values()) {
            starCount += articleList.size();
        }

        int i = 0;
        for (Entry<String, List<Article>> entry : articleMap.entrySet()) {
            String rssUrl = entry.getKey();
            List<Article> articleList = entry.getValue();
            
            // Get the feed
            FeedDao feedDao = new FeedDao();
            Feed feed = feedDao.getByRssUrl(rssUrl);
            if (feed == null) {
                log.error(MessageFormat.format("Feed not found: {0}", rssUrl));
                continue;
            }
            
            // Check if the user is already subscribed to this feed
            FeedSubscriptionCriteria feedSubscriptionCriteria = new FeedSubscriptionCriteria();
            feedSubscriptionCriteria.setUserId(user.getId());
            feedSubscriptionCriteria.setFeedUrl(rssUrl);
            List<FeedSubscriptionDto> feedSubscriptionDaoList = feedSubscriptionDao.findByCriteria(feedSubscriptionCriteria);
            if (feedSubscriptionDaoList.size() == 0) {
                log.error(MessageFormat.format("User not subscribed to feed: {0}", rssUrl));
                continue;
            }
            
            for (int j = 0; j < articleList.size(); j++) {
                if (log.isInfoEnabled()) {
                    log.info(MessageFormat.format("Importing starred article {0}/{1}", i + j + 1, starCount));
                }
                
                Article article = articleList.get(j);
                // Check if the article already exists
                String title = article.getTitle();
                if (StringUtils.isBlank(title)) {
                    if (log.isInfoEnabled()) {
                        log.info(MessageFormat.format("Cannot import starred article with an empty title for feed {0}", rssUrl));
                    }
                    continue;
                }
                ArticleCriteria articleCriteria = new ArticleCriteria();
                articleCriteria.setTitle(title);
                articleCriteria.setFeedId(feed.getId());
                
                ArticleDao articleDao = new ArticleDao();
                List<ArticleDto> currentArticleList = articleDao.findByCriteria(articleCriteria);
                if (!currentArticleList.isEmpty()) {
                    String articleId = currentArticleList.iterator().next().getId();
                    article.setId(articleId);
                } else {
                    // Create the article if needed
                    article.setFeedId(feed.getId());
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
            
            i += articleList.size();
        }
    }
}
