package com.sismics.reader.core.model.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.store.Directory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.listener.async.FaviconUpdateRequestedAsyncListener;
import com.sismics.reader.core.listener.async.OpmlImportAsyncListener;
import com.sismics.reader.core.listener.sync.DeadEventListener;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.service.IndexingService;

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
    private AsyncEventBus asyncEventBus;

    /**
     * Asynchronous event bus for emails.
     */
    private AsyncEventBus mailEventBus;
    
    /**
     * Asynchronous event bus for mass imports.
     */
    private AsyncEventBus importEventBus;

    /**
     * Feed service.
     */
    private FeedService feedService;
    
    /**
     * Indexing service.
     */
    private IndexingService indexingService;

    /**
     * Lucene directory.
     */
    private Directory luceneDirectory;
    
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
        
        indexingService = new IndexingService();
        indexingService.startAndWait();
        
        luceneDirectory = indexingService.getDirectory();
    }
    
    /**
     * (Re)-initializes the event buses.
     */
    private void resetEventBus() {
        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());
        
        asyncExecutorList = new ArrayList<ExecutorService>();
        
        ExecutorService asyncExecutor = Executors.newSingleThreadExecutor(); 
        asyncExecutorList.add(asyncExecutor);
        asyncEventBus = new AsyncEventBus(asyncExecutor);
//        asyncEventBus.register(new ArticleCreatedAsyncListener());
        asyncEventBus.register(new FaviconUpdateRequestedAsyncListener());

        ExecutorService mailExecutor = Executors.newSingleThreadExecutor(); 
        asyncExecutorList.add(mailExecutor);
        mailEventBus = new AsyncEventBus(mailExecutor);

        ExecutorService importExecutor = Executors.newSingleThreadExecutor(); 
        asyncExecutorList.add(importExecutor);
        importEventBus = new AsyncEventBus(importExecutor);
        importEventBus.register(new OpmlImportAsyncListener());
    }

    /**
     * Returns a single instance of the application context.
     * 
     * @return Contexte applicatif
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
        try {
            for (ExecutorService executor : asyncExecutorList) {
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
    public AsyncEventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    /**
     * Getter of mailEventBus.
     *
     * @return mailEventBus
     */
    public AsyncEventBus getMailEventBus() {
        return mailEventBus;
    }

    /**
     * Getter of importEventBus.
     *
     * @return importEventBus
     */
    public AsyncEventBus getImportEventBus() {
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
     * Getter of feedService.
     *
     * @return feedService
     */
    public IndexingService getIndexingService() {
        return indexingService;
    }

    /**
     * Getter of- luceneDirectory.
     *
     * @return the luceneDirectory
     */
    public Directory getLuceneDirectory() {
        return luceneDirectory;
    }
}
