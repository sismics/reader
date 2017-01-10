package com.sismics.reader.core.dao.jpa.criteria;

import java.util.Date;
import java.util.List;

/**
 * Feed criteria.
 *
 * @author jtremeaux 
 */
public class ArticleCriteria {
    /**
     * Article ID.
     */
    private String id;
    
    /**
     * Article GUID list (inclusive).
     */
    private List<String> guidIn;
    
    /**
     * Article title.
     */
    private String title;

    /**
     * Article url.
     */
    private String url;

    /**
     * Max publication date.
     */
    private Date publicationDateMin;

    /**
     * Feed ID.
     */
    private String feedId;

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
    public ArticleCriteria setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter of guidIn.
     *
     * @return guidIn
     */
    public List<String> getGuidIn() {
        return guidIn;
    }

    /**
     * Setter of guidIn.
     *
     * @param guidIn guidIn
     */
    public ArticleCriteria setGuidIn(List<String> guidIn) {
        this.guidIn = guidIn;
        return this;
    }

    /**
     * Getter of title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter of title.
     *
     * @param title title
     */
    public ArticleCriteria setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Getter of url.
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter of url.
     *
     * @param url url
     */
    public ArticleCriteria setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Getter of publicationDateMin.
     *
     * @return publicationDateMin
     */
    public Date getPublicationDateMin() {
        return publicationDateMin;
    }

    /**
     * Setter of publicationDateMin.
     *
     * @param publicationDateMin publicationDateMin
     */
    public ArticleCriteria setPublicationDateMin(Date publicationDateMin) {
        this.publicationDateMin = publicationDateMin;
        return this;
    }

    /**
     * Getter of feedId.
     *
     * @return feedId
     */
    public String getFeedId() {
        return feedId;
    }

    /**
     * Setter of feedId.
     *
     * @param feedId feedId
     */
    public ArticleCriteria setFeedId(String feedId) {
        this.feedId = feedId;
        return this;
    }
}
