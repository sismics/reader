package com.sismics.reader.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.sismics.reader.R;
import com.sismics.reader.activity.ArticleActivity;
import com.sismics.reader.constant.Constants;
import com.sismics.reader.model.application.ApplicationContext;
import com.sismics.reader.ui.adapter.ArticlesAdapter;

/**
 * Articles list fragment.
 * 
 * @author bgamard
 */
public class ArticlesFragment extends NavigationFragment {
    
    // Interface
    private ListView articleList;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.articles_fragment, container, false);
        articleList = (ListView) view.findViewById(R.id.articleList);
        
        Bundle args = getArguments();
        if (args != null) {
            String url = args.getString("url");
            boolean unread = args.getBoolean("unread");
            if (url != null) {
                initAdapter(url, unread);
            }
        }
        
        return view;
    }

    /**
     * Load articles.
     * @param url
     */
    private void initAdapter(final String url, final boolean unread) {
        final ArticlesAdapter adapter = new ArticlesAdapter(getActivity(), url, unread);
        ApplicationContext.getInstance().addOnArticleItemsChanged(adapter);
        articleList.setAdapter(adapter);
        
        articleList.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount - 2) {
                    adapter.loadArticles();
                }
            }
        });
        
        articleList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ArticleActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("unread", unread);
                intent.putExtra("total", adapter.getTotal());
                intent.putExtra("position", position);
                startActivityForResult(intent, Constants.REQUEST_CODE_ARTICLES);
            }
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_ARTICLES && resultCode == Activity.RESULT_OK) {
            articleList.smoothScrollToPosition(data.getIntExtra("position", 0));
        }
    }
    
    @Override
    public void onDestroyView() {
        ApplicationContext.getInstance().removeOnArticleItemsChanged(articleList.getAdapter());
        super.onDestroyView();
    }
}