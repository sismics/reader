package com.sismics.reader.ui.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.widget.BaseAdapter;

import com.sismics.android.SismicsHttpResponseHandler;
import com.sismics.reader.resource.SubscriptionResource;

/**
 * Helper to use the same data between multiples adapters.
 * 
 * @author bgamard
 */
public class SharedAdapterHelper {
    /**
     * Current instance.
     */
    private static SharedAdapterHelper instance;
    
    /**
     * Shared data.
     */
    private List<JSONObject> articleItems = new ArrayList<JSONObject>();
    
    /**
     * Adapters sharing the same data.
     */
    private Set<Object> adapters = new HashSet<Object>();
    
    private String url;
    
    private boolean unread;
    
    private boolean loading;
    
    private int total;
    
    /**
     * Returns an instance.
     * @return
     */
    public static SharedAdapterHelper getInstance() {
        if (instance == null) {
            instance = new SharedAdapterHelper();
        }
        return instance;
    }
    
    /**
     * Getter of articleItems.
     * @return
     */
    public List<JSONObject> getArticleItems() {
        return articleItems;
    }
    
    /**
     * Add adapter.
     * @param adapter
     */
    public void addAdapter(Object adapter) {
        adapters.add(adapter);
    }
    
    /**
     * Remove adapter.
     * @param adapter
     */
    public void removeAdapter(Object adapter) {
        adapters.remove(adapter);
    }
    
    /**
     * Called on data changed.
     */
    public void onDataChanged() {
        for (Object adapter : adapters) {
            if (adapter instanceof BaseAdapter) {
                ((BaseAdapter) adapter).notifyDataSetChanged();
            }
            if (adapter instanceof PagerAdapter) {
                ((PagerAdapter) adapter).notifyDataSetChanged();
            }
        }
    }

    /**
     * Restart shared data on a new context.
     */
    public void restart(String url, boolean unread) {
        adapters.clear();
        articleItems = new ArrayList<JSONObject>();
        this.url = url;
        this.unread = unread;
        this.loading = false;
        this.total = 0;
    }
    
    /**
     * Load more articles.
     * @param context
     */
    public void load(Context context) {
        final List<JSONObject> items = articleItems;
        
        if (loading || items.size() >= total && total != 0) {
            return;
        }
        
        loading = true;
        SubscriptionResource.feed(context, url, unread, 10, items.size(), new SismicsHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                // if reference has not changed, let's update the shared data
                if (items != articleItems) {
                    return;
                }
                
                if (total == 0) {
                    total = json.optInt("total");
                }
                
                JSONArray articles = json.optJSONArray("articles");
                for (int i = 0; i < articles.length(); i++) {
                    items.add(articles.optJSONObject(i));
                }
                
                onDataChanged();
            }
            
            @Override
            public void onFinish() {
                loading = false;
            }
        });
    }
}
