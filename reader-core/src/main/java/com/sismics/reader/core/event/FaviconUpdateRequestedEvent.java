package com.sismics.reader.core.event;

import com.google.common.base.Objects;
import com.sismics.reader.core.model.jpa.Feed;

/**
 * Event raised on request to update a feed favicon.
 *
 * @author jtremeaux 
 */
public class FaviconUpdateRequestedEvent {
    /**
     * Feed to update.
     */
    private Feed feed;
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("feedId", feed.getId())
                .toString();
    }

    /**
     * Getter of feed.
     *
     * @return feed
     */
    public Feed getFeed() {
        return feed;
    }

    /**
     * Setter of feed.
     *
     * @param feed feed
     */
    public void setFeed(Feed feed) {
        this.feed = feed;
    }
}
