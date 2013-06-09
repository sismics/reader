package com.sismics.reader.ui.adapter;

import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidquery.AQuery;

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
    public ArticlesAdapter(Activity activity) {
        this.activity = activity;
        this.aq = new AQuery(activity);
        items = SharedAdapterHelper.getInstance().getArticleItems();
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
}
