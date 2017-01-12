package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * @author jtremeaux
 */
public class FeedSubscriptionMapper extends ResultMapper<FeedSubscriptionDto> {
    @Override
    public FeedSubscriptionDto map(Object[] o) {
        int i = 0;
        FeedSubscriptionDto dto = new FeedSubscriptionDto();
        dto.setId(stringValue(o[i++]));
        String feedSubscriptionTitle = stringValue(o[i++]);
        dto.setUnreadUserArticleCount(intValue(o[i++]));
        dto.setCreateDate(dateValue(o[i++]));
        dto.setUserId(stringValue(o[i++]));
        dto.setFeedId(stringValue(o[i++]));
        String feedTitle = stringValue(o[i++]);
        dto.setFeedSubscriptionTitle(feedSubscriptionTitle != null ? feedSubscriptionTitle : feedTitle);
        dto.setFeedTitle(feedTitle);
        dto.setFeedRssUrl(stringValue(o[i++]));
        dto.setFeedUrl(stringValue(o[i++]));
        dto.setFeedDescription(stringValue(o[i++]));
        dto.setCategoryId(stringValue(o[i++]));
        dto.setCategoryParentId(stringValue(o[i++]));
        dto.setCategoryName(stringValue(o[i++]));
        Boolean folded = booleanValue(o[i++]);
        dto.setCategoryFolded(folded != null ? folded : false);
        dto.setSynchronizationFailCount(((Number) o[i]).intValue());

        return dto;
    }
}
