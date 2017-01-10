package com.sismics.reader.core.event;

import com.google.common.base.Objects;
import com.sismics.reader.core.model.jpa.Article;

import java.util.List;

/**
 * Articles deleted event.
 *
 * @author bgamard
 */
public class ArticleDeletedAsyncEvent {
    /**
     * List of deleted articles.
     */
    private List<Article> articleList;
    
    /**
     * Getter of articleList.
     *
     * @return articleList
     */
    public List<Article> getArticleList() {
        return articleList;
    }

    /**
     * Setter of articleList.
     *
     * @param articleList articleList
     */
    public void setArticleList(List<Article> articleList) {
        this.articleList = articleList;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("articles", (articleList != null ? articleList.size() : "0") + " articles")
                .toString();
    }
}