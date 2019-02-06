package com.sismics.reader.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.android.Log;
import com.sismics.reader.R;
import com.sismics.reader.listener.ArticlesHelperListener;
import com.sismics.reader.resource.ArticleResource;
import com.sismics.reader.resource.StarredResource;
import com.sismics.reader.ui.adapter.ArticlesAdapter;
import com.sismics.reader.ui.adapter.ArticlesPagerAdapter;
import com.sismics.reader.ui.adapter.SharedArticlesAdapterHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Activity displaying articles.
 * 
 * @author bgamard
 */
public class ArticleActivity extends AppCompatActivity {

    // UI cache
    private ViewPager viewPager;
    private ProgressBar progressBar;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private View drawer;
    private MenuItem starMenuItem;
    private Intent shareIntent;

    /**
     * Articles to mark as read later.
     */
    private Set<String> readArticleIdSet;
    
    /**
     * Shared articles adapter helper.
     */
    private SharedArticlesAdapterHelper sharedAdapterHelper;
    
    /**
     * Articles loading listener.
     */
    private ArticlesHelperListener articlesHelperListener = new ArticlesHelperListener() {
        @Override
        public void onStart() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess() {}

        @Override
        public void onError() {
            Toast.makeText(ArticleActivity.this, R.string.error_loading_articles, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onEnd() {
            progressBar.setVisibility(View.INVISIBLE);
        }
    };
    
    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);
        
        readArticleIdSet = new HashSet<>();
        sharedAdapterHelper = SharedArticlesAdapterHelper.getInstance();
        if (sharedAdapterHelper.getArticleItems().size() == 0) {
            finish();
            return;
        }

        // Configure the activity
        setContentView(R.layout.article_activity);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.progress_spinner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Building page change listener
        OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // We reached the latest page, load more articles if needed
                if (position + 1 >= sharedAdapterHelper.getArticleItems().size()) {
                    sharedAdapterHelper.load(ArticleActivity.this);
                }
                
                // Store article id to mark as read later
                final JSONObject article = sharedAdapterHelper.getArticleItems().get(position);
                String articleId = article.optString("id");
                if (!readArticleIdSet.contains(articleId) && !article.optBoolean("force_unread")) {
                    readArticleIdSet.add(article.optString("id"));
                }

                // Scroll the ListView
                if (drawerLayout == null) {
                    if (drawerList.getCheckedItemCount() == 0) {
                        // Don't be smooth when the activity has just opened
                        drawerList.setSelectionFromTop(position, 100);
                    } else {
                        drawerList.smoothScrollToPositionFromTop(position, 100);
                    }
                    drawerList.setItemChecked(position, true);
                }

                // Update the action bar
                updateActionBar();
            }

            float countPositionOffset = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (!sharedAdapterHelper.isFullyLoaded()) {
                    return;
                }

                // If all articles are loaded and the user try to swipe right, open the drawer
                if (positionOffset == 0) {
                    countPositionOffset++;
                } else {
                    countPositionOffset = 0;
                }

                if (countPositionOffset == 5) {
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(drawer);
                        onArticlesDrawerOpened();
                    }
                }
            }
            
            @Override
            public void onPageScrollStateChanged(int state) {}
        };

        // Setting the title
        setTitle(getIntent().getStringExtra("title"));

        // Configure the ViewPager
        viewPager = findViewById(R.id.viewPager);
        final ArticlesPagerAdapter adapter = new ArticlesPagerAdapter(getSupportFragmentManager());
        sharedAdapterHelper.addAdapter(adapter, articlesHelperListener);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);

        // Configure the ListView
        drawerList = findViewById(R.id.drawer_list);
        ArticlesAdapter listAdapter = new ArticlesAdapter(this);
        sharedAdapterHelper.addAdapter(listAdapter, null);

        // Infinite scrolling
        AQuery aq = new AQuery(this);
        aq.id(R.id.drawer_list)
            .adapter(listAdapter)
            .scrolled(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem + visibleItemCount >= totalItemCount - 2) {
                        SharedArticlesAdapterHelper.getInstance().load(ArticleActivity.this);
                    }
                }
            });

        // List item handling
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewPager.setCurrentItem(position);
                // Close the drawer if asked
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(drawer);
                }
            }
        });

        // Configure the drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        drawer = findViewById(R.id.left_drawer);

        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View view, float v) {}

                @Override
                public void onDrawerStateChanged(int state) {}

                @Override
                public void onDrawerOpened(View view) {
                    onArticlesDrawerOpened();
                }

                @Override
                public void onDrawerClosed(View view) {
                    invalidateOptionsMenu();
                }
            });
        }

        // Forcing page change listener
        int position = getIntent().getIntExtra("position", 0);
        viewPager.setCurrentItem(position);
        onPageChangeListener.onPageSelected(position);
    }

    /**
     * Called when the drawer is just opened.
     */
    private void onArticlesDrawerOpened() {
        invalidateOptionsMenu();
        getSupportActionBar().show();

        // Sync the ListView with the ViewPager
        int position = viewPager.getCurrentItem();
        drawerList.setItemChecked(position, true);
        if (position <= drawerList.getFirstVisiblePosition() || position >= drawerList.getLastVisiblePosition()) {
            drawerList.setSelectionFromTop(position, 200);
        }
    }
    
    @Override
    public void finish() {
        Intent data = new Intent();
        if (viewPager != null) {
            data.putExtra("position", viewPager.getCurrentItem());
            sharedAdapterHelper.removeAdapter(viewPager.getAdapter(), articlesHelperListener);
        }
        if (drawerList != null) {
            sharedAdapterHelper.removeAdapter(drawerList.getAdapter(), null);
        }
        setResult(RESULT_OK, data);
        
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.article_activity, menu);
        
        // Store favorite button to change his icon
        starMenuItem = menu.findItem(R.id.star);
        
        updateActionBar();
        return super.onCreateOptionsMenu(menu);
    }
    
    /**
     * Update the action bar with current article context.
     */
    private void updateActionBar() {
        final JSONObject article = sharedAdapterHelper.getArticleItems().get(viewPager.getCurrentItem());
        
        // Update the share intent
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.optString("title"));
        shareIntent.putExtra(Intent.EXTRA_TEXT, article.optString("url"));
        shareIntent.setType("text/plain");

        // Update the favorite button
        if (starMenuItem != null) {
            boolean isStarred = article.optBoolean("is_starred");
            starMenuItem.setIcon(isStarred ? R.drawable.ic_action_important_inverse : R.drawable.ic_action_not_important_inverse);
        }

        // Show the possibly hidden action bar and system UI
        getSupportActionBar().show();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final JSONObject article = sharedAdapterHelper.getArticleItems().get(viewPager.getCurrentItem());
        final String articleId = article.optString("id");
        
        // Button as progress bar during network work
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View progressView = inflater.inflate(R.layout.actionbar_indeterminate_progress, null);
        
        switch (item.getItemId()) {
            case R.id.unread:
                // Flagging article as unread
                try {
                    article.put("force_unread", true);
                } catch (JSONException e) {
                    Log.e("ArticleActivity", "Error forcing article at unread state", e);
                }

                // Removing from mark as read list
                readArticleIdSet.remove(articleId);

                item.setActionView(progressView);

                // Marking article as unread
                ArticleResource.unread(ArticleActivity.this, articleId, new JsonHttpResponseHandler() {
                    public void onSuccess(JSONObject json) {
                        try {
                            article.put("is_read", false);
                            sharedAdapterHelper.onDataChanged();
                            Toast.makeText(ArticleActivity.this, R.string.marked_as_unread, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Log.e("ArticleActivity", "Error changing read state", e);
                        }
                    }

                    @Override
                    public void onFinish() {
                        item.setActionView(null);
                    }
                });
                return true;
            
            case R.id.star:
                item.setActionView(progressView);

                final boolean isStarred = article.optBoolean("is_starred");

                // Star or unstar the article
                StarredResource.star(ArticleActivity.this, articleId, !isStarred, new JsonHttpResponseHandler() {
                    public void onSuccess(JSONObject json) {
                        try {
                            article.put("is_starred", !isStarred);
                            updateActionBar();
                            sharedAdapterHelper.onDataChanged();
                        } catch (JSONException e) {
                            Log.e("ArticleActivity", "Error starring/unstarring article", e);
                        }
                    }

                    @Override
                    public void onFinish() {
                        item.setActionView(null);
                    }
                });
                return true;

            case R.id.share:
                if (shareIntent != null) {
                    startActivity(Intent.createChooser(shareIntent, getText(R.string.share_to)));
                }
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onPause() {
        if (readArticleIdSet != null && !readArticleIdSet.isEmpty()) {
            // Mark articles as read on local data
            for (JSONObject article : sharedAdapterHelper.getArticleItems()) {
                String articleId = article.optString("id");
                if (readArticleIdSet.contains(articleId)) {
                    try {
                        article.put("is_read", true);
                    } catch (JSONException e) {
                        Log.e("ArticleActivity", "Error changing read state", e);
                    }
                }
            }
            sharedAdapterHelper.onDataChanged();

            // Update the server
            ArticleResource.readMultiple(ArticleActivity.this, readArticleIdSet, new JsonHttpResponseHandler());
        }
        super.onPause();
    }
}
