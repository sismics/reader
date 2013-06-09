package com.sismics.reader.ui.adapter;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sismics.android.SismicsHttpResponseHandler;
import com.sismics.reader.fragment.ArticleFragment;
import com.sismics.reader.model.application.ApplicationContext;
import com.sismics.reader.resource.SubscriptionResource;


/**
 * Adapter for articles ViewPager.
 * 
 * @author bgamard
 */
public class ArticlesPagerAdapter extends FragmentStatePagerAdapter {
    /**
     * Articles from server.
     */
    private List<JSONObject> items;
    
    /**
     * Total number of articles.
     */
    private int total;

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
     * Constructor.
     * @param FragmentManager
     * @param ArticlesAdapter to wrap
     */
    public ArticlesPagerAdapter(Activity activity, FragmentManager fm, int total, String url, boolean unread) {
        super(fm);
        this.activity = activity;
        this.url = url;
        this.unread = unread;
        this.total = total;
        this.items = ApplicationContext.getInstance().getArticleItems();
    }

    /**
     * Load more articles.
     */
    public void loadArticles() {
        // TODO Share this code among articles adapter
        if (loading || items.size() >= total) {
            return;
        }
        
        loading = true;
        SubscriptionResource.feed(activity, url, unread, 10, items.size(), new SismicsHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                if (activity.isFinishing()) {
                    return;
                }
                
                JSONArray articles = json.optJSONArray("articles");
                for (int i = 0; i < articles.length(); i++) {
                    items.add(articles.optJSONObject(i));
                }
                
                ApplicationContext.getInstance().onArticleItemsChanged();
            }
            
            @Override
            public void onFinish() {
                loading = false;
            }
        });
    }
    
    @Override
    public Fragment getItem(int position) {
        JSONObject article = items.get(position);
        return ArticleFragment.newInstance(article);
    }

    @Override
    public int getCount() {
        return items.size();
    }
}
