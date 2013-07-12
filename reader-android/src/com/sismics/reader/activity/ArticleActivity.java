package com.sismics.reader.activity;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.sismics.android.Log;
import com.sismics.android.SismicsHttpResponseHandler;
import com.sismics.reader.R;
import com.sismics.reader.listener.ArticlesHelperListener;
import com.sismics.reader.resource.ArticleResource;
import com.sismics.reader.ui.adapter.ArticlesPagerAdapter;
import com.sismics.reader.ui.adapter.SharedArticlesAdapterHelper;
import com.viewpagerindicator.UnderlinePageIndicator;

/**
 * Activity displaying articles.
 * 
 * @author bgamard
 */
public class ArticleActivity extends FragmentActivity {
    /**
     * Articles ViewPager.
     */
    private ViewPager viewPager;
    
    /**
     * Articles to mark as read later.
     */
    private ArrayList<String> readArticleIdList;
    
    /**
     * Shared articles adapter helper.
     */
    private SharedArticlesAdapterHelper sharedAdapterHelper;
    
    /**
     * Articles loading listener.
     */
    private ArticlesHelperListener articlesHelperListener = new ArticlesHelperListener() {
        @Override
        public void onStart() {
            setProgressBarIndeterminateVisibility(true);
        }
        
        @Override
        public void onEnd() {
            setProgressBarIndeterminateVisibility(false);
        }
    };
    
    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);
        
        readArticleIdList = new ArrayList<String>();
        sharedAdapterHelper = SharedArticlesAdapterHelper.getInstance();
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.article_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        // Building page change listener
        OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position + 1 >= sharedAdapterHelper.getArticleItems().size()) {
                    sharedAdapterHelper.load(ArticleActivity.this);
                }
                
                // Store article id to mark as read later
                final JSONObject article = sharedAdapterHelper.getArticleItems().get(position);
                String articleId = article.optString("id");
                if (!readArticleIdList.contains(articleId) && !article.optBoolean("force_unread")) {
                    readArticleIdList.add(article.optString("id"));
                }
                
                // Update activity title
                setTitle((position + 1) + "/" + sharedAdapterHelper.getTotal());
            }
            
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
        
        // Configuring ViewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        final ArticlesPagerAdapter adapter = new ArticlesPagerAdapter(getSupportFragmentManager());
        sharedAdapterHelper.addAdapter(adapter, articlesHelperListener);
        viewPager.setAdapter(adapter);
        
        // Configuring ViewPagerIndicator
        int position = getIntent().getIntExtra("position", 0);
        if (position >= sharedAdapterHelper.getArticleItems().size()) {
            position = 0;
        }
        UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewPager, position, sharedAdapterHelper.getTotal());
        indicator.setOnPageChangeListener(onPageChangeListener);
        
        // Forcing page change listener
        viewPager.setCurrentItem(position);
        onPageChangeListener.onPageSelected(position);
    }
    
    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("position", viewPager.getCurrentItem());
        setResult(RESULT_OK, data);
        
        sharedAdapterHelper.removeAdapter(viewPager.getAdapter(), articlesHelperListener);
        
        super.finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.article_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.unread:
            final JSONObject article = sharedAdapterHelper.getArticleItems().get(viewPager.getCurrentItem());
            
            // Flagging article as unread
            try {
                article.put("force_unread", true);
            } catch (JSONException e) {
                Log.e("ArticleActivity", "Error forcing article at unread state", e);
            }
            
            // Removing from mark as read list
            String articleId = article.optString("id");
            readArticleIdList.remove(articleId);
            
            // Marking article as unread
            ArticleResource.unread(ArticleActivity.this, articleId, new SismicsHttpResponseHandler() {
                public void onSuccess(JSONObject json) {
                    try {
                        article.put("is_read", false);
                        sharedAdapterHelper.onDataChanged();
                        Toast.makeText(ArticleActivity.this, R.string.marked_as_unread, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Log.e("ArticleActivity", "Error changing read state", e);
                    }
                }
            });
            return true;
            
        case android.R.id.home:
            finish();
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onPause() {
        if (readArticleIdList != null && !readArticleIdList.isEmpty()) {
            ArticleResource.readMultiple(ArticleActivity.this, readArticleIdList, new SismicsHttpResponseHandler() {
                @Override
                public void onSuccess(JSONObject json) {
                    // Mark articles as read on local data
                    for (JSONObject article : sharedAdapterHelper.getArticleItems()) {
                        String articleId = article.optString("id");
                        if (readArticleIdList.contains(articleId)) {
                            try {
                                article.put("is_read", true);
                            } catch (JSONException e) {
                                Log.e("ArticleActivity", "Error changing read state", e);
                            }
                        }
                    }
                    
                    sharedAdapterHelper.onDataChanged();
                }
            });
        }
        super.onPause();
    }
}
