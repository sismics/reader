package com.sismics.reader.core.dao.file.json;

/**
 * Listener for starred item import events.
 *
 * @author jtremeaux 
 */
public interface StarredArticleImportedListener {
    /**
     * Invoked when a new starred item is imported.
     * 
     * @param event Event
     */
    void onStarredArticleImported(StarredArticleImportedEvent event);
}
