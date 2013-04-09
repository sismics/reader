package com.sismics.reader.core.event;

import java.util.List;

import com.google.common.base.Objects;
import com.sismics.reader.core.dao.file.opml.Outline;
import com.sismics.reader.core.model.jpa.User;

/**
 * Event raised on request to import an OPML file.
 *
 * @author jtremeaux 
 */
public class OpmlImportedEvent {
    /**
     * User requesting the import.
     */
    private User user;
    
    /**
     * OPML outline tree.
     */
    private List<Outline> outlineList;
    
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

    /**
     * Getter of outlineList.
     *
     * @return outlineList
     */
    public List<Outline> getOutlineList() {
        return outlineList;
    }

    /**
     * Setter of outlineList.
     *
     * @param outlineList outlineList
     */
    public void setOutlineList(List<Outline> outlineList) {
        this.outlineList = outlineList;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("user", user)
                .toString();
    }
}
