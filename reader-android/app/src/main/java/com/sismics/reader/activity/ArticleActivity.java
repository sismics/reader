package com.sismics.reader.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.android.Log;
import com.sismics.reader.R;
import com.sismics.reader.listener.ArticlesHelperListener;
import com.sismics.reader.resource.ArticleResource;
import com.sismics.reader.resource.StarredResource;
import com.sismics.reader.ui.adapter.ArticlesPagerAdapter;
import com.sismics.reader.ui.adapter.SharedArticlesAdapterHelper;
import com.viewpagerindicator.UnderlinePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

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
    private Set<String> readArticleIdSet;
    
    /**
     * Shared articles adapter helper.
     */
    private SharedArticlesAdapterHelper sharedAdapterHelper;
    
    /**
     * Share action provider.
     */
    private ShareActionProvider shareActionProvider;
    
    /**
     * Favorite menu item.
     */
    private MenuItem favoriteMenuItem;
    
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
        
        readArticleIdSet = new HashSet<String>();
        sharedAdapterHelper = SharedArticlesAdapterHelper.getInstance();
        if (sharedAdapterHelper.getArticleItems().size() == 0) {
            finish();
            return;
        }
        
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
                if (!readArticleIdSet.contains(articleId) && !article.optBoolean("force_unread")) {
                    readArticleIdSet.add(article.optString("id"));
                }
                
                // Update the action bar
                updateActionBar();
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
        UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewPager, position);
        indicator.setOnPageChangeListener(onPageChangeListener);
        
        // Forcing page change listener
        viewPager.setCurrentItem(position);
        onPageChangeListener.onPageSelected(position);
    }
    
    @Override
    public void finish() {
        Intent data = new Intent();
        if (viewPager != null) {
            data.putExtra("position", viewPager.getCurrentItem());
            sharedAdapterHelper.removeAdapter(viewPager.getAdapter(), articlesHelperListener);
        }
        setResult(RESULT_OK, data);
        
        super.finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.article_activity, menu);
        
        // Fetch and store ShareActionProvider to feed later
        MenuItem item = menu.findItem(R.id.share);
        shareActionProvider = (ShareActionProvider) item.getActionProvider();
        
        // Store favorite button to change his icon
        favoriteMenuItem = menu.findItem(R.id.favorite);
        
        updateActionBar();
        return super.onCreateOptionsMenu(menu);
    }
    
    /**
     * Update the action bar with current article context.
     */
    private void updateActionBar() {
        final JSONObject article = sharedAdapterHelper.getArticleItems().get(viewPager.getCurrentItem());
        
        // Update the share action provider with a new intent
        if (shareActionProvider != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, article.optString("title"));
            intent.putExtra(Intent.EXTRA_TEXT, article.optString("url"));
            intent.setType("text/plain");
            shareActionProvider.setShareIntent(intent);
        }
        
        // Update the favorite button
        if (favoriteMenuItem != null) {
            boolean isStarred = article.optBoolean("is_starred");
            favoriteMenuItem.setIcon(isStarred ? R.drawable.ic_action_important : R.drawable.ic_action_not_important);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final JSONObject article = sharedAdapterHelper.getArticleItems().get(viewPager.getCurrentItem());
        final String articleId = article.optString("id");
        
        // Button as progress bar during network work
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View progressView = inflater.inflate(R.layout.actionbar_indeterminate_progress, null);
        
        switch (item.getItemId()) {
        case R.id.unread:
            // Flagging article as unread
            try {
                article.put("force_unread", true);
            } catch (JSONException e) {
                Log.e("ArticleActivity", "Error forcing article at unread state", e);
            }
            
            // Removing from mark as read list
            readArticleIdSet.remove(articleId);
            
            item.setActionView(progressView);
            
            // Marking article as unread
            ArticleResource.unread(ArticleActivity.this, articleId, new JsonHttpResponseHandler() {
                public void onSuccess(JSONObject json) {
                    try {
                        article.put("is_read", false);
                        sharedAdapterHelper.onDataChanged();
                        Toast.makeText(ArticleActivity.this, R.string.marked_as_unread, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Log.e("ArticleActivity", "Error changing read state", e);
                    }
                }
                
                @Override
                public void onFinish() {
                    item.setActionView(null);
                }
            });
            return true;
            
        case R.id.favorite:
            item.setActionView(progressView);
            
            final boolean isStarred = article.optBoolean("is_starred");
            
            // Star or unstar the article
            StarredResource.star(ArticleActivity.this, articleId, !isStarred, new JsonHttpResponseHandler() {
                public void onSuccess(JSONObject json) {
                    try {
                        article.put("is_starred", !isStarred);
                        updateActionBar();
                        sharedAdapterHelper.onDataChanged();
                    } catch (JSONException e) {
                        Log.e("ArticleActivity", "Error starring/unstarring article", e);
                    }
                }
                
                @Override
                public void onFinish() {
                    item.setActionView(null);
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
        if (readArticleIdSet != null && !readArticleIdSet.isEmpty()) {
            ArticleResource.readMultiple(ArticleActivity.this, readArticleIdSet, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(JSONObject json) {
                    // Mark articles as read on local data
                    for (JSONObject article : sharedAdapterHelper.getArticleItems()) {
                        String articleId = article.optString("id");
                        if (readArticleIdSet.contains(articleId)) {
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
