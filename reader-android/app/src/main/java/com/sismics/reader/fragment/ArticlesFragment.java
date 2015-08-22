package com.sismics.reader.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.android.Log;
import com.sismics.reader.R;
import com.sismics.reader.activity.ArticleActivity;
import com.sismics.reader.constant.Constants;
import com.sismics.reader.listener.ArticlesHelperListener;
import com.sismics.reader.resource.ArticleResource;
import com.sismics.reader.resource.StarredResource;
import com.sismics.reader.ui.adapter.ArticlesAdapter;
import com.sismics.reader.ui.adapter.SharedArticlesAdapterHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        public void onStart() {}

        @Override
        public void onSuccess() {
            Bundle args = getArguments();
            int str = R.string.no_articles;
            if (args != null) {
                boolean unread = args.getBoolean("unread");
                if (unread) {
                    str = R.string.no_unread_articles;
                }
            }
            aq.id(R.id.emptyList).text(str)
                    .getTextView()
                    .setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.character);
        }

        @Override
        public void onError() {
            aq.id(R.id.emptyList).text(R.string.error_loading_articles)
                    .getTextView()
                    .setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.character_sad);
        }

        /**
         * Animate the ListView only once per instance.
         */
        private boolean listViewAnimated = false;

        @Override
        public void onEnd() {
            ListView articleList = aq.id(R.id.articleList).getListView();
            View emptyView = aq.id(R.id.emptyList).getView();

            if (!listViewAnimated) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
                articleList.startAnimation(animation);
                emptyView.startAnimation(animation);
                listViewAnimated = true;
            }

            articleList.setEmptyView(emptyView);
            aq.id(R.id.progressBar).gone();
        }
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.articles_fragment, container, false);
        aq = new AQuery(view);

        // RSSMan animation
        aq.id(R.id.emptyList).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TextView textView = (TextView) view;
                if (textView.getText().equals(getString(R.string.error_loading_articles))) {
                    // No happy animation in case of error
                    return;
                }

                // OK for animating
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.rssman);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.character_happy);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.character);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                view.startAnimation(anim);
            }
        });

        // Init fragment
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
     *
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
        final ListView articleList = aq.id(R.id.articleList)
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
            }).getListView();

        // Batch contextual actions
        articleList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        articleList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mode.setTitle(getString(R.string.selected_items, articleList.getCheckedItemCount()));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_articles, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, final MenuItem item) {
                final Set<String> articleIdList = new HashSet<>();
                final List<JSONObject> articleJsonList = new ArrayList<>();
                SparseBooleanArray booleanArray = articleList.getCheckedItemPositions();
                for (int i = 0; i < booleanArray.size(); i++) {
                    if (!booleanArray.valueAt(i)) {
                        continue;
                    }
                    JSONObject article = adapter.getItem(booleanArray.keyAt(i));
                    articleIdList.add(article.optString("id"));
                    articleJsonList.add(article);
                }

                switch (item.getItemId()) {
                    case R.id.star:
                    case R.id.unstar:
                        JsonHttpResponseHandler callback = new JsonHttpResponseHandler() {
                            public void onSuccess(JSONObject json) {
                                try {
                                    for (JSONObject article : articleJsonList) {
                                        article.put("is_starred", item.getItemId() == R.id.star);
                                    }
                                    SharedArticlesAdapterHelper.getInstance().onDataChanged();
                                } catch (JSONException e) {
                                    Log.e("ArticlesFragment", "Error starring articles", e);
                                }
                            }
                        };

                        if (item.getItemId() == R.id.star) {
                            StarredResource.starMultiple(getActivity(), articleIdList, callback);
                        } else {
                            StarredResource.unstarMultiple(getActivity(), articleIdList, callback);
                        }
                        break;

                    case R.id.read:
                    case R.id.unread:
                        callback = new JsonHttpResponseHandler() {
                            public void onSuccess(JSONObject json) {
                                try {
                                    for (JSONObject article : articleJsonList) {
                                        article.put("is_read", item.getItemId() == R.id.read);
                                    }
                                    SharedArticlesAdapterHelper.getInstance().onDataChanged();
                                } catch (JSONException e) {
                                    Log.e("ArticlesFragment", "Error unreading articles", e);
                                }
                            }
                        };

                        if (item.getItemId() == R.id.read) {
                            ArticleResource.readMultiple(getActivity(), articleIdList, callback);
                        } else {
                            ArticleResource.unreadMultiple(getActivity(), articleIdList, callback);
                        }
                        break;
                    default:
                        return false;
                }

                articleList.clearChoices();
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_ARTICLES && resultCode == Activity.RESULT_OK) {
            // We are coming back from the articles pager, scroll to the last viewed
            ListView articleList = aq.id(R.id.articleList).getListView();
            articleList.setSelectionFromTop(data.getIntExtra("position", 0), 100);
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