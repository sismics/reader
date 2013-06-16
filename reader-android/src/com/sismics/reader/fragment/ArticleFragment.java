package com.sismics.reader.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.sismics.reader.R;

/**
 * Fragment displaying an article.
 * 
 * @author bgamard
 */
public class ArticleFragment extends Fragment {
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
        WebView webView = (WebView) view.findViewById(R.id.articleWebView);
        webView.getSettings().setUseWideViewPort(true);
        
        Bundle args = getArguments();
        if (args != null) {
            String jsonStr = args.getString("json");
            if (jsonStr != null) {
                try {
                    JSONObject json = new JSONObject(jsonStr);
                    
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
                } catch (JSONException e) {
                    Log.e("ArticleFragment", "Unable to parse JSON", e);
                }
            }
        }
        return view;
    }
}
