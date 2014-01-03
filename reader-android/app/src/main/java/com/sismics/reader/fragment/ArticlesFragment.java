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
        public void onSuccess() {
            aq.id(R.id.emptyList).text(R.string.no_articles);;
        }

        @Override
        public void onError() {
            aq.id(R.id.emptyList).text(R.string.error_loading_articles);
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
            String title = args.getString("title");
            String id = args.getString("id");
            String url = args.getString("url");
            boolean unread = args.getBoolean("unread");
            if (url != null) {
                initFragment(title, id, url, unread, savedInstanceState);
            }
        }
        
        return view;
    }

    /**
     * Load articles.
     * @param title Title
     * @param subscriptionId Subscription ID
     * @param url URL
     * @param unread Unread state
     * @param savedInstanceState Saved bundle
     */
    private void initFragment(final String title, final String subscriptionId, final String url, final boolean unread, Bundle savedInstanceState) {
        // Initializing empty view
        aq.id(R.id.articleList)
            .getListView()
            .setEmptyView(aq.id(R.id.progressBar).getView());
        aq.id(R.id.loadingText).text(R.string.loading_articles);

        // Load articles or not? It depends if we are restoring the parent activity
        if (savedInstanceState == null) {
            SharedArticlesAdapterHelper.getInstance().restart(url, unread);
            SharedArticlesAdapterHelper.getInstance().load(getActivity());
        } else {
            articlesHelperListener.onSuccess();
            articlesHelperListener.onEnd();
        }

        // Start listening to articles loading states
        final ArticlesAdapter adapter = new ArticlesAdapter(getActivity());
        SharedArticlesAdapterHelper.getInstance().addAdapter(adapter, articlesHelperListener);

        // Configure the articles list to listen for scrolls (infinite loading) and clicks
        aq.id(R.id.articleList)
            .adapter(adapter)
            .scrolled(new OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {}
                
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
                    intent.putExtra("id", subscriptionId);
                    intent.putExtra("title", title);
                    intent.putExtra("position", position);
                    intent.putExtra("url", url);
                    intent.putExtra("unread", unread);
                    startActivityForResult(intent, Constants.REQUEST_CODE_ARTICLES);
                }
            });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_ARTICLES && resultCode == Activity.RESULT_OK) {
            // We are coming back from the articles pager, scroll to the last viewed
            ListView articleList = aq.id(R.id.articleList).getListView();
            articleList.setSelection(data.getIntExtra("position", 0));
            ((ArticlesAdapter)articleList.getAdapter()).notifyDataSetChanged();
        }
    }
    
    @Override
    public void onDestroyView() {
        // Don't forget to stop listening to articles loading state to avoid crash and memory leaks
        Adapter adapter = aq.id(R.id.articleList).getListView().getAdapter();
        SharedArticlesAdapterHelper.getInstance().removeAdapter(adapter, articlesHelperListener);
        super.onDestroyView();
    }
}