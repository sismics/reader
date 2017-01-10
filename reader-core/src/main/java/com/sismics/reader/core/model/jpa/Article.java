package com.sismics.reader.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.Date;

/**
 * Article entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_ARTICLE")
public class Article {
    /**
     * Article ID.
     */
    @Id
    @Column(name = "ART_ID_C", length = 36)
    private String id;
    
    /**
     * Feed ID.
     */
    @Column(name = "ART_IDFEED_C", nullable = false, length = 36)
    private String feedId;
    
    /**
     * Article URL.
     */
    @Column(name = "ART_URL_C", length = 2000)
    private String url;

    /**
     * Relative URI (Atom).
     */
    @Column(name = "ART_BASEURI_C", length = 2000)
    private String baseUri;

    /**
     * Article GUID.
     */
    @Column(name = "ART_GUID_C", nullable = false, length = 2000)
    private String guid;

    /**
     * Article title.
     */
    @Column(name = "ART_TITLE_C", length = 4000)
    private String title;

    /**
     * Article creator.
     */
    @Column(name = "ART_CREATOR_C", length = 200)
    private String creator;

    /**
     * Article description.
     */
    @Lob
    @Column(name = "ART_DESCRIPTION_C")
    private String description;

    /**
     * Comment URL.
     */
    @Column(name = "ART_COMMENTURL_C", length = 2000)
    private String commentUrl;

    /**
     * Comment count.
     */
    @Column(name = "ART_COMMENTCOUNT_N")
    private Integer commentCount;

    /**
     * Enclosure URL.
     */
    @Column(name = "ART_ENCLOSUREURL_C", length = 2000)
    private String enclosureUrl;

    /**
     * Enclosure length in bytes.
     */
    @Column(name = "ART_ENCLOSURELENGTH_N")
    private Integer enclosureLength;

    /**
     * Enclosure MIME type.
     */
    @Column(name = "ART_ENCLOSURETYPE_C", length = 2000)
    private String enclosureType;

    /**
     * Publication date.
     */
    @Column(name = "ART_PUBLICATIONDATE_D", nullable = false)
    private Date publicationDate;
    
    /**
     * Creation date.
     */
    @Column(name = "ART_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "ART_DELETEDATE_D")
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
    public void setFeedId(String feedId) {
        this.feedId = feedId;
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
     * Getter of guid.
     *
     * @return guid
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Setter of guid.
     *
     * @param guid guid
     */
    public void setGuid(String guid) {
        this.guid = guid;
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
     * Getter of creator.
     *
     * @return creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Setter of creator.
     *
     * @param creator creator
     */
    public void setCreator(String creator) {
        this.creator = creator;
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
     * Getter of commentUrl.
     *
     * @return commentUrl
     */
    public String getCommentUrl() {
        return commentUrl;
    }

    /**
     * Setter of commentUrl.
     *
     * @param commentUrl commentUrl
     */
    public void setCommentUrl(String commentUrl) {
        this.commentUrl = commentUrl;
    }

    /**
     * Getter of commentCount.
     *
     * @return commentCount
     */
    public Integer getCommentCount() {
        return commentCount;
    }

    /**
     * Setter of commentCount.
     *
     * @param commentCount commentCount
     */
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    /**
     * Getter of enclosureUrl.
     *
     * @return enclosureUrl
     */
    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    /**
     * Setter of enclosureUrl.
     *
     * @param enclosureUrl enclosureUrl
     */
    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    /**
     * Getter of enclosureLength.
     *
     * @return enclosureLength
     */
    public Integer getEnclosureLength() {
        return enclosureLength;
    }

    /**
     * Setter of enclosureLength.
     *
     * @param enclosureLength enclosureLength
     */
    public void setEnclosureLength(Integer enclosureLength) {
        this.enclosureLength = enclosureLength;
    }

    /**
     * Getter of enclosureType.
     *
     * @return enclosureType
     */
    public String getEnclosureType() {
        return enclosureType;
    }

    /**
     * Setter of enclosureType.
     *
     * @param enclosureType enclosureType
     */
    public void setEnclosureType(String enclosureType) {
        this.enclosureType = enclosureType;
    }

    /**
     * Getter of publicationDate.
     *
     * @return publicationDate
     */
    public Date getPublicationDate() {
        return publicationDate;
    }

    /**
     * Setter of publicationDate.
     *
     * @param publicationDate publicationDate
     */
    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
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

    public Article() {
    }

    public Article(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("url", url)
                .toString();
    }
}
