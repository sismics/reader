package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.util.jpa.ResultMapper;

import java.util.Date;

/**
 * @author jtremeaux
 */
public class ArticleMapper extends ResultMapper<ArticleDto> {
    @Override
    public ArticleDto map(Object[] o) {
        int i = 0;
        ArticleDto dto = new ArticleDto();
        dto.setId((String) o[i++]);
        dto.setUrl((String) o[i++]);
        dto.setGuid((String) o[i++]);
        dto.setTitle((String) o[i++]);
        dto.setCreator((String) o[i++]);
        dto.setDescription((String) o[i++]);
        dto.setCommentUrl((String) o[i++]);
        dto.setCommentCount((Integer) o[i++]);
        dto.setEnclosureUrl((String) o[i++]);
        dto.setEnclosureCount((Integer) o[i++]);
        dto.setEnclosureType((String) o[i++]);
        dto.setPublicationDate((Date) o[i++]);
        dto.setFeedId((String) o[i]);

        return dto;
    }
}
