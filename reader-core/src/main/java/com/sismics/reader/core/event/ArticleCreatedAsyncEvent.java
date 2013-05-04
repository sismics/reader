package com.sismics.reader.core.event;

import java.util.List;

import com.google.common.base.Objects;
import com.sismics.reader.core.model.jpa.Article;

/**
 * New articles created event.
 *
 * @author bgamard
 */
public class ArticleCreatedAsyncEvent {
    /**
     * List of newly created articles.
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