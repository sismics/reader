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
     * Auth token used to download favicons.
     */
    private String authToken;
    
    /**
     * AQuery.
     */
    private AQuery aq;
    
    /**
     * Constructor.
     * @param context Context
     * @param input Subscriptions data from server
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
            if (item.getType() == SubscriptionItem.SUBSCRIPTION_ITEM) layout = R.layout.drawer_list_item_subscription;
            if (item.getType() == SubscriptionItem.CATEGORY_ITEM) layout = R.layout.drawer_list_item_category;
            view = vi.inflate(layout, null);
        }
        
        // Recycling AQuery
        aq.recycle(view);
        
        // Type specific layout data
        switch (item.getType()) {
        case SubscriptionItem.HEADER_ITEM:
            break;
        case SubscriptionItem.SUBSCRIPTION_ITEM:
            if (item.getId() != null) {
                String faviconUrl = PreferenceUtil.getServerUrl(context) + "/api/subscription/" + item.getId() + "/favicon";
                Bitmap placeHolder = aq.getCachedImage(R.drawable.ic_launcher);
                aq.id(R.id.imgFavicon)
                    .image(new BitmapAjaxCallback()
                        .url(faviconUrl)
                        .fallback(R.drawable.ic_launcher)
                        .preset(placeHolder)
                        .animation(AQuery.FADE_IN_NETWORK)
                        .cookie("auth_token", authToken))
                    .margin(item.isRoot() ? 16 : 32, 0, 0, 0);
            } else {
                if (item.getUrl().equals("/all")) {
                    aq.id(R.id.imgFavicon).image(item.isUnread() ? R.drawable.drawer_list_item_unread : R.drawable.drawer_list_item_read);
                } else if (item.getUrl().equals("/starred")) {
                    aq.id(R.id.imgFavicon).image(R.drawable.drawer_list_item_important);
                } else {
                    aq.id(R.id.imgFavicon).image(0);
                }
            }
            break;
        case SubscriptionItem.CATEGORY_ITEM:
            break;
        }
        
        // Common layout data
        aq.id(R.id.content).text(item.getTitle());
        if (item.getUnreadCount() == 0) {
            aq.id(R.id.unreadCount).gone();
        } else {
            aq.id(R.id.unreadCount).visible().text("" + item.getUnreadCount());
        }
        
        return view;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
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
        return item != null
                && (item.getType() == SubscriptionItem.SUBSCRIPTION_ITEM
                || item.getType() == SubscriptionItem.CATEGORY_ITEM);
    }

    /**
     * Clear data.
     */
    public void setItems(JSONObject input) {
        items = new ArrayList<SubscriptionItem>();
        SubscriptionItem item;
        
        // Adding fixed items
        item = new SubscriptionItem();
        item.setType(SubscriptionItem.HEADER_ITEM);
        item.setTitle(context.getString(R.string.latest));
        items.add(item);
        
        item = new SubscriptionItem();
        item.setType(SubscriptionItem.SUBSCRIPTION_ITEM);
        item.setTitle(context.getString(R.string.unread));
        item.setUrl("/all");
        item.setUnread(true);
        item.setUnreadCount(input.optInt("unread_count"));
        items.add(item);
        
        item = new SubscriptionItem();
        item.setType(SubscriptionItem.SUBSCRIPTION_ITEM);
        item.setTitle(context.getString(R.string.all));
        item.setUrl("/all");
        item.setUnread(false);
        items.add(item);
        
        item = new SubscriptionItem();
        item.setType(SubscriptionItem.SUBSCRIPTION_ITEM);
        item.setTitle(context.getString(R.string.starred));
        item.setUrl("/starred");
        items.add(item);
        
        item = new SubscriptionItem();
        item.setType(SubscriptionItem.HEADER_ITEM);
        item.setTitle(context.getString(R.string.subscriptions));
        items.add(item);
        
        // Adding categories and subscriptions
        JSONObject rootCategory = input.optJSONArray("categories").optJSONObject(0);
        JSONArray categories = rootCategory.optJSONArray("categories");
        if (categories != null) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.optJSONObject(i);
    
                item = new SubscriptionItem();
                item.setType(SubscriptionItem.CATEGORY_ITEM);
                item.setId(category.optString("id"));
                item.setTitle(category.optString("name"));
                item.setUrl("/category/" + item.getId());
                item.setUnreadCount(category.optInt("unread_count"));
                items.add(item);
                
                JSONArray subscriptions = category.optJSONArray("subscriptions");
                if (subscriptions != null) {
                    for (int j = 0; j < subscriptions.length(); j++) {
                        JSONObject subscription = subscriptions.optJSONObject(j);
        
                        item = new SubscriptionItem();
                        item.setType(SubscriptionItem.SUBSCRIPTION_ITEM);
                        item.setId(subscription.optString("id"));
                        item.setTitle(subscription.optString("title"));
                        item.setUrl("/subscription/" + item.getId());
                        item.setUnreadCount(subscription.optInt("unread_count"));
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
                item.setType(SubscriptionItem.SUBSCRIPTION_ITEM);
                item.setId(subscription.optString("id"));
                item.setTitle(subscription.optString("title"));
                item.setUrl("/subscription/" + item.getId());
                item.setUnreadCount(subscription.optInt("unread_count"));
                item.setRoot(true);
                items.add(item);
            }
        }
    }
}
