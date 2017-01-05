package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.JobEventDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * @author jtremeaux
 */
public class JobEventMapper extends ResultMapper<JobEventDto> {
    @Override
    public JobEventDto map(Object[] o) {
        int i = 0;
        JobEventDto dto = new JobEventDto();
        dto.setId((String) o[i++]);
        dto.setName((String) o[i++]);
        dto.setValue((String) o[i++]);

        return dto;
    }
}
