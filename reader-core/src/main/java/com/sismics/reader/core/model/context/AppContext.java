package com.sismics.reader.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.listener.async.*;
import com.sismics.reader.core.listener.sync.DeadEventListener;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.util.EnvironmentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Global application context.
 *
 * @author jtremeaux 
 */
public class AppContext {
    /**
     * Singleton instance.
     */
    private static AppContext instance;

    /**
     * Event bus.
     */
    private EventBus eventBus;
    
    /**
     * Generic asynchronous event bus.
     */
    private EventBus asyncEventBus;

    /**
     * Asynchronous event bus for emails.
     */
    private EventBus mailEventBus;
    
    /**
     * Asynchronous event bus for mass imports.
     */
    private EventBus importEventBus;

    /**
     * Feed service.
     */
    private FeedService feedService;
    
    /**
     * Indexing service.
     */
    private IndexingService indexingService;

    /**
     * Asynchronous executors.
     */
    private List<ExecutorService> asyncExecutorList;
    
    /**
     * Private constructor.
     */
    private AppContext() {
        resetEventBus();
        
        feedService = new FeedService();
        feedService.startAndWait();
        
        ConfigDao configDao = new ConfigDao();
        Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
        indexingService = new IndexingService(luceneStorageConfig != null ? luceneStorageConfig.getValue() : null);
        indexingService.startAndWait();
    }
    
    /**
     * (Re)-initializes the event buses.
     */
    private void resetEventBus() {
        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());
        
        asyncExecutorList = new ArrayList<ExecutorService>();
        
        asyncEventBus = newAsyncEventBus();
        asyncEventBus.register(new ArticleCreatedAsyncListener());
        asyncEventBus.register(new ArticleUpdatedAsyncListener());
        asyncEventBus.register(new ArticleDeletedAsyncListener());
        asyncEventBus.register(new RebuildIndexAsyncListener());
        asyncEventBus.register(new FaviconUpdateRequestedAsyncListener());

        mailEventBus = newAsyncEventBus();

        importEventBus = newAsyncEventBus();
        importEventBus.register(new SubscriptionImportAsyncListener());
    }

    /**
     * Returns a single instance of the application context.
     * 
     * @return Application context
     */
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }
    
    /**
     * Wait for termination of all asynchronous events.
     * /!\ Must be used only in unit tests and never a multi-user environment. 
     */
    public void waitForAsync() {
        if (EnvironmentUtil.isUnitTest()) {
            return;
        }
        try {
            for (ExecutorService executor : asyncExecutorList) {
                // Shutdown executor, don't accept any more tasks (can cause error with nested events)
                try {
                    executor.shutdown();
                    executor.awaitTermination(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    // NOP
                }
            }
        } finally {
            resetEventBus();
        }
    }

    /**
     * Creates a new asynchronous event bus.
     * 
     * @return Async event bus
     */
    private EventBus newAsyncEventBus() {
        if (EnvironmentUtil.isUnitTest()) {
            return new EventBus();
        } else {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            asyncExecutorList.add(executor);
            return new AsyncEventBus(executor);
        }
    }

    /**
     * Getter of eventBus.
     *
     * @return eventBus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Getter of asyncEventBus.
     *
     * @return asyncEventBus
     */
    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    /**
     * Getter of mailEventBus.
     *
     * @return mailEventBus
     */
    public EventBus getMailEventBus() {
        return mailEventBus;
    }

    /**
     * Getter of importEventBus.
     *
     * @return importEventBus
     */
    public EventBus getImportEventBus() {
        return importEventBus;
    }

    /**
     * Getter of feedService.
     *
     * @return feedService
     */
    public FeedService getFeedService() {
        return feedService;
    }
    
    /**
     * Getter of indexingService.
     *
     * @return indexingService
     */
    public IndexingService getIndexingService() {
        return indexingService;
    }
}
