package com.sismics.reader.core.event;

import com.google.common.base.Objects;
import com.sismics.reader.core.model.jpa.Article;

/**
 * Article updated event.
 *
 * @author bgamard
 */
public class ArticleUpdatedAsyncEvent {
    /**
     * Updated article.
     */
    private Article article;
    
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
                .add("article", article)
                .toString();
    }
}