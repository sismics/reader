package com.sismics.reader.core.dao.file.json;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.JsonValidationUtil;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;

/**
 * Reads a starred.json file from Google Takeout.
 *
 * @author jtremeaux 
 */
public class StarredReader {
    private static final Logger log = LoggerFactory.getLogger(StarredReader.class);

    private StarredArticleImportedListener starredArticleImportedListener;
    
    /**
     * Reads a starred file.
     * 
     * @param is JSON input stream
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
            
            JsonValidationUtil.validateJsonString(origin, "htmlUrl", true);
            String feedUrl = origin.path("htmlUrl").getTextValue();
            
            JsonValidationUtil.validateJsonString(origin, "title", false);
            String feedTitle = origin.path("title").getTextValue();

            // Extract the article data
            JsonValidationUtil.validateJsonString(itemNode, "id", true);
//            String id = itemNode.path("id").getTextValue();
            
            JsonValidationUtil.validateJsonString(itemNode, "title", false);
            String title = itemNode.path("title").getTextValue();
            
            JsonValidationUtil.validateJsonNumber(itemNode, "published", true);
            long publicationDate = itemNode.path("published").getLongValue() * 1000;

            JsonValidationUtil.validateJsonArray(itemNode, "alternate", false);
            String url = null;
            if (itemNode.has("alternate")) {
                ArrayNode alternate = (ArrayNode) itemNode.path("alternate");
                
                for (JsonNode alternateNode : alternate) {
                    JsonValidationUtil.validateJsonString(alternateNode, "href", true);
                    url = alternateNode.path("href").getTextValue();
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

            // Raise a starred article imported event
            if (starredArticleImportedListener != null) {
                Feed feed = new Feed();
                feed.setRssUrl(feedRssUrl);
                feed.setTitle(feedTitle);
                feed.setUrl(feedUrl);
    
                Article article = new Article();
                article.setTitle(title);
                article.setPublicationDate(new Date(publicationDate));
                article.setUrl(url);
                article.setDescription(description);
    
                StarredArticleImportedEvent event = new StarredArticleImportedEvent();
                event.setFeed(feed);
                event.setArticle(article);
                starredArticleImportedListener.onStarredArticleImported(event);
            }
        }
    }

    /**
     * Setter of starredArticleListener.
     *
     * @param starredArticleListener starredArticleListener
     */
    public void setStarredArticleListener(StarredArticleImportedListener starredArticleListener) {
        this.starredArticleImportedListener = starredArticleListener;
    }
}
