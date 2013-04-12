package com.sismics.reader.core.service;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.reader.core.dao.file.html.FeedChooserStrategy;
import com.sismics.reader.core.dao.file.html.RssExtractor;
import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.dao.jpa.ArticleDao;
import com.sismics.reader.core.dao.jpa.FeedDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.event.ArticleCreatedAsyncEvent;
import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.util.ArticleSanitizer;
import com.sismics.reader.core.util.TransactionUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;

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
    protected void runOneIteration() throws Exception {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                FeedDao feedDao = new FeedDao();
                FeedCriteria feedCriteria = new FeedCriteria();
                feedCriteria.setWithUserSubscription(true);
                List<FeedDto> feedList = feedDao.findByCriteria(feedCriteria);
                for (FeedDto feed : feedList) {
                    try {
                        synchronize(feed.getRssUrl());
                    } catch (Exception e) {
                        log.error(MessageFormat.format("Synchronizing feed at URL: {0}", feed.getRssUrl()), e);
                    }
                }
            }
        });
    }
    
    @Override
    protected Scheduler scheduler() {
        // TODO Implement a better schedule strategy... Use update period specified in the feed if avail & use last update date from feed to backoff
        return Scheduler.newFixedDelaySchedule(0, 10, TimeUnit.MINUTES);
    }
    
    /**
     * Synchronize the feed to local database.
     * 
     * @param rssUrl RSS url to synchronize to
     * @param Feed ID
     * @throws Exception
     */
    public String synchronize(String rssUrl) throws Exception {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Synchronizing feed at URL: {0}", rssUrl));
        }
        
        // Parse the feed
        RssReader rssReader = parseFeedOrPage(rssUrl, true);
        Feed newFeed = rssReader.getFeed();
        List<Article> articleList = rssReader.getArticleList();
        
        // Create the feed if necessary (not created and currently in use by another user)
        FeedDao feedDao = new FeedDao();
        Feed feed = feedDao.getByRssUrl(rssUrl);
        if (feed == null) {
            feed = new Feed();
            feed.setUrl(newFeed.getUrl());
            feed.setRssUrl(rssUrl);
            feed.setTitle(newFeed.getTitle());
            feed.setLanguage(newFeed.getLanguage());
            feed.setDescription(newFeed.getDescription());
            feed.setLastFetchDate(new Date());
            feedDao.create(feed);

            // Try to download the feed's favicon TODO update periodically (like 1 time /week)
            FaviconUpdateRequestedEvent faviconUpdateRequestedEvent = new FaviconUpdateRequestedEvent();
            faviconUpdateRequestedEvent.setFeed(feed);
            AppContext.getInstance().getAsyncEventBus().post(faviconUpdateRequestedEvent);
        } else {
            // Update metadata
            feed.setUrl(newFeed.getUrl());
            feed.setTitle(newFeed.getTitle());
            feed.setLanguage(newFeed.getLanguage());
            feed.setDescription(newFeed.getDescription());
            feed.setLastFetchDate(new Date());
            feedDao.update(feed);
        }
        
        // Update existing articles
        Map<String, Article> articleMap = new HashMap<>();
        for (Article article : articleList) {
            articleMap.put(article.getGuid(), article);
        }

        List<String> guidIn = new ArrayList<>();
        for (Article article : articleList) {
            guidIn.add(article.getGuid());
        }
        
        ArticleSanitizer sanitizer = new ArticleSanitizer(feed.getUrl());
        ArticleDao articleDao = new ArticleDao();
        if (!guidIn.isEmpty()) {
            ArticleCriteria articleCriteria = new ArticleCriteria();
            articleCriteria.setGuidIn(guidIn);
            List<ArticleDto> currentArticleList = articleDao.findByCriteria(articleCriteria);
            for (ArticleDto currentArticle : currentArticleList) {
                Article newArticle = articleMap.remove(currentArticle.getGuid());
                
                Article article = new Article();
                article.setId(currentArticle.getId());
                article.setUrl(newArticle.getUrl());
                article.setTitle(newArticle.getTitle());
                article.setCreator(newArticle.getCreator());
                article.setDescription(sanitizer.sanitize(newArticle.getDescription()));
                article.setCommentUrl(newArticle.getCommentUrl());
                article.setCommentCount(newArticle.getCommentCount());
                article.setEnclosureUrl(newArticle.getEnclosureUrl());
                article.setEnclosureLength(newArticle.getEnclosureLength());
                article.setEnclosureType(newArticle.getEnclosureType());
                
                // TODO Update Lucene index
                articleDao.update(article);
            }
        }
        
        // Create new articles
        for (Article article : articleMap.values()) {
            article.setFeedId(feed.getId());
            article.setDescription(sanitizer.sanitize(article.getDescription()));
            if (article.getPublicationDate() == null) {
                article.setPublicationDate(new Date());
            }
            articleDao.create(article);
        }
        
        // Add new articles to the index
        ArticleCreatedAsyncEvent articleCreatedAsyncEvent = new ArticleCreatedAsyncEvent();
        articleCreatedAsyncEvent.setArticleList(Lists.newArrayList(articleMap.values()));
        AppContext.getInstance().getAsyncEventBus().post(articleCreatedAsyncEvent);

        return feed.getId();
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
            RssReader reader = new RssReader(url);
            reader.readRssFeed();
            return reader;
        } catch (Exception eRss) {
            boolean recoverable = !(eRss instanceof UnknownHostException ||
                    eRss instanceof FileNotFoundException);
            if (parsePage && recoverable) {
                try {
                    RssExtractor extractor = new RssExtractor(url);
                    extractor.readPage();
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
        if (log.isErrorEnabled()) {
            if (e instanceof UnknownHostException ||
                    e instanceof FileNotFoundException ||
                    e instanceof ConnectException) {
                log.error(MessageFormat.format("Error parsing HTML page at URL {0} : {1}", url, e.getMessage()));
            } else {
                log.error(MessageFormat.format("Error parsing HTML page at URL {0}", url));
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
        userArticleCriteria.setUnread(false);
        userArticleCriteria.setUserId(userId);
        userArticleCriteria.setFeedId(feedSubscription.getFeedId());

        UserArticleDao userArticleDao = new UserArticleDao();
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(); //TODO we could fetch as much articles as in the feed, not 10
        userArticleDao.findByCriteria(userArticleCriteria, paginatedList);
        for (UserArticleDto userArticleDto : paginatedList.getResultList()) {
            if (userArticleDto.getId() == null) {
                UserArticle userArticle = new UserArticle();
                userArticle.setArticleId(userArticleDto.getArticleId());
                userArticle.setUserId(userId);
                String userArticleId = userArticleDao.create(userArticle);
                userArticleDto.setId(userArticleId);
            }
        }
    }
}
