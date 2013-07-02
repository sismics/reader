package com.sismics.reader.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.sismics.reader.R;
import com.sismics.reader.activity.ArticleActivity;
import com.sismics.reader.constant.Constants;
import com.sismics.reader.listener.ArticlesHelperListener;
import com.sismics.reader.ui.adapter.ArticlesAdapter;
import com.sismics.reader.ui.adapter.SharedArticlesAdapterHelper;

/**
 * Articles list fragment.
 * 
 * @author bgamard
 */
public class ArticlesFragment extends NavigationFragment {
    
    /**
     * AQuery.
     */
    private AQuery aq;
    
    /**
     * Articles loading listener.
     */
    private ArticlesHelperListener articlesHelperListener = new ArticlesHelperListener() {
        @Override
        public void onStart() {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }
        
        @Override
        public void onEnd() {
            getActivity().setProgressBarIndeterminateVisibility(false);
            aq.id(R.id.articleList).getListView().setEmptyView(aq.id(R.id.emptyList).getView());
            aq.id(R.id.progressBar).gone();
        }
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.articles_fragment, container, false);
        aq = new AQuery(view);
        
        Bundle args = getArguments();
        if (args != null) {
            String url = args.getString("url");
            boolean unread = args.getBoolean("unread");
            if (url != null) {
                initFragment(url, unread, savedInstanceState);
            }
        }
        
        return view;
    }

    /**
     * Load articles.
     * @param url
     */
    private void initFragment(final String url, final boolean unread, Bundle savedInstanceState) {
        aq.id(R.id.articleList).getListView().setEmptyView(aq.id(R.id.progressBar).getView());
        
        if (savedInstanceState == null) {
            SharedArticlesAdapterHelper.getInstance().restart(url, unread);
            SharedArticlesAdapterHelper.getInstance().load(getActivity());
        } else {
            articlesHelperListener.onEnd();
        }
        
        final ArticlesAdapter adapter = new ArticlesAdapter(getActivity());
        SharedArticlesAdapterHelper.getInstance().addAdapter(adapter, articlesHelperListener);
        
        aq.id(R.id.articleList)
            .adapter(adapter)
            .scrolled(new OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }
                
                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem + visibleItemCount >= totalItemCount - 2) {
                        SharedArticlesAdapterHelper.getInstance().load(getActivity());
                    }
                }
            })
            .itemClicked(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), ArticleActivity.class);
                    intent.putExtra("position", position);
                    startActivityForResult(intent, Constants.REQUEST_CODE_ARTICLES);
                }
            });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_ARTICLES && resultCode == Activity.RESULT_OK) {
            ListView articleList = aq.id(R.id.articleList).getListView();
            articleList.smoothScrollToPosition(data.getIntExtra("position", 0));
            ((ArticlesAdapter)articleList.getAdapter()).notifyDataSetChanged();
        }
    }
    
    @Override
    public void onDestroyView() {
        Adapter adapter = aq.id(R.id.articleList).getListView().getAdapter();
        SharedArticlesAdapterHelper.getInstance().removeAdapter(adapter, articlesHelperListener);
        super.onDestroyView();
    }
}