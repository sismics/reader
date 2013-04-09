package com.sismics.reader.core.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.reader.core.dao.file.html.FaviconExtractor;
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
import com.sismics.reader.core.util.DirectoryUtil;
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
     * Authorized favicon MIME types and corresponding file extensions.
     */
    final ImmutableMap<String, String> faviconMimeMap = new ImmutableMap.Builder<String, String>()
            .put("image/bmp", ".bmp")
            .put("image/gif", ".gif")
            .put("image/jpeg", ".jpg")
            .put("image/png", ".png")
            .put("image/x-icon", ".ico")
            .put("image/vnd.microsoft.icon", ".ico")
            .build();
    
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
                List<FeedDto> feedList = feedDao.findByCriteria(new FeedCriteria());
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
                article.setDescription(sanitize(newArticle.getDescription()));
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
            article.setDescription(sanitize(article.getDescription()));
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
     * Sanitize HTML description : removes iframes, JS etc.
     * 
     * @param html HTML to sanitize
     * @return Sanitized HTML
     */
    private String sanitize(String html) {
        // Allow YouTube iframes
        PolicyFactory videoPolicyFactory = new HtmlPolicyBuilder()
                .allowStandardUrlProtocols()
                .allowElements("iframe")
                .allowAttributes("src")
                .matching(Pattern.compile("http://(www.)?youtube.com/embed/.+"))
                .onElements("iframe")
                .disallowWithoutAttributes("iframe")
                .toFactory();

        PolicyFactory policy = Sanitizers.BLOCKS
                .and(Sanitizers.FORMATTING)
                .and(Sanitizers.IMAGES)
                .and(Sanitizers.LINKS)
                .and(Sanitizers.STYLES)
                .and(videoPolicyFactory);
        
        String safeHtml = policy.sanitize(html);
        
        return safeHtml;
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
    
    /**
     * Attempt to download the favicon from a feed's webpage.
     * 
     * @param feed Feed
     */
    public void downloadFavicon(Feed feed) {
        // Try to extract the favicon URL from the page specified in the feed
        String faviconUrl = null;
        try {
            FaviconExtractor extractor = new FaviconExtractor(feed.getUrl());
            extractor.readPage();
            faviconUrl = extractor.getFavicon();
        } catch (Exception e) {
            log.error("Error extracting icon from feed HTML page", e);
        }
        
        // Attempt to download a valid favicon from the HTML page 
        String localFilename = null;
        if (faviconUrl != null) {
            localFilename = downloadFavicon(faviconUrl, feed.getId());
        }

        // Attempt to download a valid favicon from guessed URLs 
        final List<String> filenameList = ImmutableList.of(
                "favicon.png", "favicon.gif", "favicon.ico", "favicon.jpg", "favicon.jpeg", "favicon.bmp");
        Iterator<String> iterator = filenameList.iterator(); 
        while (localFilename == null && iterator.hasNext()) {
            String filename = iterator.next();
            faviconUrl = getFaviconUrl(feed, filename);
            localFilename = downloadFavicon(faviconUrl, feed.getId());
        }
        
        if (log.isInfoEnabled()) {
            if (localFilename != null) {
                log.info(MessageFormat.format("Favicon successfully downloaded to {0}", localFilename));
            } else {
                log.info(MessageFormat.format("Cannot find a valid favicon for feed {0} at page {1}", feed.getId(), feed.getUrl()));
            }
        }
    }

    /**
     * Constructs a favicon URL from a feed path, and a guessed of the favicon file name (e.g. "favicon.ico").
     * 
     * @param feed Feed
     * @param fileName Favicon file name
     * @return Favicon URL
     */
    private String getFaviconUrl(Feed feed, String fileName) {
        String feedUrl = feed.getUrl();
        
        if (feedUrl != null) {
            try {
                URL url = new URL(feedUrl);
                return new URL(url.getProtocol(), url.getHost(), url.getPort(), "/" + fileName).toString();
            } catch (MalformedURLException e) {
                if (log.isErrorEnabled()) {
                    log.error(MessageFormat.format("Error building Favicon URL from URL {0} with filename {1}", feedUrl, fileName));
                }
            }
        }
        return null;
    }

    /**
     * Attempts to download a favicon from an URL.
     * 
     * @param faviconUrl URL to download the favicon from
     * @param feedId Feed ID
     * @return Local file path or null if failed
     */
    private String downloadFavicon(String faviconUrl, String feedId) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(faviconUrl).openConnection();
            String type = connection.getContentType();
    
            String extension = faviconMimeMap.get(type);
            if (extension != null) {
                File faviconDirectory = DirectoryUtil.getFaviconDirectory();
                File outputFile = new File(faviconDirectory + File.separator + feedId + extension);
                ByteStreams.copy(connection.getInputStream(), new FileOutputStream(outputFile));
                return outputFile.getPath();
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(MessageFormat.format(MessageFormat.format("Error downloading favicon at URL {0}", faviconUrl), e));
            }
        }
        return null;
    }
}
