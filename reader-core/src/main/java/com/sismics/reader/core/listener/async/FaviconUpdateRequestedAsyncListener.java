package com.sismics.reader.core.listener.async;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.util.TransactionUtil;

/**
 * Listener on a feed favicon update request.
 * 
 * @author jtremeaux
 */
public class FaviconUpdateRequestedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FaviconUpdateRequestedAsyncListener.class);

    /**
     * Process the event.
     * 
     * @param faviconUpdateRequestedEvent OPML imported event
     * @throws Exception
     */
    @Subscribe
    public void onOpmlImport(final FaviconUpdateRequestedEvent faviconUpdateRequestedEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Favicon update requested event: {0}", faviconUpdateRequestedEvent.toString()));
        }
        
        final Feed feed = faviconUpdateRequestedEvent.getFeed();
       
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                final FeedService feedService = AppContext.getInstance().getFeedService();
                feedService.downloadFavicon(feed);
            }
        });
    }
}
