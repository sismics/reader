package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * @author jtremeaux
 */
public class FeedMapper extends ResultMapper<FeedDto> {
    @Override
    public FeedDto map(Object[] o) {
        int i = 0;
        FeedDto dto = new FeedDto();
        dto.setId(stringValue(o[i++]));
        dto.setRssUrl(stringValue(o[i]));

        return dto;
    }
}
