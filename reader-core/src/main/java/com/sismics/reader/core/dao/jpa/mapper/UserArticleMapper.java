package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.util.jpa.ResultMapper;

import java.sql.Timestamp;

/**
 * @author jtremeaux
 */
public class UserArticleMapper extends ResultMapper<UserArticleDto> {
    @Override
    public UserArticleDto map(Object[] o) {
        int i = 0;
        UserArticleDto dto = new UserArticleDto();
        dto.setId(stringValue(o[i++]));
        Timestamp readTimestamp = (Timestamp) o[i++];
        if (readTimestamp != null) {
            dto.setReadTimestamp(readTimestamp.getTime());
        }
        Timestamp starTimestamp = (Timestamp) o[i++];
        if (starTimestamp != null) {
            dto.setStarTimestamp(starTimestamp.getTime());
        }
        dto.setFeedTitle(stringValue(o[i++]));
        dto.setFeedSubscriptionId(stringValue(o[i++]));
        dto.setFeedSubscriptionTitle(stringValue(o[i++]));
        dto.setArticleId(stringValue(o[i++]));
        dto.setArticleUrl(stringValue(o[i++]));
        dto.setArticleGuid(stringValue(o[i++]));
        dto.setArticleTitle(stringValue(o[i++]));
        dto.setArticleCreator(stringValue(o[i++]));
        dto.setArticleDescription(stringValue(o[i++]));
        dto.setArticleCommentUrl(stringValue(o[i++]));
        dto.setArticleCommentCount(intValue(o[i++]));
        dto.setArticleEnclosureUrl(stringValue(o[i++]));
        dto.setArticleEnclosureLength(intValue(o[i++]));
        dto.setArticleEnclosureType(stringValue(o[i++]));
        dto.setArticlePublicationTimestamp(((Timestamp) o[i]).getTime());

        return dto;
    }
}
