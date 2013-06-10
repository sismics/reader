package com.sismics.reader.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.sismics.android.Log;
import com.sismics.android.SismicsHttpResponseHandler;
import com.sismics.reader.R;
import com.sismics.reader.resource.ArticleResource;
import com.sismics.reader.ui.adapter.ArticlesPagerAdapter;
import com.sismics.reader.ui.adapter.SharedAdapterHelper;

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
    
    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);
        
        setContentView(R.layout.article_activity);
        
        viewPager = (ViewPager) findViewById(R.id.viewPager);
            
        final ArticlesPagerAdapter adapter = new ArticlesPagerAdapter(getSupportFragmentManager());
        SharedAdapterHelper.getInstance().addAdapter(adapter);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                SharedAdapterHelper sharedAdapterHelper = SharedAdapterHelper.getInstance();
                if (position + 1 >= sharedAdapterHelper.getArticleItems().size()) {
                    sharedAdapterHelper.load(ArticleActivity.this);
                }
                
                // Mark article as read
                final JSONObject article = sharedAdapterHelper.getArticleItems().get(position);
                if (!article.optBoolean("is_read")) {
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
        });
        
        viewPager.setCurrentItem(getIntent().getIntExtra("position", 0));
    }
    
    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("position", viewPager.getCurrentItem());
        setResult(RESULT_OK, data);
        
        SharedAdapterHelper.getInstance().removeAdapter(viewPager.getAdapter());
        
        super.finish();
    }
}
