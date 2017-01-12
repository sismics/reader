package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.UserDto;
import com.sismics.util.jpa.ResultMapper;

import java.sql.Timestamp;

/**
 * @author jtremeaux
 */
public class UserMapper extends ResultMapper<UserDto> {
    @Override
    public UserDto map(Object[] o) {
        int i = 0;
        UserDto dto = new UserDto();
        dto.setId(stringValue(o[i++]));
        dto.setUsername(stringValue(o[i++]));
        dto.setEmail(stringValue(o[i++]));
        dto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
        dto.setLocaleId(stringValue(o[i]));

        return dto;
    }
}
