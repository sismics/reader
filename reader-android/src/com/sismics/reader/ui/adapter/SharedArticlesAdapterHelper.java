package com.sismics.reader.ui.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.widget.BaseAdapter;

import com.sismics.android.Log;
import com.sismics.android.SismicsHttpResponseHandler;
import com.sismics.reader.resource.SubscriptionResource;

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
     * Shared data.
     */
    private List<JSONObject> articleItems = new ArrayList<JSONObject>();
    
    /**
     * Adapters sharing the same data.
     */
    private Set<Object> adapters = new HashSet<Object>();
    
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
     * Total number of articles.
     */
    private int total;
    
    /**
     * Regexp to extract images URL.
     */
    private static final Pattern IMG_PATTERN = Pattern.compile("<img(.*?)src=\"(.*?)\"(.*?)>");
    
    /**
     * Returns an instance.
     * @return
     */
    public static SharedArticlesAdapterHelper getInstance() {
        if (instance == null) {
            instance = new SharedArticlesAdapterHelper();
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
        int offset = items.size();

        // Remove read articles in unread context
        if (unread) {
            for (JSONObject article : articleItems) {
                if (article.optBoolean("is_read")) {
                    offset--;
                }
            }
        }
        
        // Remove unstarred articles in starred context
        if (url.equals("/starred")) {
            for (JSONObject article : articleItems) {
                if (article.optBoolean("is_starred")) {
                    offset--;
                }
            }
        }
        
        SubscriptionResource.feed(context, url, unread, 10, offset, new SismicsHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                // If reference has not changed, let's update the shared data
                if (items != articleItems) {
                    return;
                }
                
                if (total == 0) {
                    total = json.optInt("total");
                }
                
                JSONArray articles = json.optJSONArray("articles");
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject article = articles.optJSONObject(i);
                    
                    // Precompute some data
                    try {
                        // TODO Use Apache's commons lang 2.6 StringEscapeUtils.unescapeHtml
                        String description = article.optString("description");
                        Matcher matcher = IMG_PATTERN.matcher(description);
                        if (matcher.find()) {
                            article.put("image_url", Html.fromHtml(matcher.group(2)).toString());
                        }
                        String cleanedDescription = description.replaceAll("\\<.*?>", "");
                        int length = cleanedDescription.length();
                        String summary = cleanedDescription.substring(0, length < 300 ? length : 300);
                        article.put("summary", summary);
                    } catch (JSONException e) {
                        Log.e("ArticlesAdapter", "Cannot precompute article", e);
                    }
                    
                    items.add(article);
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
