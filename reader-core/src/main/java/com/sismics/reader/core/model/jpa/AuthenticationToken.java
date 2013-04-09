package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Authentication token entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_AUTHENTICATION_TOKEN")
public class AuthenticationToken {
    /**
     * Token.
     */
    @Id
    @Column(name = "AUT_TOKEN_C")
    private String token;

    /**
     * User ID.
     */
    @Column(name = "AUT_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Token creation date.
     */
    @Column(name = "AUT_CREATIONDATE_D", nullable = false)
    private Date creationDate;

    /**
     * Getter of token.
     *
     * @return token
     */
    public String getToken() {
        return token;
    }

    /**
     * Setter of token.
     *
     * @param token token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of creationDate.
     *
     * @return creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Setter of creationDate.
     *
     * @param creationDate creationDate
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("token", "**hidden**")
                .add("userId", userId)
                .toString();
    }
}