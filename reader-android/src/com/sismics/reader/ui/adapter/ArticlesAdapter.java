package com.sismics.reader.ui.adapter;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sismics.android.SismicsHttpResponseHandler;
import com.sismics.reader.resource.SubscriptionResource;

/**
 * Adapter for articles list.
 * 
 * @author bgamard
 */
public class ArticlesAdapter extends BaseAdapter {
    /**
     * Articles from server.
     */
    private List<JSONObject> items;
    
    /**
     * Total number of articles.
     */
    private int total = 0;

    /**
     * API URL to load.
     */
    private String url;
    
    /**
     * Load read or unread articles.
     */
    private boolean unread;
    
    /**
     * True if a loading is in progress.
     */
    private boolean loading = false;
    
    /**
     * Context.
     */
    private Activity activity;
    
    /**
     * AQuery.
     */
    private AQuery aq;
    
    /**
     * Constructeur.
     * @param context
     * @param items
     */
    public ArticlesAdapter(Activity activity, String url, boolean unread) {
        this.activity = activity;
        this.aq = new AQuery(activity);
        this.url = url;
        this.unread = unread;
        items = SharedAdapterHelper.getInstance().getArticleItems();
        loadArticles();
    }

    /**
     * Load more articles.
     */
    public void loadArticles() {
        // TODO Share this code among articles adapter
        if (loading || items.size() >= total && total != 0) {
            return;
        }
        
        loading = true;
        SubscriptionResource.feed(activity, url, unread, 10, items.size(), new SismicsHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                if (total == 0) {
                    total = json.optInt("total");
                }
                JSONArray articles = json.optJSONArray("articles");
                for (int i = 0; i < articles.length(); i++) {
                    items.add(articles.optJSONObject(i));
                }
                
                SharedAdapterHelper.getInstance().onDataChanged();
            }
            
            @Override
            public void onFinish() {
                loading = false;
            }
        });
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            // LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // view = vi.inflate(R.layout.article_item, null);
            view = new TextView(activity);
        }
        
        aq.recycle(view);
        
        // Filling articles data
        JSONObject article = getItem(position);
        aq.text(article.optString("title"));
        
        return view;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    /**
     * Getter of total.
     * @return total
     */
    public int getTotal() {
        return total;
    }
}
