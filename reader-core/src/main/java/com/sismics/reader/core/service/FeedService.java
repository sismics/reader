package com.sismics.reader.core.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.reader.core.dao.file.html.FeedChooserStrategy;
import com.sismics.reader.core.dao.file.html.RssExtractor;
import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.dao.jpa.*;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.event.ArticleCreatedAsyncEvent;
import com.sismics.reader.core.event.ArticleDeletedAsyncEvent;
import com.sismics.reader.core.event.ArticleUpdatedAsyncEvent;
import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.*;
import com.sismics.reader.core.util.EntityManagerUtil;
import com.sismics.reader.core.util.TransactionUtil;
import com.sismics.reader.core.util.http.ReaderHttpClient;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.core.util.sanitizer.ArticleSanitizer;
import com.sismics.reader.core.util.sanitizer.TextSanitizer;
import com.sismics.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
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
            TransactionUtil.handle(() -> synchronizeAllFeeds());
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
     * Synchronize all feeds.
     */
    public void synchronizeAllFeeds() {
        // Update all feeds currently having subscribed users
        FeedDao feedDao = new FeedDao();
        FeedCriteria feedCriteria = new FeedCriteria()
                .setWithUserSubscription(true);
        List<FeedDto> feedList = feedDao.findByCriteria(feedCriteria);
        List<FeedSynchronization> feedSynchronizationList = new ArrayList<FeedSynchronization>();
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
            feedSynchronizationList.add(feedSynchronization);
            TransactionUtil.commit();
        }

        // If all feeds have failed, then we infer that the network is probably down
        FeedSynchronizationDao feedSynchronizationDao = new FeedSynchronizationDao();
        boolean networkDown = true;
        for (FeedSynchronization feedSynchronization : feedSynchronizationList) {
            if (feedSynchronization.isSuccess()) {
                networkDown = false;
                break;
            }
        }

        // Update the status of all synchronized feeds
        if (!networkDown) {
            for (FeedSynchronization feedSynchronization : feedSynchronizationList) {
                feedSynchronizationDao.create(feedSynchronization);
                feedSynchronizationDao.deleteOldFeedSynchronization(feedSynchronization.getFeedId(), 600);
            }
            TransactionUtil.commit();
        }
    }

    /**
     * Synchronize the feed to local database.
     * 
     * @param url RSS url of a feed or page containing a feed to synchronize
     */
    public Feed synchronize(String url) throws Exception {
        long startTime = System.currentTimeMillis();
        
        // Parse the feed
        RssReader rssReader = parseFeedOrPage(url, true);
        Feed newFeed = rssReader.getFeed();
        List<Article> articleList = rssReader.getArticleList();

        completeArticleList(articleList);

        // Get articles that were removed from RSS compared to last fetch
        List<Article> articleToRemove = getArticleToRemove(articleList);
        if (!articleToRemove.isEmpty()) {
            for (Article article : articleToRemove) {
                // Update unread counts
                // FIXME count be optimized in 1 query instead of a*s*2
                List<UserArticleDto> userArticleDtoList = new UserArticleDao()
                        .findByCriteria(new UserArticleCriteria()
                                .setArticleId(article.getId())
                                .setFetchAllFeedSubscription(true) // to test: subscribe another user, u2, read u1, not u2, u1 is decremented anyway
                                .setUnread(true));

                for (UserArticleDto userArticleDto : userArticleDtoList) {
                    FeedSubscriptionDto feedSubscriptionDto = new FeedSubscriptionDao().findFirstByCriteria(new FeedSubscriptionCriteria()
                            .setId(userArticleDto.getFeedSubscriptionId()));
                    if (feedSubscriptionDto != null) {
                        new FeedSubscriptionDao().updateUnreadCount(feedSubscriptionDto.getId(), feedSubscriptionDto.getUnreadUserArticleCount() - 1);
                    }
                }
            }

            // Delete articles that don't exist anymore
            for (Article article: articleToRemove) {
                new ArticleDao().delete(article.getId());
            }

            // Removed articles from index
            ArticleDeletedAsyncEvent articleDeletedAsyncEvent = new ArticleDeletedAsyncEvent();
            articleDeletedAsyncEvent.setArticleList(articleToRemove);
            AppContext.getInstance().getAsyncEventBus().post(articleDeletedAsyncEvent);
        }

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
            EntityManagerUtil.flush();

            // Try to download the feed's favicon
            FaviconUpdateRequestedEvent faviconUpdateRequestedEvent = new FaviconUpdateRequestedEvent();
            faviconUpdateRequestedEvent.setFeed(feed);
            AppContext.getInstance().getAsyncEventBus().post(faviconUpdateRequestedEvent);
        } else {
            // Try to update the feed's favicon every week
            boolean updateFavicon = isFaviconUpdated(feed);

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
            ArticleCriteria articleCriteria = new ArticleCriteria()
                    .setFeedId(feed.getId())
                    .setGuidIn(guidIn);
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

                if (!Strings.nullToEmpty(currentArticle.getTitle()).equals(Strings.nullToEmpty(article.getTitle())) ||
                        !Strings.nullToEmpty(currentArticle.getDescription()).equals(Strings.nullToEmpty(article.getDescription()))) {
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
            FeedSubscriptionCriteria feedSubscriptionCriteria = new FeedSubscriptionCriteria()
                    .setFeedId(feed.getId());
            
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
                articleDao.create(article);
    
                // Create the user articles eagerly for users already subscribed
                // FIXME count be optimized in 1 query instad of a*s
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
            log.info(MessageFormat.format("Synchronized feed at URL {0} in {1}ms, {2} articles added, {3} deleted", url, endTime - startTime, articleMap.size(), articleToRemove.size()));
        }
        
        return feed;
    }

    /**
     * Add missing data to articles after parsing.
     *
     * @param articleList The list of articles
     */
    private void completeArticleList(List<Article> articleList) {
        for (Article article : articleList) {
            Date now = new Date();
            if (article.getPublicationDate() == null || article.getPublicationDate().after(now)) {
                article.setPublicationDate(now);
            }
        }
    }

    /**
     * Delete articles that were removed (ninja edited) from the feed.
     *
     * @param articleList Articles just downloaded
     */
    private List<Article> getArticleToRemove(List<Article> articleList) {
        List<Article> removedArticleList = new ArrayList<Article>();
        
        // Check if the oldest article from stream was already synced
        Article oldestArticle = getOldestArticle(articleList);
        if (oldestArticle == null) {
            return removedArticleList;
        }
        ArticleDto localArticle = new ArticleDao().findFirstByCriteria(new ArticleCriteria()
                .setGuidIn(Lists.newArrayList(oldestArticle.getGuid())));
        if (localArticle == null) {
            return removedArticleList;
        }

        // Get newer articles in stream
        List<Article> newerArticles = getNewerArticleList(articleList, oldestArticle);
        Set<String> newerArticleGuids = new HashSet<String>();
        for (Article article : newerArticles) {
            newerArticleGuids.add(article.getGuid());
        }

        // Get newer articles in local DB
        List<ArticleDto> newerLocalArticles = new ArticleDao().findByCriteria(new ArticleCriteria()
                .setFeedId(localArticle.getFeedId())
                .setPublicationDateMin(oldestArticle.getPublicationDate()));

        // Delete articles removed from stream, and not too old
        Date dateMin = new DateTime().withFieldAdded(DurationFieldType.days(), -1).toDate();
        for (ArticleDto newerLocalArticle : newerLocalArticles) {
            if (!newerArticleGuids.contains(newerLocalArticle.getGuid()) && newerLocalArticle.getCreateDate().after(dateMin)) {
                removedArticleList.add(new Article(newerLocalArticle.getId()));
            }
        }
        
        return removedArticleList;
    }

    private List<Article> getNewerArticleList(List<Article> articleList, Article oldestArticle) {
        List<Article> presentArticles = new ArrayList<Article>();
        for (Article article : articleList) {
            if (article.getPublicationDate().after(oldestArticle.getPublicationDate())) {
                presentArticles.add(article);
            }
        }
        return presentArticles;
    }

    private Article getOldestArticle(List<Article> articleList) {
        Article oldestArticle = null;
        for (Article article : articleList) {
            if (oldestArticle == null || article.getPublicationDate().before(oldestArticle.getPublicationDate())) { // check me
                oldestArticle = article;
            }
        }
        return oldestArticle;
    }

    /**
     * Update the favicon once a week.
     *
     * @param feed The feed
     * @return True if the favicon must be updated
     */
    private boolean isFaviconUpdated(Feed feed) {
        boolean newDay = feed.getLastFetchDate() == null ||
                DateTime.now().getDayOfYear() != new DateTime(feed.getLastFetchDate()).getDayOfYear();
        int daysFromCreation = Days.daysBetween(Instant.now(), new Instant(feed.getCreateDate().getTime())).getDays();
        return newDay && daysFromCreation % 7 == 0;
    }

    /**
     * Parse a page containing a RSS or Atom feed, or HTML linking to a feed.
     * 
     * @param url Url to parse
     * @param parsePage If true, try to parse the resource as an HTML page linking to a feed
     * @return Reader
     */
    private RssReader parseFeedOrPage(String url, boolean parsePage) throws Exception {
        try {
            final RssReader reader = new RssReader();
            new ReaderHttpClient() {
                
                @Override
                public Void process(InputStream is) throws Exception {
                    reader.readRssFeed(is);
                    return null;
                }
            }.open(new URL(url));
            reader.getFeed().setRssUrl(url);
            return reader;
        } catch (Exception eRss) {
            boolean recoverable = !(eRss instanceof UnknownHostException ||
                    eRss instanceof FileNotFoundException);
            if (parsePage && recoverable) {
                try {
                    final RssExtractor extractor = new RssExtractor(url);
                    new ReaderHttpClient() {
                        
                        @Override
                        public Void process(InputStream is) throws Exception {
                            extractor.readPage(is);
                            return null;
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
    
    private void logParsingError(String url, Exception e) {
        if (log.isWarnEnabled()) {
            if (e instanceof UnknownHostException ||
                    e instanceof FileNotFoundException ||
                    e instanceof ConnectException) {
                log.warn(MessageFormat.format("Error parsing HTML page at URL {0} : {1}", url, e.getMessage()));
            } else {
                log.warn(MessageFormat.format("Error parsing HTML page at URL {0}", url));
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
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                .setUserId(userId)
                .setSubscribed(true)
                .setFeedId(feedSubscription.getFeedId());

        UserArticleDao userArticleDao = new UserArticleDao();
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(); //TODO we could fetch as many articles as in the feed, not 10
        userArticleDao.findByCriteria(paginatedList, userArticleCriteria, null, null);
        for (UserArticleDto userArticleDto : paginatedList.getResultList()) {
            if (userArticleDto.getId() == null) {
                UserArticle userArticle = new UserArticle();
                userArticle.setArticleId(userArticleDto.getArticleId());
                userArticle.setUserId(userId);
                userArticleDao.create(userArticle);
                feedSubscription.setUnreadCount(feedSubscription.getUnreadCount() + 1);
            } else if (userArticleDto.getReadTimestamp() == null) {
                feedSubscription.setUnreadCount(feedSubscription.getUnreadCount() + 1);
            }
        }

        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        feedSubscriptionDao.updateUnreadCount(feedSubscription.getId(), feedSubscription.getUnreadCount());
    }
}
