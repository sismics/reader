package com.sismics.reader.activity;

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
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.article_activity);
        getActionBar().setHomeButtonEnabled(true);
        
        // Building page change listener
        OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                SharedArticlesAdapterHelper sharedAdapterHelper = SharedArticlesAdapterHelper.getInstance();
                if (position + 1 >= sharedAdapterHelper.getArticleItems().size()) {
                    sharedAdapterHelper.load(ArticleActivity.this);
                }
                
                // Mark article as read
                // TODO New API needed to mark several articles as read and call onPause
                final JSONObject article = sharedAdapterHelper.getArticleItems().get(position);
                if (!article.optBoolean("is_read") && !article.optBoolean("force_unread")) {
                    ArticleResource.read(ArticleActivity.this, article.optString("id"), new SismicsHttpResponseHandler() {
                        public void onSuccess(JSONObject json) {
                            try {
                                article.put("is_read", true);
                            } catch (JSONException e) {
                                Log.e("ArticleActivity", "Error changing read state", e);
                            }
                        }
                    });
                }
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
        SharedArticlesAdapterHelper.getInstance().addAdapter(adapter, articlesHelperListener);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(onPageChangeListener);
        
        // Setting current page
        int position = getIntent().getIntExtra("position", 0);
        viewPager.setCurrentItem(position);
        if (position == 0) {
            onPageChangeListener.onPageSelected(0);
        }
    }
    
    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("position", viewPager.getCurrentItem());
        setResult(RESULT_OK, data);
        
        SharedArticlesAdapterHelper.getInstance().removeAdapter(viewPager.getAdapter(), articlesHelperListener);
        
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
            SharedArticlesAdapterHelper sharedAdapterHelper = SharedArticlesAdapterHelper.getInstance();
            final JSONObject article = sharedAdapterHelper.getArticleItems().get(viewPager.getCurrentItem());
            
            // Flagging article as unread
            try {
                article.put("force_unread", true);
            } catch (JSONException e) {
                Log.e("ArticleActivity", "Error forcing article at unread state", e);
            }
            
            // Marking article as unread
            ArticleResource.unread(ArticleActivity.this, article.optString("id"), new SismicsHttpResponseHandler() {
                public void onSuccess(JSONObject json) {
                    try {
                        article.put("is_read", false);
                        Toast.makeText(ArticleActivity.this, R.string.marked_as_unread, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Log.e("ArticleActivity", "Error changing read state", e);
                    }
                }
            });
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
