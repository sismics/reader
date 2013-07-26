package com.sismics.reader.core.dao.jpa.dto;

/**
 * Job DTO.
 *
 * @author jtremeaux 
 */
public class JobEventDto {
    /**
     * Job ID.
     */
    private String id;

    /**
     * Job event name.
     */
    private String name;

    /**
     * Job event value.
     */
    private String value;

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
     * Getter of name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter of name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter of value.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Setter of value.
     *
     * @param value value
     */
    public void setValue(String value) {
        this.value = value;
    }

}
