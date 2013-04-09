package com.sismics.reader.core.dao.jpa.dto;


/**
 * User article DTO.
 *
 * @author jtremeaux 
 */
public class UserArticleDto {
    /**
     * User article ID.
     */
    private String id;
    
    /**
     * Date the user read this article.
     */
    private Long readTimestamp;

    /**
     * Date the user star this article.
     */
    private Long starTimestamp;
    
    /**
     * Feed title.
     */
    private String feedTitle;

    /**
     * Feed subscription ID.
     */
    private String feedSubscriptionId;
    
    /**
     * Feed subscription title.
     */
    private String feedSubscriptionTitle;
    
    /**
     * Article ID.
     */
    private String articleId;
    
    /**
     * Article URL.
     */
    private String articleUrl;

    /**
     * Article GUID.
     */
    private String articleGuid;

    /**
     * Article title.
     */
    private String articleTitle;

    /**
     * Article creator.
     */
    private String articleCreator;

    /**
     * Article description.
     */
    private String articleDescription;

    /**
     * Comment URL.
     */
    private String articleCommentUrl;

    /**
     * Comment count.
     */
    private Integer articleCommentCount;

   /**
    * Enclosure URL.
    */
   private String articleEnclosureUrl;

   /**
    * Enclosure length in bytes.
    */
   private Integer articleEnclosureLength;

   /**
    * Enclosure MIME type.
    */
   private String articleEnclosureType;

    /**
     * Publication date.
     */
    private Long articlePublicationTimestamp;

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
     * Getter of readTimestamp.
     *
     * @return readTimestamp
     */
    public Long getReadTimestamp() {
        return readTimestamp;
    }

    /**
     * Setter of readTimestamp.
     *
     * @param readTimestamp readTimestamp
     */
    public void setReadTimestamp(Long readTimestamp) {
        this.readTimestamp = readTimestamp;
    }
    
    /**
     * Getter of starTimestamp.
     *
     * @return starTimestamp
     */
    public Long getStarTimestamp() {
        return starTimestamp;
    }

    /**
     * Setter of starTimestamp.
     *
     * @param starTimestamp starTimestamp
     */
    public void setStarTimestamp(Long starTimestamp) {
        this.starTimestamp = starTimestamp;
    }

    /**
     * Getter of feedTitle.
     *
     * @return feedTitle
     */
    public String getFeedTitle() {
        return feedTitle;
    }

    /**
     * Setter of feedTitle.
     *
     * @param feedTitle feedTitle
     */
    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    /**
     * Getter of feedSubscriptionId.
     *
     * @return feedSubscriptionId
     */
    public String getFeedSubscriptionId() {
        return feedSubscriptionId;
    }

    /**
     * Setter of feedSubscriptionId.
     *
     * @param feedSubscriptionId feedSubscriptionId
     */
    public void setFeedSubscriptionId(String feedSubscriptionId) {
        this.feedSubscriptionId = feedSubscriptionId;
    }

    /**
     * Getter of feedSubscriptionTitle.
     *
     * @return feedSubscriptionTitle
     */
    public String getFeedSubscriptionTitle() {
        return feedSubscriptionTitle;
    }

    /**
     * Setter of feedSubscriptionTitle.
     *
     * @param feedSubscriptionTitle feedSubscriptionTitle
     */
    public void setFeedSubscriptionTitle(String feedSubscriptionTitle) {
        this.feedSubscriptionTitle = feedSubscriptionTitle;
    }

    /**
     * Getter of articleId.
     *
     * @return articleId
     */
    public String getArticleId() {
        return articleId;
    }

    /**
     * Setter of articleId.
     *
     * @param articleId articleId
     */
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    /**
     * Getter of articleUrl.
     *
     * @return articleUrl
     */
    public String getArticleUrl() {
        return articleUrl;
    }

    /**
     * Setter of articleUrl.
     *
     * @param articleUrl articleUrl
     */
    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }

    /**
     * Getter of articleGuid.
     *
     * @return articleGuid
     */
    public String getArticleGuid() {
        return articleGuid;
    }

    /**
     * Setter of articleGuid.
     *
     * @param articleGuid articleGuid
     */
    public void setArticleGuid(String articleGuid) {
        this.articleGuid = articleGuid;
    }

    /**
     * Getter of articleTitle.
     *
     * @return articleTitle
     */
    public String getArticleTitle() {
        return articleTitle;
    }

    /**
     * Setter of articleTitle.
     *
     * @param articleTitle articleTitle
     */
    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    /**
     * Getter of articleCreator.
     *
     * @return articleCreator
     */
    public String getArticleCreator() {
        return articleCreator;
    }

    /**
     * Setter of articleCreator.
     *
     * @param articleCreator articleCreator
     */
    public void setArticleCreator(String articleCreator) {
        this.articleCreator = articleCreator;
    }

    /**
     * Getter of articleDescription.
     *
     * @return articleDescription
     */
    public String getArticleDescription() {
        return articleDescription;
    }

    /**
     * Setter of articleDescription.
     *
     * @param articleDescription articleDescription
     */
    public void setArticleDescription(String articleDescription) {
        this.articleDescription = articleDescription;
    }

    /**
     * Getter of articleCommentUrl.
     *
     * @return articleCommentUrl
     */
    public String getArticleCommentUrl() {
        return articleCommentUrl;
    }

    /**
     * Setter of articleCommentUrl.
     *
     * @param articleCommentUrl articleCommentUrl
     */
    public void setArticleCommentUrl(String articleCommentUrl) {
        this.articleCommentUrl = articleCommentUrl;
    }

    /**
     * Getter of articleCommentCount.
     *
     * @return articleCommentCount
     */
    public Integer getArticleCommentCount() {
        return articleCommentCount;
    }

    /**
     * Setter of articleCommentCount.
     *
     * @param articleCommentCount articleCommentCount
     */
    public void setArticleCommentCount(Integer articleCommentCount) {
        this.articleCommentCount = articleCommentCount;
    }

    /**
     * Getter of articleEnclosureUrl.
     *
     * @return articleEnclosureUrl
     */
    public String getArticleEnclosureUrl() {
        return articleEnclosureUrl;
    }

    /**
     * Setter of articleEnclosureUrl.
     *
     * @param articleEnclosureUrl articleEnclosureUrl
     */
    public void setArticleEnclosureUrl(String articleEnclosureUrl) {
        this.articleEnclosureUrl = articleEnclosureUrl;
    }

    /**
     * Getter of articleEnclosureLength.
     *
     * @return articleEnclosureLength
     */
    public Integer getArticleEnclosureLength() {
        return articleEnclosureLength;
    }

    /**
     * Setter of articleEnclosureLength.
     *
     * @param articleEnclosureLength articleEnclosureLength
     */
    public void setArticleEnclosureLength(Integer articleEnclosureLength) {
        this.articleEnclosureLength = articleEnclosureLength;
    }

    /**
     * Getter of articleEnclosureType.
     *
     * @return articleEnclosureType
     */
    public String getArticleEnclosureType() {
        return articleEnclosureType;
    }

    /**
     * Setter of articleEnclosureType.
     *
     * @param articleEnclosureType articleEnclosureType
     */
    public void setArticleEnclosureType(String articleEnclosureType) {
        this.articleEnclosureType = articleEnclosureType;
    }

    /**
     * Getter of articlePublicationTimestamp.
     *
     * @return articlePublicationTimestamp
     */
    public Long getArticlePublicationTimestamp() {
        return articlePublicationTimestamp;
    }

    /**
     * Setter of articlePublicationTimestamp.
     *
     * @param articlePublicationTimestamp articlePublicationTimestamp
     */
    public void setArticlePublicationTimestamp(Long articlePublicationTimestamp) {
        this.articlePublicationTimestamp = articlePublicationTimestamp;
    }
}
