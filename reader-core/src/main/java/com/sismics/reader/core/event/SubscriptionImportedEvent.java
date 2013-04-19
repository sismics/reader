package com.sismics.reader.core.event;

import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
import com.sismics.reader.core.dao.file.opml.Outline;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.model.jpa.User;

/**
 * Event raised on request to import an subscriptions (OPML, Google Takeout) file.
 *
 * @author jtremeaux 
 */
public class SubscriptionImportedEvent {
    /**
     * User requesting the import.
     */
    private User user;
    
    /**
     * OPML outline tree.
     */
    private List<Outline> outlineList;
    
    /**
     * Map of List<Article>, indexed by feed URL. 
     */
    private Map<String, List<Article>> articleMap;
    
    /**
     * List of feeds referenced by starred articles.
     */
    private List<Feed> feedList;
    
    /**
     * Getter of user.
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    /**
     * Setter of user.
     *
     * @param user user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Getter of outlineList.
     *
     * @return outlineList
     */
    public List<Outline> getOutlineList() {
        return outlineList;
    }

    /**
     * Setter of outlineList.
     *
     * @param outlineList outlineList
     */
    public void setOutlineList(List<Outline> outlineList) {
        this.outlineList = outlineList;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("user", user)
                .toString();
    }

    /**
     * Getter of articleMap.
     *
     * @return articleMap
     */
    public Map<String, List<Article>> getArticleMap() {
        return articleMap;
    }

    /**
     * Setter of articleMap.
     *
     * @param articleMap articleMap
     */
    public void setArticleMap(Map<String, List<Article>> articleMap) {
        this.articleMap = articleMap;
    }

    /**
     * Getter of feedList.
     *
     * @return feedList
     */
    public List<Feed> getFeedList() {
        return feedList;
    }

    /**
     * Setter of feedList.
     *
     * @param feedList feedList
     */
    public void setFeedList(List<Feed> feedList) {
        this.feedList = feedList;
    }
}
