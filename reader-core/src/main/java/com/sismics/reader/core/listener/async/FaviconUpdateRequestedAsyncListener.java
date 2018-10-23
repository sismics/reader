package com.sismics.reader.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.dao.file.html.FaviconDownloader;
import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.util.DirectoryUtil;
import com.sismics.reader.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

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
     */
    @Subscribe
    public void onFaviconUpdateRequested(final FaviconUpdateRequestedEvent faviconUpdateRequestedEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Favicon update requested event: {0}", faviconUpdateRequestedEvent.toString()));
        }
        
        final Feed feed = faviconUpdateRequestedEvent.getFeed();
       
        TransactionUtil.handle(() -> {
            String faviconDirectory = DirectoryUtil.getFaviconDirectory().getPath();
            FaviconDownloader downloader = new FaviconDownloader();

            String localFilename = null;
            if (feed.getUrl() != null) {
                // Try with the feed URL if available
                downloader.downloadFaviconFromPage(feed.getUrl(), faviconDirectory, feed.getId());
            }

            if (localFilename == null) {
                // If nothing is found, try again with the RSS URL
                downloader.downloadFaviconFromPage(feed.getRssUrl(), faviconDirectory, feed.getId());
            }
        });
    }
}
