package com.sismics.reader.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sismics.reader.R;

/**
 * Adapter for subscriptions list.
 * 
 * @author bgamard
 */
public class SubscriptionAdapter extends BaseAdapter {

    /**
     * Items in list.
     */
    private List<SubscriptionItem> items = new ArrayList<SubscriptionItem>();

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
     * Constructor.
     * @param context
     * @param input
     */
    public SubscriptionAdapter(Context context, JSONObject input) {
        this.context = context;
        SubscriptionItem item = null;
        
        // Adding fixed items
        item = new SubscriptionItem();
        item.type = HEADER_ITEM;
        item.title = "Latest";
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = SUBSCRIPTION_ITEM;
        item.title = "Unread";
        item.url = "/all";
        item.unread = true;
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = SUBSCRIPTION_ITEM;
        item.title = "All";
        item.url = "/all";
        item.unread = false;
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = SUBSCRIPTION_ITEM;
        item.title = "Starred";
        item.url = "/starred";
        items.add(item);
        
        // Adding categories and subscriptions
        JSONObject rootCategory = input.optJSONArray("categories").optJSONObject(0);
        JSONArray categories = rootCategory.optJSONArray("categories");
        for (int i = 0; i < categories.length(); i++) {
            JSONObject category = categories.optJSONObject(i);

            item = new SubscriptionItem();
            item.type = CATEGORY_ITEM;
            item.id = category.optString("id");
            item.title = category.optString("name");
            item.url = "/category/" + item.id;
            items.add(item);
            
            JSONArray subscriptions = category.optJSONArray("subscriptions");
            for (int j = 0; j < subscriptions.length(); j++) {
                JSONObject subscription = subscriptions.optJSONObject(j);

                item = new SubscriptionItem();
                item.type = SUBSCRIPTION_ITEM;
                item.id = subscription.optString("id");
                item.title = subscription.optString("title");
                item.url = "/subscription/" + item.id;
                items.add(item);
            }
        }
        
        // Root subscriptions
        JSONArray subscriptions = rootCategory.optJSONArray("subscriptions");
        for (int j = 0; j < subscriptions.length(); j++) {
            JSONObject subscription = subscriptions.optJSONObject(j);

            item = new SubscriptionItem();
            item.type = SUBSCRIPTION_ITEM;
            item.id = subscription.optString("id");
            item.title = subscription.optString("title");
            item.url = "/subscription/" + item.id;
            items.add(item);
        }
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.drawer_list_item, null);
        }
        
        SubscriptionItem item = getItem(position);
        ((TextView) v).setText(item.title);
        
        return v;
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
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public boolean isEnabled(int position) {
        int type = getItem(position).type;
        return type == SUBSCRIPTION_ITEM || type == CATEGORY_ITEM;
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
        private boolean unread = false;
        
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
    }
}
