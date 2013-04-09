package com.sismics.reader.core.dao.jpa.dto;

/**
 * Feed DTO.
 *
 * @author jtremeaux 
 */
public class FeedDto {
    /**
     * Feed ID.
     */
    private String id;
    
    /**
     * Feed RSS URL.
     */
    private String rssUrl;

    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of rssUrl.
     *
     * @return rssUrl
     */
    public String getRssUrl() {
        return rssUrl;
    }

    /**
     * Setter of rssUrl.
     *
     * @param rssUrl rssUrl
     */
    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }
}
