package com.sismics.reader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.sismics.reader.R;
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
                if (position + 1 >= SharedAdapterHelper.getInstance().getArticleItems().size()) {
                    SharedAdapterHelper.getInstance().load(ArticleActivity.this);
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
