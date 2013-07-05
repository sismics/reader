package com.sismics.reader.core.dao.file.json;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.JsonValidationUtil;

/**
 * Reads a starred.json file from Google Takeout.
 *
 * @author jtremeaux 
 */
public class StarredReader {
    private static final Logger log = LoggerFactory.getLogger(StarredReader.class);


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
            if (feedRssUrl.startsWith("feed/")) {
                feedRssUrl = feedRssUrl.substring("feed/".length());
            }
            
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
            long publicationDate = itemNode.path("published").getLongValue() * 1000;

            JsonValidationUtil.validateJsonArray(itemNode, "alternate", false);
            String url = null;
            if (itemNode.has("alternate")) {
                ArrayNode alternate = (ArrayNode) itemNode.path("alternate");
                
                for (JsonNode alternateNode : alternate) {
                    JsonValidationUtil.validateJsonString(alternateNode, "href", true);
                    url = alternate.path("href").getTextValue();
                }
            }

            String description = null;
            if (itemNode.has("summary")) {
                ObjectNode summaryNode = (ObjectNode) itemNode.path("summary");
                if (summaryNode.has("content")) {
                    JsonValidationUtil.validateJsonString(summaryNode, "content", true);
                    description = summaryNode.path("content").getTextValue();
                }
            }
            
            if (itemNode.has("content")) {
                ObjectNode contentNode = (ObjectNode) itemNode.path("content");
                if (contentNode.has("content")) {
                    JsonValidationUtil.validateJsonString(contentNode, "content", true);
                    description = contentNode.path("content").getTextValue();
                }
            }
            
            if (description == null) {
                if (log.isInfoEnabled()) {
                    log.info(MessageFormat.format("Content not found for starred article: {0}", title));
                }
                continue;
            }

            List<Article> articleList = articleMap.get(feedRssUrl);
            if (articleList == null) {
                articleList = new ArrayList<Article>();
                articleMap.put(feedRssUrl, articleList);
            }
            
            
            Article article = new Article();
            article.setTitle(title);
            article.setPublicationDate(new Date(publicationDate));
            article.setUrl(url);
            article.setDescription(description);
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
     * Returns the list of feeds referenced by starred articles.
     *
     * @return List of feeds
     */
    public List<Feed> getFeedList() {
        return new ArrayList<Feed>(feedMap.values());
    }
}
