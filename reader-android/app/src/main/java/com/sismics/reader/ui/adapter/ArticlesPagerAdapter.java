package com.sismics.reader.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sismics.reader.fragment.ArticleFragment;

import org.json.JSONObject;

import java.util.List;


/**
 * Adapter for articles ViewPager.
 * 
 * @author bgamard
 */
public class ArticlesPagerAdapter extends FragmentStatePagerAdapter {
    /**
     * Constructor.
     * @param fm FragmentManager
     */
    public ArticlesPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        JSONObject article = SharedArticlesAdapterHelper.getInstance().getArticleItems().get(position);
        return ArticleFragment.newInstance(article);
    }

    @Override
    public int getCount() {
        return SharedArticlesAdapterHelper.getInstance().getArticleItems().size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_UNCHANGED;
        // Do not return POSITION_NONE all the time or the whole fragment will be invalidated
        // return POSITION_NONE;
    }
}
