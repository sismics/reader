package com.sismics.reader.core.service;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.reader.core.dao.file.html.FeedChooserStrategy;
import com.sismics.reader.core.dao.file.html.RssExtractor;
import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.dao.jpa.ArticleDao;
import com.sismics.reader.core.dao.jpa.FeedDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.FeedSynchronizationDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.event.ArticleCreatedAsyncEvent;
import com.sismics.reader.core.event.ArticleUpdatedAsyncEvent;
import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.model.jpa.FeedSynchronization;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.util.ReaderHttpClient;
import com.sismics.reader.core.util.TransactionUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.core.util.sanitizer.ArticleSanitizer;
import com.sismics.reader.core.util.sanitizer.TextSanitizer;
import com.sismics.util.UrlUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Feed service.
 *
 * @author jtremeaux 
 */
public class FeedService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FeedService.class);

    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void shutDown() throws Exception {
    }

    @Override
    protected void runOneIteration() {
        // Don't let Guava manage our exceptions, or they will be swallowed and the service will silently stop
        try {
            TransactionUtil.handle(new Runnable() {
                @Override
                public void run() {
                    FeedDao feedDao = new FeedDao();
                    FeedSynchronizationDao feedSynchronizationDao = new FeedSynchronizationDao();
                    FeedCriteria feedCriteria = new FeedCriteria();
                    feedCriteria.setWithUserSubscription(true);
                    List<FeedDto> feedList = feedDao.findByCriteria(feedCriteria);
                    for (FeedDto feed : feedList) {
                        FeedSynchronization feedSynchronization = new FeedSynchronization();
                        feedSynchronization.setFeedId(feed.getId());
                        feedSynchronization.setSuccess(true);
                        long startTime = System.currentTimeMillis();
                        
                        try {
                            synchronize(feed.getRssUrl());
                        } catch (Exception e) {
                            log.error(MessageFormat.format("Error synchronizing feed at URL: {0}", feed.getRssUrl()), e);
                            feedSynchronization.setSuccess(false);
                            feedSynchronization.setMessage(ExceptionUtils.getStackTrace(e));
                        }
                        
                        feedSynchronization.setDuration((int) (System.currentTimeMillis() - startTime));
                        feedSynchronizationDao.create(feedSynchronization);
                        TransactionUtil.commit();
                    }
                }
            });
        } catch (Throwable t) {
            log.error("Error synchronizing feeds", t);
        }
    }
    
    @Override
    protected Scheduler scheduler() {
        // TODO Implement a better schedule strategy... Use update period specified in the feed if avail & use last update date from feed to backoff
        return Scheduler.newFixedDelaySchedule(0, 10, TimeUnit.MINUTES);
    }
    
    /**
     * Synchronize the feed to local database.
     * 
     * @param url RSS url of a feed or page containing a feed to synchronize
     * @throws Exception
     */
    public Feed synchronize(String url) throws Exception {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Synchronizing feed at URL: {0}", url));
        }
        long startTime = System.currentTimeMillis();
        
        // Parse the feed
        RssReader rssReader = parseFeedOrPage(url, true);
        Feed newFeed = rssReader.getFeed();
        List<Article> articleList = rssReader.getArticleList();
        
        // Create the feed if necessary (not created and currently in use by another user)
        FeedDao feedDao = new FeedDao();
        String rssUrl = newFeed.getRssUrl();
        Feed feed = feedDao.getByRssUrl(rssUrl);
        if (feed == null) {
            feed = new Feed();
            feed.setUrl(newFeed.getUrl());
            feed.setBaseUri(newFeed.getBaseUri());
            feed.setRssUrl(rssUrl);
            feed.setTitle(StringUtils.abbreviate(newFeed.getTitle(), 100));
            feed.setLanguage(newFeed.getLanguage() != null && newFeed.getLanguage().length() <= 10 ? newFeed.getLanguage() : null);
            feed.setDescription(StringUtils.abbreviate(newFeed.getDescription(), 4000));
            feed.setLastFetchDate(new Date());
            feedDao.create(feed);

            // Try to download the feed's favicon
            FaviconUpdateRequestedEvent faviconUpdateRequestedEvent = new FaviconUpdateRequestedEvent();
            faviconUpdateRequestedEvent.setFeed(feed);
            AppContext.getInstance().getAsyncEventBus().post(faviconUpdateRequestedEvent);
        } else {
            // Try to update the feed's favicon every week
            boolean newDay = feed.getLastFetchDate() == null ||
                    DateTime.now().getDayOfYear() != new DateTime(feed.getLastFetchDate()).getDayOfYear();
            int daysFromCreation = Days.daysBetween(Instant.now(), new Instant(feed.getCreateDate().getTime())).getDays();
            boolean updateFavicon = newDay && daysFromCreation % 7 == 0;

            // Update metadata
            feed.setUrl(newFeed.getUrl());
            feed.setBaseUri(newFeed.getBaseUri());
            feed.setTitle(StringUtils.abbreviate(newFeed.getTitle(), 100));
            feed.setLanguage(newFeed.getLanguage() != null && newFeed.getLanguage().length() <= 10 ? newFeed.getLanguage() : null);
            feed.setDescription(StringUtils.abbreviate(newFeed.getDescription(), 4000));
            feed.setLastFetchDate(new Date());
            feedDao.update(feed);

            // Update the favicon
            if (updateFavicon) {
                FaviconUpdateRequestedEvent faviconUpdateRequestedEvent = new FaviconUpdateRequestedEvent();
                faviconUpdateRequestedEvent.setFeed(feed);
                AppContext.getInstance().getAsyncEventBus().post(faviconUpdateRequestedEvent);
            }
        }
        
        // Update existing articles
        Map<String, Article> articleMap = new HashMap<String, Article>();
        for (Article article : articleList) {
            articleMap.put(article.getGuid(), article);
        }

        List<String> guidIn = new ArrayList<String>();
        for (Article article : articleList) {
            guidIn.add(article.getGuid());
        }
        
        ArticleSanitizer sanitizer = new ArticleSanitizer();
        ArticleDao articleDao = new ArticleDao();
        if (!guidIn.isEmpty()) {
            ArticleCriteria articleCriteria = new ArticleCriteria();
            articleCriteria.setFeedId(feed.getId());
            articleCriteria.setGuidIn(guidIn);
            List<ArticleDto> currentArticleDtoList = articleDao.findByCriteria(articleCriteria);
            List<Article> articleUpdatedList = new ArrayList<Article>();
            for (ArticleDto currentArticle : currentArticleDtoList) {
                Article newArticle = articleMap.remove(currentArticle.getGuid());
                
                Article article = new Article();
                article.setPublicationDate(currentArticle.getPublicationDate());
                article.setId(currentArticle.getId());
                article.setFeedId(feed.getId());
                article.setUrl(newArticle.getUrl());
                article.setTitle(StringUtils.abbreviate(TextSanitizer.sanitize(newArticle.getTitle()), 4000));
                article.setCreator(StringUtils.abbreviate(newArticle.getCreator(), 200));
                String baseUri = UrlUtil.getBaseUri(feed, newArticle);
                article.setDescription(sanitizer.sanitize(baseUri, newArticle.getDescription()));
                article.setCommentUrl(newArticle.getCommentUrl());
                article.setCommentCount(newArticle.getCommentCount());
                article.setEnclosureUrl(newArticle.getEnclosureUrl());
                article.setEnclosureLength(newArticle.getEnclosureLength());
                article.setEnclosureType(newArticle.getEnclosureType());
                
                if (!currentArticle.getTitle().equals(article.getTitle()) || !currentArticle.getDescription().equals(article.getDescription())) {
                    articleDao.update(article);
                    articleUpdatedList.add(article);
                }
            }
            
            // Update indexed article
            if (!articleUpdatedList.isEmpty()) {
                ArticleUpdatedAsyncEvent articleUpdatedAsyncEvent = new ArticleUpdatedAsyncEvent();
                articleUpdatedAsyncEvent.setArticleList(articleUpdatedList);
                AppContext.getInstance().getAsyncEventBus().post(articleUpdatedAsyncEvent);
            }
        }
        
        // Create new articles
        if (!articleMap.isEmpty()) {
            FeedSubscriptionCriteria feedSubscriptionCriteria = new FeedSubscriptionCriteria();
            feedSubscriptionCriteria.setFeedId(feed.getId());
            
            FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
            List<FeedSubscriptionDto> feedSubscriptionList = feedSubscriptionDao.findByCriteria(feedSubscriptionCriteria);
            
            UserArticleDao userArticleDao = new UserArticleDao();
            for (Article article : articleMap.values()) {
                // Create the new article
                article.setFeedId(feed.getId());
                article.setTitle(StringUtils.abbreviate(TextSanitizer.sanitize(article.getTitle()), 4000));
                article.setCreator(StringUtils.abbreviate(article.getCreator(), 200));
                String baseUri = UrlUtil.getBaseUri(feed, article);
                article.setDescription(sanitizer.sanitize(baseUri, article.getDescription()));
                if (article.getPublicationDate() == null) {
                    article.setPublicationDate(new Date());
                }
                articleDao.create(article);
    
                // Create the user articles eagerly for users already subscribed
                for (FeedSubscriptionDto feedSubscription : feedSubscriptionList) {
                    UserArticle userArticle = new UserArticle();
                    userArticle.setArticleId(article.getId());
                    userArticle.setUserId(feedSubscription.getUserId());
                    userArticleDao.create(userArticle);

                    feedSubscription.setUnreadUserArticleCount(feedSubscription.getUnreadUserArticleCount() + 1);
                    feedSubscriptionDao.updateUnreadCount(feedSubscription.getId(), feedSubscription.getUnreadUserArticleCount());
                }
            }

            // Add new articles to the index
            ArticleCreatedAsyncEvent articleCreatedAsyncEvent = new ArticleCreatedAsyncEvent();
            articleCreatedAsyncEvent.setArticleList(Lists.newArrayList(articleMap.values()));
            AppContext.getInstance().getAsyncEventBus().post(articleCreatedAsyncEvent);
        }

        long endTime = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Synchronization done in {0}ms", endTime - startTime));
        }
        
        return feed;
    }
    
    /**
     * Parse a page containing a RSS or Atom feed, or HTML linking to a feed.
     * 
     * @param url Url to parse
     * @param parsePage If true, try to parse the resource as an HTML page linking to a feed
     * @return Reader
     * @throws Exception
     */
    private RssReader parseFeedOrPage(String url, boolean parsePage) throws Exception {
        try {
            final RssReader reader = new RssReader();
            new ReaderHttpClient() {
                
                @Override
                public void process(InputStream is) throws Exception {
                    reader.readRssFeed(is);
                }
            }.open(new URL(url));
            reader.getFeed().setRssUrl(url.toString());
            return reader;
        } catch (Exception eRss) {
            boolean recoverable = !(eRss instanceof UnknownHostException ||
                    eRss instanceof FileNotFoundException);
            if (parsePage && recoverable) {
                try {
                    final RssExtractor extractor = new RssExtractor(url);
                    new ReaderHttpClient() {
                        
                        @Override
                        public void process(InputStream is) throws Exception {
                            extractor.readPage(is);
                        }
                    }.open(new URL(url));
                    List<String> feedList = extractor.getFeedList();
                    if (feedList == null || feedList.isEmpty()) {
                        logParsingError(url, eRss);
                    }
                    String feed = new FeedChooserStrategy().guess(feedList);
                    return parseFeedOrPage(feed, false);
                } catch (Exception ePage) {
                    logParsingError(url, ePage);
                }
            } else {
                logParsingError(url, eRss);
            }
            
            throw eRss;
        }
    }
    
    protected void logParsingError(String url, Exception e) {
        if (log.isInfoEnabled()) {
            if (e instanceof UnknownHostException ||
                    e instanceof FileNotFoundException ||
                    e instanceof ConnectException) {
                log.info(MessageFormat.format("Error parsing HTML page at URL {0} : {1}", url, e.getMessage()));
            } else {
                log.info(MessageFormat.format("Error parsing HTML page at URL {0}", url));
            }
        }
    }

    /**
     * Create the first batch of user articles when subscribing to a feed, so that the user has at least
     * a few unread articles.
     * 
     * @param userId User ID
     * @param feedSubscription Feed subscription
     */
    public void createInitialUserArticle(String userId, FeedSubscription feedSubscription) {
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria();
        userArticleCriteria.setUserId(userId);
        userArticleCriteria.setSubscribed(true);
        userArticleCriteria.setFeedId(feedSubscription.getFeedId());

        UserArticleDao userArticleDao = new UserArticleDao();
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(); //TODO we could fetch as many articles as in the feed, not 10
        userArticleDao.findByCriteria(userArticleCriteria, paginatedList);
        for (UserArticleDto userArticleDto : paginatedList.getResultList()) {
            if (userArticleDto.getId() == null) {
                UserArticle userArticle = new UserArticle();
                userArticle.setArticleId(userArticleDto.getArticleId());
                userArticle.setUserId(userId);
                userArticleDao.create(userArticle);
                feedSubscription.setUnreadCount(feedSubscription.getUnreadCount() + 1);
            }
        }

        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        feedSubscriptionDao.updateUnreadCount(feedSubscription.getId(), feedSubscription.getUnreadCount());
    }
}
