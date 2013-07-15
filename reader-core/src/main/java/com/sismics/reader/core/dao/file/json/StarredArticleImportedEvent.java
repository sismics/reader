package com.sismics.reader.core.dao.file.json;

import com.google.common.base.Objects;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;

/**
 * Event raised on starred articles imported.
 *
 * @author jtremeaux 
 */
public class StarredArticleImportedEvent {
    private Feed feed;
    
    private Article article;

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

    /**
     * Getter of article.
     *
     * @return article
     */
    public Article getArticle() {
        return article;
    }

    /**
     * Setter of article.
     *
     * @param article article
     */
    public void setArticle(Article article) {
        this.article = article;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("feed", feed)
                .add("article", article)
                .toString();
    }
}
