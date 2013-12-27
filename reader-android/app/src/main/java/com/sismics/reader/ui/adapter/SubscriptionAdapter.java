package com.sismics.reader.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.reader.R;
import com.sismics.reader.util.PreferenceUtil;

/**
 * Adapter for subscriptions list.
 * 
 * @author bgamard
 */
public class SubscriptionAdapter extends BaseAdapter {

    /**
     * Items in list.
     */
    private List<SubscriptionItem> items;

    /**
     * Context.
     */
    private Context context;
    
    /**
     * Header item type.
     */
    private static final int HEADER_ITEM = 0;
    
    /**
     * Category item type.
     */
    private static final int CATEGORY_ITEM = 1;
    
    /**
     * Subscription item type.
     */
    private static final int SUBSCRIPTION_ITEM = 2;
    
    /**
     * Auth token used to download favicons.
     */
    private String authToken;
    
    /**
     * AQuery.
     */
    private AQuery aq;
    
    /**
     * Constructor.
     * @param context
     * @param input
     */
    public SubscriptionAdapter(Context context, JSONObject input) {
        this.context = context;
        this.aq = new AQuery(context);
        this.authToken = PreferenceUtil.getAuthToken(context);
        
        setItems(input);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        SubscriptionItem item = getItem(position);
        
        // Inflating the right layout
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layout = R.layout.drawer_list_item_header;
            if (item.type == SUBSCRIPTION_ITEM) layout = R.layout.drawer_list_item_subscription;
            if (item.type == CATEGORY_ITEM) layout = R.layout.drawer_list_item_category;
            view = vi.inflate(layout, null);
        }
        
        // Recycling AQuery
        aq.recycle(view);
        
        // Type specific layout data
        switch (item.type) {
        case HEADER_ITEM:
            break;
        case SUBSCRIPTION_ITEM:
            if (item.id != null) {
                String faviconUrl = PreferenceUtil.getServerUrl(context) + "/api/subscription/" + item.id + "/favicon";
                Bitmap placeHolder = aq.getCachedImage(R.drawable.ic_launcher);
                aq.id(R.id.imgFavicon)
                    .image(new BitmapAjaxCallback()
                        .url(faviconUrl)
                        .fallback(R.drawable.ic_launcher)
                        .preset(placeHolder)
                        .animation(AQuery.FADE_IN_NETWORK)
                        .cookie("auth_token", authToken))
                    .margin(item.root ? 16 : 32, 0, 0, 0);
            } else {
                aq.id(R.id.imgFavicon).image(0);
            }
            break;
        case CATEGORY_ITEM:
            break;
        }
        
        // Common layout data
        aq.id(R.id.content).text(item.title);
        if (item.unreadCount == 0) {
            aq.id(R.id.unreadCount).gone();
        } else {
            aq.id(R.id.unreadCount).visible().text("" + item.unreadCount);
        }
        
        return view;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }
    
    @Override
    public int getViewTypeCount() {
        return 3;
    }
    
    @Override
    public SubscriptionItem getItem(int position) {
        try {
            return items.get(position);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public boolean isEnabled(int position) {
        SubscriptionItem item = getItem(position);
        if (item == null) {
            return false;
        }
        return item.type == SUBSCRIPTION_ITEM || item.type == CATEGORY_ITEM;
    }
    
    /**
     * Item in subscription list.
     * 
     * @author bgamard
     */
    public class SubscriptionItem {
        
        private int type;
        private String id;
        private String title;
        private String url;
        private int unreadCount;
        private boolean unread = false;
        private boolean root = false;
        
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
         * Getter de type.
         * @return type
         */
        public int getType() {
            return type;
        }

        /**
         * Setter de type.
         * @param type type
         */
        public void setType(int type) {
            this.type = type;
        }
    }

    /**
     * Clear data.
     */
    public void setItems(JSONObject input) {
        items = new ArrayList<SubscriptionItem>();
        SubscriptionItem item = null;
        
        // Adding fixed items
        item = new SubscriptionItem();
        item.type = HEADER_ITEM;
        item.title = context.getString(R.string.latest);
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = SUBSCRIPTION_ITEM;
        item.title = context.getString(R.string.unread);
        item.url = "/all";
        item.unread = true;
        item.unreadCount = input.optInt("unread_count");
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = SUBSCRIPTION_ITEM;
        item.title = context.getString(R.string.all);
        item.url = "/all";
        item.unread = false;
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = SUBSCRIPTION_ITEM;
        item.title = context.getString(R.string.starred);
        item.url = "/starred";
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = HEADER_ITEM;
        item.title = context.getString(R.string.subscriptions);
        items.add(item);
        
        // Adding categories and subscriptions
        JSONObject rootCategory = input.optJSONArray("categories").optJSONObject(0);
        JSONArray categories = rootCategory.optJSONArray("categories");
        if (categories != null) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.optJSONObject(i);
    
                item = new SubscriptionItem();
                item.type = CATEGORY_ITEM;
                item.id = category.optString("id");
                item.title = category.optString("name");
                item.url = "/category/" + item.id;
                item.unreadCount = category.optInt("unread_count");
                items.add(item);
                
                JSONArray subscriptions = category.optJSONArray("subscriptions");
                if (subscriptions != null) {
                    for (int j = 0; j < subscriptions.length(); j++) {
                        JSONObject subscription = subscriptions.optJSONObject(j);
        
                        item = new SubscriptionItem();
                        item.type = SUBSCRIPTION_ITEM;
                        item.id = subscription.optString("id");
                        item.title = subscription.optString("title");
                        item.url = "/subscription/" + item.id;
                        item.unreadCount = subscription.optInt("unread_count");
                        items.add(item);
                    }
                }
            }
        }
        
        // Root subscriptions
        JSONArray subscriptions = rootCategory.optJSONArray("subscriptions");
        if (subscriptions != null) {
            for (int j = 0; j < subscriptions.length(); j++) {
                JSONObject subscription = subscriptions.optJSONObject(j);
    
                item = new SubscriptionItem();
                item.type = SUBSCRIPTION_ITEM;
                item.id = subscription.optString("id");
                item.title = subscription.optString("title");
                item.url = "/subscription/" + item.id;
                item.unreadCount = subscription.optInt("unread_count");
                item.root = true;
                items.add(item);
            }
        }
    }
}
