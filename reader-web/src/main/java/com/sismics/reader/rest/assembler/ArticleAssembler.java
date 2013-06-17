package com.sismics.reader.rest.assembler;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;

/**
 * Article DTO / JSON assembler.
 *
 * @author jtremeaux 
 */
public class ArticleAssembler {

    /**
     * Returns a user article as JSON data.
     * 
     * @param userArticle User article
     * @return User article as JSON
     */
    public static JSONObject asJson(UserArticleDto userArticle) throws JSONException {
        JSONObject userArticleJson = new JSONObject();
        userArticleJson.put("id", userArticle.getId());
        JSONObject subscription = new JSONObject();
        subscription.put("id", userArticle.getFeedSubscriptionId());
        subscription.put("title", userArticle.getFeedSubscriptionTitle() != null ? userArticle.getFeedSubscriptionTitle() : userArticle.getFeedTitle());
        userArticleJson.put("subscription", subscription);
        userArticleJson.put("title", userArticle.getArticleTitle());
        userArticleJson.put("url", userArticle.getArticleUrl());
        userArticleJson.put("date", userArticle.getArticlePublicationTimestamp());
        userArticleJson.put("creator", userArticle.getArticleCreator());
        userArticleJson.put("description", userArticle.getArticleDescription());
        userArticleJson.put("comment_url", userArticle.getArticleCommentUrl());
        userArticleJson.put("comment_count", userArticle.getArticleCommentCount());
        if (userArticle.getArticleEnclosureUrl() != null) {
            JSONObject enclosure = new JSONObject();
            enclosure.put("url", userArticle.getArticleEnclosureUrl());
            enclosure.put("length", userArticle.getArticleEnclosureLength());
            enclosure.put("type", userArticle.getArticleEnclosureType());
            userArticleJson.put("enclosure", enclosure);
        }
        userArticleJson.put("is_read", userArticle.getReadTimestamp() != null);
        userArticleJson.put("is_starred", userArticle.getStarTimestamp() != null);
        return userArticleJson;
    }
}
