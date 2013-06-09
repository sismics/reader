package com.sismics.reader.ui.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.support.v4.view.PagerAdapter;
import android.widget.BaseAdapter;

import com.sismics.android.Log;

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
     * Clear shared data.
     */
    public void clearData() {
        if (adapters.size() > 0) {
            Log.e("ApplicationContext", "Adapters list not empty!!");
        }
        adapters.clear();
        articleItems = new ArrayList<JSONObject>();
    }
}
