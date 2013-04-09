package com.sismics.reader.core.event;

import com.sismics.reader.core.model.jpa.User;

/**
 * Event raised after the user changes his password.
 *
 * @author jtremeaux 
 */
public class PasswordChangedEvent {
    /**
     * Created user.
     */
    private User user;

    /**
     * Getter of user.
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    /**
     * Setter of user.
     *
     * @param user user
     */
    public void setUser(User user) {
        this.user = user;
    }
}
