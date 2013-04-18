package com.sismics.reader.core.dao.file.json;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.JsonValidationUtil;

/**
 * Reads a starred.json file from Google Takeout.
 *
 * @author jtremeaux 
 */
public class StarredReader {

    /**
     * Map of List<Article>, indexed by feed URL. 
     */
    private Map<String, List<Article>> articleMap;
    
    /**
     * Map of feeds, indexed by feed URL.
     */
    private Map<String, Feed> feedMap;
    
    /**
     * Constructor of StarredReader.
     */
    public StarredReader() {
        articleMap = new HashMap<String, List<Article>>();
        feedMap = new HashMap<String, Feed>();
    }
    
    /**
     * Reads a starred file.
     * 
     * @param is JSON input stream
     * @throws Exception
     */
    public void read(InputStream is) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(is);

        // Validate root node
        JsonValidationUtil.validateJsonString(rootNode, "id", true);
        JsonValidationUtil.validateJsonString(rootNode, "title", true);
        JsonValidationUtil.validateJsonString(rootNode, "author", true);
        JsonValidationUtil.validateJsonArray(rootNode, "items", true);
        
        // Iterate over starred items
        ArrayNode items = (ArrayNode) rootNode.get("items");
        for (JsonNode itemNode : items) {
            // Extract the feed data
            JsonValidationUtil.validateJsonObject(itemNode, "origin", true);
            JsonNode origin = itemNode.path("origin");
            
            JsonValidationUtil.validateJsonString(origin, "streamId", true);
            String feedRssUrl = origin.path("streamId").getTextValue();
            
            JsonValidationUtil.validateJsonString(origin, "title", true);
            String feedTitle = origin.path("title").getTextValue();

            JsonValidationUtil.validateJsonString(origin, "htmlUrl", true);
            String feedUrl = origin.path("htmlUrl").getTextValue();
            
            Feed feed = feedMap.get(feedRssUrl);
            if (feed == null) {
                feed = new Feed();
                feed.setRssUrl(feedRssUrl);
                feed.setTitle(feedTitle);
                feed.setUrl(feedUrl);
                feedMap.put(feedRssUrl, feed);
            }

            // Extract the article data
            JsonValidationUtil.validateJsonString(itemNode, "id", true);
//            String id = itemNode.path("id").getTextValue();
            
            JsonValidationUtil.validateJsonString(itemNode, "title", true);
            String title = itemNode.path("title").getTextValue();
            
            JsonValidationUtil.validateJsonNumber(itemNode, "published", true);
            long publicationDate = itemNode.path("published").getLongValue();

            JsonValidationUtil.validateJsonArray(itemNode, "alternate", false);
            String url = null;
            if (itemNode.has("alternate")) {
                ArrayNode alternate = (ArrayNode) itemNode.path("alternate");
                
                for (JsonNode alternateNode : alternate) {
                    JsonValidationUtil.validateJsonString(alternateNode, "href", true);
                    url = alternate.path("href").getTextValue();
                }
            }

            List<Article> articleList = articleMap.get(feedRssUrl);
            if (articleList == null) {
                articleList = new ArrayList<Article>();
                articleMap.put(feedUrl, articleList);
            }
            
            Article article = new Article();
            article.setTitle(title);
            article.setPublicationDate(new Date(publicationDate));
            article.setUrl(url);
            articleList.add(article);
        }
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
     * Getter of feedMap.
     *
     * @return feedMap
     */
    public Map<String, Feed> getFeedMap() {
        return feedMap;
    }
}
