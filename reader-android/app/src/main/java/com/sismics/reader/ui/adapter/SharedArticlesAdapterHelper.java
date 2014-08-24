package com.sismics.reader.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.widget.BaseAdapter;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.sismics.android.Log;
import com.sismics.reader.listener.ArticlesHelperListener;
import com.sismics.reader.resource.SubscriptionResource;
import com.sismics.reader.util.PreferenceUtil;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static helper to use the same articles between multiples adapters.
 * 
 * @author bgamard
 */
public class SharedArticlesAdapterHelper {
    /**
     * Current instance.
     */
    private static SharedArticlesAdapterHelper instance;

    /**
     * Feed loading request handle.
     */
    private RequestHandle feedRequestHandle;
    
    /**
     * Shared data.
     */
    private List<JSONObject> articleItems = new ArrayList<>();
    
    /**
     * Adapters sharing the same data.
     */
    private Set<Object> adapters = new HashSet<>();
    
    /**
     * Listeners on articles loading.
     */
    private Set<ArticlesHelperListener> listeners = new HashSet<>();
    
    /**
     * API URL.
     */
    private String url;
    
    /**
     * Unread state.
     */
    private boolean unread;
    
    /**
     * Is loading.
     */
    private boolean loading;
    
    /**
     * All articles are loaded.
     */
    private boolean fullyLoaded;
    
    /**
     * Returns an instance.
     * @return Adapter helper instance
     */
    public static SharedArticlesAdapterHelper getInstance() {
        if (instance == null) {
            instance = new SharedArticlesAdapterHelper();
        }
        return instance;
    }
    
    /**
     * Getter of articleItems.
     * Never copy this list or be out of sync.
     * @return articleItems
     */
    public List<JSONObject> getArticleItems() {
        return articleItems;
    }
    
    /**
     * Add adapter.
     * @param adapter Adapter to add
     * @param listener Listener to add
     */
    public void addAdapter(Object adapter, ArticlesHelperListener listener) {
        if (adapter != null) {
            adapters.add(adapter);
        }
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove adapter.
     * @param adapter Adapter to remove
     * @param listener Listener to remove
     */
    public void removeAdapter(Object adapter, ArticlesHelperListener listener) {
        if (adapter != null) {
            adapters.remove(adapter);
        }
        if (listener != null) {
            listeners.remove(listener);
        }
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
        articleItems = new ArrayList<>();
        this.url = url;
        this.unread = unread;
        this.loading = false;
        this.fullyLoaded = false;

        if (feedRequestHandle != null && !feedRequestHandle.isCancelled() && !feedRequestHandle.isFinished()) {
            // Cancel the previous request if it's not yet finished
            feedRequestHandle.cancel(true);
        }

        // The data has been changed, there is no more articles
        onDataChanged();
    }
    
    /**
     * Getter of URL.
     * @return URL
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Load more articles.
     * @param context Context
     */
    public void load(Context context) {
        final List<JSONObject> items = articleItems;
        
        if (loading || fullyLoaded || url == null) {
            return;
        }
        
        loading = true;

        for (ArticlesHelperListener listener : listeners) {
            listener.onStart();
        }
        
        String afterArticleId = null;
        if (items.size() > 0) {
            afterArticleId = items.get(items.size() - 1).optString("id");
        }

        // Number of articles to fetch
        int articlesFetchedPref = PreferenceUtil.getIntegerPreference(context, PreferenceUtil.PREF_ARTICLES_FETCHED, 10);

        // Load data from server
        feedRequestHandle = SubscriptionResource.feed(context, url, unread, url.startsWith("/search/") ? items.size() : -1,
                articlesFetchedPref, afterArticleId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                // Tell the listeners we have finished
                loading = false;
                for (ArticlesHelperListener listener : listeners) {
                    listener.onSuccess();
                    listener.onEnd();
                }

                // If reference has not changed, let's update the shared data
                if (items != articleItems) {
                    return;
                }

                // Add new articles to the common articles list
                JSONArray articles = json.optJSONArray("articles");
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject article = articles.optJSONObject(i);
                    
                    // Precompute some data
                    try {
                        String description = article.optString("description");
                        String cleanedDescription = description.replaceAll("<.*?>", "").trim();
                        int length = cleanedDescription.length();
                        String summary = cleanedDescription.substring(0, length < 500 ? length : 500);
                        article.put("summary", summary);
                    } catch (JSONException e) {
                        Log.e("ArticlesAdapter", "Cannot precompute article", e);
                    }
                    
                    items.add(article);
                }

                // If there is no articles here, there won't be more
                if (articles.length() == 0) {
                    fullyLoaded = true;
                }
                
                onDataChanged();
            }

            @Override
            public void onFailure(final int statusCode, final Header[] headers, final byte[] responseBytes, final Throwable throwable) {
                // Tell the listeners something bad happened
                loading = false;
                for (ArticlesHelperListener listener : listeners) {
                    listener.onError();
                    listener.onEnd();
                }
            }
        });
    }

    /**
     * Returns true if all articles are loaded.
     *
     * @return True if all articles are loaded
     */
    public boolean isFullyLoaded() {
        return fullyLoaded;
    }
}
