package com.sismics.reader.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.android.Log;
import com.sismics.android.SystemBarTintManager;
import com.sismics.reader.R;
import com.sismics.reader.listener.ArticlesHelperListener;
import com.sismics.reader.resource.ArticleResource;
import com.sismics.reader.resource.StarredResource;
import com.sismics.reader.ui.adapter.ArticlesAdapter;
import com.sismics.reader.ui.adapter.ArticlesPagerAdapter;
import com.sismics.reader.ui.adapter.SharedArticlesAdapterHelper;
import com.viewpagerindicator.UnderlinePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Activity displaying articles.
 * 
 * @author bgamard
 */
public class ArticleActivity extends FragmentActivity {

    // UI cache
    private ViewPager viewPager;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private View drawer;
    private MenuItem starMenuItem;
    private ShareActionProvider shareActionProvider;

    /**
     * Use low profile mode in immersion mode.
     */
    boolean lowProfile;

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
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onSuccess() {}

        @Override
        public void onError() {
            Toast.makeText(ArticleActivity.this, R.string.error_loading_articles, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onEnd() {
            setProgressBarIndeterminateVisibility(false);
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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.article_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        if (Build.VERSION.SDK_INT >= 19) {
            lowProfile = !new SystemBarTintManager(this).getConfig().isNavigationAtBottom();
        }
        showSystemUi();

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
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        final ArticlesPagerAdapter adapter = new ArticlesPagerAdapter(getSupportFragmentManager());
        sharedAdapterHelper.addAdapter(adapter, articlesHelperListener);
        viewPager.setAdapter(adapter);

        // Pretty animation between pages
        // Issue #89 : This animation blocks the vertical scrolling on API16 (at least)
        // viewPager.setPageTransformer(true, new CardTransformer(.7f));

        // Configure the ViewPagerIndicator
        int position = getIntent().getIntExtra("position", 0);
        UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        if (indicator != null) {
            indicator.setViewPager(viewPager, position);
            indicator.setOnPageChangeListener(onPageChangeListener);
        } else {
            viewPager.setOnPageChangeListener(onPageChangeListener);
        }

        // Configure the ListView
        drawerList = (ListView) findViewById(R.id.drawer_list);
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
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer = findViewById(R.id.left_drawer);

        if (drawerLayout != null) {
            // Set a custom shadow that overlays the main content when the drawer opens
            drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            drawerLayout.setScrimColor(getResources().getColor(R.color.drawer_shadow));

            drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
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
        viewPager.setCurrentItem(position);
        onPageChangeListener.onPageSelected(position);
    }

    /**
     * Called when the drawer is just opened.
     */
    private void onArticlesDrawerOpened() {
        invalidateOptionsMenu();
        getActionBar().show();

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
        
        // Fetch and store ShareActionProvider to feed later
        MenuItem item = menu.findItem(R.id.share);
        shareActionProvider = (ShareActionProvider) item.getActionProvider();
        
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
        
        // Update the share action provider with a new intent
        if (shareActionProvider != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, article.optString("title"));
            intent.putExtra(Intent.EXTRA_TEXT, article.optString("url"));
            intent.setType("text/plain");
            shareActionProvider.setShareIntent(intent);
        }
        
        // Update the favorite button
        if (starMenuItem != null) {
            boolean isStarred = article.optBoolean("is_starred");
            starMenuItem.setIcon(isStarred ? R.drawable.ic_action_important_inverse : R.drawable.ic_action_not_important_inverse);
        }

        // Show the possibly hidden action bar and system UI
        getActionBar().show();
        showSystemUi();
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

    /**
     * Hide the system UI on API >= 19 (immersive mode).
     */
    @TargetApi(19)
    public void hideSystemUi() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | (lowProfile ? View.SYSTEM_UI_FLAG_LOW_PROFILE : View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    /**
     * Show the system UI on API >= 19 (immersive mode).
     */
    @TargetApi(19)
    public void showSystemUi() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}
