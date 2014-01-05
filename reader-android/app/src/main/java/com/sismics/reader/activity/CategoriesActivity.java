package com.sismics.reader.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mobeta.android.dslv.DragSortListView;
import com.sismics.reader.R;
import com.sismics.reader.resource.SubscriptionResource;
import com.sismics.reader.ui.adapter.CategoryAdapter;
import com.sismics.reader.ui.adapter.SubscriptionItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage categories activity.
 *
 * @author bgamard
 */
public class CategoriesActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activity setup
        setContentView(R.layout.categories_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Loading categories from server
        SubscriptionResource.list(this, false, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                // Extracting categories
                JSONObject rootCategory = json.optJSONArray("categories").optJSONObject(0);
                JSONArray categories = rootCategory.optJSONArray("categories");
                List<CategoryAdapter.Category> items = new ArrayList<CategoryAdapter.Category>();
                if (categories != null) {
                    for (int i = 0; i < categories.length(); i++) {
                        JSONObject category = categories.optJSONObject(i);
                        CategoryAdapter.Category item = new CategoryAdapter.Category();
                        item.setId(category.optString("id"));
                        item.setTitle(category.optString("name"));
                        items.add(item);
                    }
                }

                // Initializing the DragSortListView
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                DragSortListView categoryList = (DragSortListView) findViewById(R.id.categoryList);
                final CategoryAdapter categoryAdapter = new CategoryAdapter(CategoriesActivity.this, items);
                categoryList.setAdapter(categoryAdapter);

                categoryList.setDropListener(new DragSortListView.DropListener() {
                    @Override
                    public void drop(int from, int to) {
                        categoryAdapter.move(from, to);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.categories_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SubscriptionResource.cancel(this);
    }
}
