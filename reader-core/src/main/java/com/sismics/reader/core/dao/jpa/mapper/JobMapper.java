package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.JobDto;
import com.sismics.util.jpa.ResultMapper;

import java.sql.Timestamp;

/**
 * @author jtremeaux
 */
public class JobMapper extends ResultMapper<JobDto> {
    @Override
    public JobDto map(Object[] o) {
        int i = 0;
        JobDto dto = new JobDto();
        dto.setId(stringValue(o[i++]));
        dto.setName(stringValue(o[i++]));
        dto.setUserId(stringValue(o[i++]));
        Timestamp createTimestamp = (Timestamp) o[i++];
        if (createTimestamp != null) {
            dto.setCreateTimestamp(createTimestamp.getTime());
        }
        Timestamp startTimestamp = (Timestamp) o[i++];
        if (startTimestamp != null) {
            dto.setStartTimestamp(startTimestamp.getTime());
        }
        Timestamp endTimestamp = (Timestamp) o[i];
        if (endTimestamp != null) {
            dto.setEndTimestamp(endTimestamp.getTime());
        }

        return dto;
    }
}
