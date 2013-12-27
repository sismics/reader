package com.sismics.reader.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;
import com.sismics.reader.R;

/**
 * Articles default fragment.
 * 
 * @author bgamard
 */
public class ArticlesDefaultFragment extends NavigationFragment {
    
    /**
     * AQuery.
     */
    private AQuery aq;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.articles_fragment, container, false);
        aq = new AQuery(view);
        aq.id(R.id.articleList).getListView().setEmptyView(aq.id(R.id.progressBar).getView());
        aq.id(R.id.loadingText).text(R.string.loading_subscriptions);
        
        return view;
    }
}