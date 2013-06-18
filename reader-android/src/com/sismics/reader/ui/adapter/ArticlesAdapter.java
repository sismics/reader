package com.sismics.reader.ui.adapter;

import java.util.List;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.reader.R;
import com.sismics.reader.constant.Constants;
import com.sismics.reader.util.PreferenceUtil;

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
     * Auth token used to download favicons.
     */
    private String authToken;
    
    /**
     * Constructeur.
     * @param context
     * @param items
     */
    public ArticlesAdapter(Activity activity) {
        this.activity = activity;
        this.aq = new AQuery(activity);
        this.items = SharedArticlesAdapterHelper.getInstance().getArticleItems();
        this.authToken = PreferenceUtil.getAuthToken(activity);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;
        
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.article_item, null);
            aq.recycle(view);
            holder = new ViewHolder();
            holder.txtTitle = aq.id(R.id.txtTitle).getTextView();
            holder.txtSummary = aq.id(R.id.txtSummary).getTextView();
            holder.imgFavicon = aq.id(R.id.imgFavicon).getImageView();
            view.setTag(holder);
        } else {
            aq.recycle(view);
            holder = (ViewHolder) view.getTag();
        }
        
        // Filling articles data
        JSONObject article = getItem(position);
        holder.txtTitle.setText(Html.fromHtml(article.optString("title")));
        if (article.optBoolean("is_read")) {
            holder.txtTitle.setTypeface(null, Typeface.NORMAL);
        } else {
            holder.txtTitle.setTypeface(null, Typeface.BOLD);
        }
        JSONObject subscription = article.optJSONObject("subscription");
        Bitmap placeHolder = aq.getCachedImage(R.drawable.ic_launcher);
        String faviconUrl = Constants.READER_API_URL + "/subscription/" + subscription.optString("id") + "/favicon";
        if (aq.shouldDelay(position, view, parent, faviconUrl)) {
            aq.id(holder.imgFavicon).image(placeHolder);
        } else {
            aq.id(holder.imgFavicon).image(new BitmapAjaxCallback()
                .url(faviconUrl)
                .fallback(R.drawable.ic_launcher)
                .preset(placeHolder)
                .animation(AQuery.FADE_IN_NETWORK)
                .cookie("auth_token", authToken));
        }
        String summary = "<b>" + subscription.optString("title") + "</b> &mdash; " + article.optString("summary");
        holder.txtSummary.setText(Html.fromHtml(summary));
        
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
     * Article ViewHolder.
     * 
     * @author bgamard
     */
    private static class ViewHolder {
        TextView txtTitle;
        TextView txtSummary;
        ImageView imgFavicon;
    }
}
