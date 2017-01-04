package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.util.jpa.ResultMapper;

import java.util.Date;

/**
 * @author jtremeaux
 */
public class FeedSubscriptionMapper extends ResultMapper<FeedSubscriptionDto> {
    @Override
    public FeedSubscriptionDto map(Object[] o) {
        int i = 0;
        FeedSubscriptionDto dto = new FeedSubscriptionDto();
        dto.setId((String) o[i++]);
        String feedSubscriptionTitle = (String) o[i++];
        dto.setUnreadUserArticleCount((Integer) o[i++]);
        dto.setCreateDate((Date) o[i++]);
        dto.setUserId((String) o[i++]);
        dto.setFeedId((String) o[i++]);
        String feedTitle = (String) o[i++];
        dto.setFeedSubscriptionTitle(feedSubscriptionTitle != null ? feedSubscriptionTitle : feedTitle);
        dto.setFeedTitle(feedTitle);
        dto.setFeedRssUrl((String) o[i++]);
        dto.setFeedUrl((String) o[i++]);
        dto.setFeedDescription((String) o[i++]);
        dto.setCategoryId((String) o[i++]);
        dto.setCategoryParentId((String) o[i++]);
        dto.setCategoryName((String) o[i++]);
        Boolean folded = (Boolean) o[i++];
        dto.setCategoryFolded(folded != null ? folded : false);
        dto.setSynchronizationFailCount(((Number) o[i]).intValue());

        return dto;
    }
}
