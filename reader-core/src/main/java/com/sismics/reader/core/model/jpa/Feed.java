package com.sismics.reader.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Feed entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_FEED")
public class Feed {
    /**
     * Feed ID.
     */
    @Id
    @Column(name = "FED_ID_C", length = 36)
    private String id;
    
    /**
     * Feed RSS URL.
     */
    @Column(name = "FED_RSSURL_C", nullable = false, length = 2000)
    private String rssUrl;
    
    /**
     * Feed website URL.
     */
    @Column(name = "FED_URL_C", length = 2000)
    private String url;
    
    /**
     * Relative URI (Atom).
     */
    @Column(name = "FED_RSSBASEURI_C", length = 2000)
    private String baseUri;
    
    /**
     * Feed title.
     */
    @Column(name = "FED_TITLE_C", length = 100)
    private String title;
    
    /**
     * Feed language.
     */
    @Column(name = "FED_LANGUAGE_C", length = 10)
    private String language;

    /**
     * Feed description.
     */
    @Column(name = "FED_DESCRIPTION_C", length = 4000)
    private String description;

    /**
     * Creation date.
     */
    @Column(name = "FED_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Last fetch date.
     */
    @Column(name = "FED_LASTFETCHDATE_D")
    private Date lastFetchDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "FED_DELETEDATE_D")
    private Date deleteDate;
    
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
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter of baseUri.
     *
     * @return baseUri
     */
    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Setter of baseUri.
     *
     * @param baseUri baseUri
     */
    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
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
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter of language.
     *
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Setter of language.
     *
     * @param language language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Getter of description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter of description.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter of createDate.
     *
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     *
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Getter of lastFetchDate.
     *
     * @return lastFetchDate
     */
    public Date getLastFetchDate() {
        return lastFetchDate;
    }

    /**
     * Setter of lastFetchDate.
     *
     * @param lastFetchDate lastFetchDate
     */
    public void setLastFetchDate(Date lastFetchDate) {
        this.lastFetchDate = lastFetchDate;
    }

    /**
     * Getter of deleteDate.
     *
     * @return deleteDate
     */
    public Date getDeleteDate() {
        return deleteDate;
    }

    /**
     * Setter of deleteDate.
     *
     * @param deleteDate deleteDate
     */
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public Feed() {
    }

    public Feed(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("title", title)
                .toString();
    }
}
