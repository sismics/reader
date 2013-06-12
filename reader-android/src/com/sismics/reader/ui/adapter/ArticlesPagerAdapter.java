package com.sismics.reader.ui.adapter;

import java.util.List;

import org.json.JSONObject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sismics.reader.fragment.ArticleFragment;


/**
 * Adapter for articles ViewPager.
 * 
 * @author bgamard
 */
public class ArticlesPagerAdapter extends FragmentStatePagerAdapter {
    /**
     * Articles from server.
     */
    private List<JSONObject> items;
    
    /**
     * Constructor.
     * @param FragmentManager
     * @param ArticlesAdapter to wrap
     */
    public ArticlesPagerAdapter(FragmentManager fm) {
        super(fm);
        this.items = SharedArticlesAdapterHelper.getInstance().getArticleItems();
    }

    @Override
    public Fragment getItem(int position) {
        JSONObject article = items.get(position);
        return ArticleFragment.newInstance(article);
    }

    @Override
    public int getCount() {
        return items.size();
    }
}
