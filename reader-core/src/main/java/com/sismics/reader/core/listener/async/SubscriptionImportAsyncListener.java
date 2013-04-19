package com.sismics.reader.core.listener.async;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.dao.file.opml.OpmlFlattener;
import com.sismics.reader.core.dao.file.opml.Outline;
import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.FeedDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.event.SubscriptionImportedEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Category;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.model.jpa.User;
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
                
                // Create new subscriptions
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
                    for (Outline outline : categoryOutlineList) {
                        String feedTitle = outline.getText() != null ? outline.getText() : outline.getTitle();
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

                        // Create the subscription
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
                }
                
                // Create the feed for user's starred articles
                if (feedList != null) {
                    EntityManagerUtil.flush();
                    for (Feed feed : feedList) {
                        String rssUrl = feed.getRssUrl();
                        
                        // Synchronize feed and articles
                        final FeedService feedService = AppContext.getInstance().getFeedService();
                        try {
                            feedService.synchronize(rssUrl);
                        } catch (Exception e) {
                            // Add the feed with the old data if it is not valid anymore
                            FeedDao feedDao = new FeedDao();
                            Feed feedFromDb = feedDao.getByRssUrl(rssUrl.toString());
                            if (feedFromDb == null) {
                                feed = new Feed();
                                feed.setUrl(feed.getUrl());
                                feed.setRssUrl(rssUrl);
                                feed.setTitle(feed.getTitle());
                                feedDao.create(feed);
                            }
                            
                            if (log.isErrorEnabled()) {
                                log.error(MessageFormat.format("Error importing the feed at URL {0} for user {1}''s stared articles", rssUrl, user.getId()), e);
                            }
                            continue;
                        }
                    }
                }
                
                // TODO Create the user's starred articles
                if (articleMap != null) {
                    EntityManagerUtil.flush();
                }
            }
        });
    }
}
