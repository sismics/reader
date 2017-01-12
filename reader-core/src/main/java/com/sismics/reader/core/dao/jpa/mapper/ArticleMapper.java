package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * @author jtremeaux
 */
public class ArticleMapper extends ResultMapper<ArticleDto> {
    @Override
    public ArticleDto map(Object[] o) {
        int i = 0;
        ArticleDto dto = new ArticleDto();
        dto.setId(stringValue(o[i++]));
        dto.setUrl(stringValue(o[i++]));
        dto.setGuid(stringValue(o[i++]));
        dto.setTitle(stringValue(o[i++]));
        dto.setCreator(stringValue(o[i++]));
        dto.setDescription(stringValue(o[i++]));
        dto.setCommentUrl(stringValue(o[i++]));
        dto.setCommentCount(intValue(o[i++]));
        dto.setEnclosureUrl(stringValue(o[i++]));
        dto.setEnclosureCount(intValue(o[i++]));
        dto.setEnclosureType(stringValue(o[i++]));
        dto.setPublicationDate(dateValue(o[i++]));
        dto.setCreateDate(dateValue(o[i++]));
        dto.setFeedId(stringValue(o[i]));

        return dto;
    }
}
