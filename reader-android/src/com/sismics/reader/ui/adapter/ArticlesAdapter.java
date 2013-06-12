package com.sismics.reader.ui.adapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sismics.reader.R;

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
        items = SharedArticlesAdapterHelper.getInstance().getArticleItems();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.article_item, null);
        }
        
        aq.recycle(view);
        
        // Filling articles data
        JSONObject article = getItem(position);
        TextView txtTitle = aq.id(R.id.txtTitle)
                .text(Html.fromHtml(article.optString("title")))
                .getTextView();
        if (article.optBoolean("is_read")) {
            txtTitle.setTypeface(null, Typeface.NORMAL);
        } else {
            txtTitle.setTypeface(null, Typeface.BOLD);
        }
        JSONObject subscription = article.optJSONObject("subscription");
        String imageUrl = article.optString("image_url");
        if (imageUrl != null) {
            Bitmap placeHolder = aq.getCachedImage(R.drawable.ic_launcher);
//            if (aq.shouldDelay(position, view, parent, imageUrl)) {
//                aq.id(R.id.imgThumbnail).image(placeHolder);
//            } else {
            aq.id(R.id.imgThumbnail).image(imageUrl, true, true, 200, AQuery.INVISIBLE, placeHolder, AQuery.FADE_IN_NETWORK);
//            }
        } else {
            aq.id(R.id.imgThumbnail).invisible();
        }
        String summary = "<b>" + subscription.optString("title") + "</b> &mdash; " + article.optString("summary");
        aq.id(R.id.txtSummary).text(Html.fromHtml(summary));
        
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
