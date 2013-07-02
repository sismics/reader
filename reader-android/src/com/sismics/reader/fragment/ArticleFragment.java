package com.sismics.reader.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.androidquery.AQuery;
import com.sismics.reader.R;

/**
 * Fragment displaying an article.
 * 
 * @author bgamard
 */
public class ArticleFragment extends Fragment {
    
    /**
     * AQuery.
     */
    private AQuery aq;
    
    /**
     * Create a new instance of ArticleFragment.
     */
    public static ArticleFragment newInstance(JSONObject json) {
        ArticleFragment f = new ArticleFragment();

        // Supply argument
        Bundle args = new Bundle();
        args.putString("json", json.toString());
        f.setArguments(args);

        return f;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.article_fragment, container, false);
        aq = new AQuery(view);
        
        WebView webView = aq.id(R.id.articleWebView).getWebView();
        webView.getSettings().setUseWideViewPort(true);
        
        Bundle args = getArguments();
        if (args != null) {
            String jsonStr = args.getString("json");
            if (jsonStr != null) {
                try {
                    final JSONObject json = new JSONObject(jsonStr);
                    
                    // HTML modification to fit the article content in the screen width
                    String html = json.optString("description");
                    try {
                        html = "<!DOCTYPE html>" +
                        		"<html>" +
                        		"<head>" +
                        		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
                        		"<meta name=\"viewport\" content=\"initial-scale=1, minimum-scale=1, width=device-width, maximum-scale=1, user-scalable=no\" />" +
                        		"<style>" +
                        		"img, iframe { max-width: 100%; height: auto; } " +
                        		"pre { max-width: 100%; overflow: hidden; } " +
                        		"</style>" +
                        		"</head>" +
                        		"<body>" +
                        		URLEncoder.encode(html, "UTF-8").replaceAll("\\+", "%20") +
                        		"</body>" +
                        		"</html>";
                    } catch (UnsupportedEncodingException e) {
                        Log.e("ArticleFragment", "Error modifying article HTML", e);
                    }
                    webView.loadData(html, "text/html; charset=UTF-8", null);
                    
                    // Other articles data
                    aq.id(R.id.title)
                        .text(Html.fromHtml(json.optString("title")))
                        .clicked(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(json.optString("url")));
                                startActivity(intent);
                            }
                        });
                    String subscriptionTitle = json.optJSONObject("subscription").optString("title");
                    String creator = json.optString("creator");
                    if (!creator.isEmpty()) {
                        aq.id(R.id.author).text(getString(R.string.article_subscription_author, subscriptionTitle, creator));
                    } else {
                        aq.id(R.id.author).text(getString(R.string.article_subscription, subscriptionTitle));
                    }
                    aq.id(R.id.date).text(DateUtils.getRelativeTimeSpanString(json.optLong("date"), new Date().getTime(), 0).toString());
                } catch (JSONException e) {
                    Log.e("ArticleFragment", "Unable to parse JSON", e);
                }
            }
        }
        return view;
    }
}
