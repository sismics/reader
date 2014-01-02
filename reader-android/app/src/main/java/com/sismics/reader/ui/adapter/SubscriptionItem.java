package com.sismics.reader.ui.adapter;

/**
 * Item in subscription list.
 *
 * @author bgamard
 */
public class SubscriptionItem {

    /**
     * Header item type.
     */
    public static final int HEADER_ITEM = 0;

    /**
     * Category item type.
     */
    public static final int CATEGORY_ITEM = 1;

    /**
     * Subscription item type.
     */
    public static final int SUBSCRIPTION_ITEM = 2;

    private int type;
    private String id;
    private String title;
    private String url;
    private int unreadCount;
    private boolean unread = false;
    private boolean root = false;

    /**
     * Getter of type
     * @return type
     */
    public int getType() {
        return type;
    }

    /**
     * Getter of id.
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Getter of title.
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter of url.
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Getter of unread.
     * @return unread
     */
    public boolean isUnread() {
        return unread;
    }

    /**
     * Getter of root.
     * @return root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * Getter of unreadCount.
     * @return unreadCount
     */
    public int getUnreadCount() {
        return unreadCount;
    }

    /**
     * Setter of type.
     * @param type type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Setter of id.
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Setter of title.
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Setter of url.
     * @param url url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Setter of unreadCount.
     * @param unreadCount unreadCount
     */
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * Setter of unread.
     * @param unread unread
     */
    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    /**
     * Setter of root.
     * @param root root
     */
    public void setRoot(boolean root) {
        this.root = root;
    }
}
