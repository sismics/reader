package com.sismics.reader.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.mobeta.android.dslv.DragSortListView;
import com.sismics.reader.R;
import com.sismics.reader.resource.CategoryResource;
import com.sismics.reader.resource.SubscriptionResource;
import com.sismics.reader.ui.adapter.CategoryAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manage categories activity.
 *
 * @author bgamard
 */
public class CategoriesActivity extends AppCompatActivity {

    CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activity setup
        setContentView(R.layout.categories_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Loading categories from server
        CategoryResource.list(this, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject json) {
                // Extracting categories
                JSONObject rootCategory = json.optJSONArray("categories").optJSONObject(0);
                JSONArray categories = rootCategory.optJSONArray("categories");
                List<CategoryAdapter.Category> items = new ArrayList<>();
                if (categories != null) {
                    for (int i = 0; i < categories.length(); i++) {
                        JSONObject category = categories.optJSONObject(i);
                        CategoryAdapter.Category item = new CategoryAdapter.Category();
                        item.setId(category.optString("id"));
                        item.setName(category.optString("name"));
                        items.add(item);
                    }
                }

                // Initializing the DragSortListView
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                DragSortListView categoryList = (DragSortListView) findViewById(R.id.categoryList);
                categoryAdapter = new CategoryAdapter(CategoriesActivity.this, items);
                categoryList.setAdapter(categoryAdapter);

                categoryList.setDropListener(new DragSortListView.DropListener() {
                    @Override
                    public void drop(int from, int to) {
                        categoryAdapter.move(from, to);
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(CategoriesActivity.this, R.string.error_loading_categories, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public void finish() {
        if (categoryAdapter != null && categoryAdapter.getStates().size() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.finish_save_title)
                    .setMessage(R.string.finish_save_message)
                    .setPositiveButton(R.string.save_now, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            save();
                        }
                    })
                    .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            categoryAdapter.clearStates();
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    })
                    .show();
            return;
        }

        super.finish();
    }

    private void save() {
        if (categoryAdapter != null && categoryAdapter.getStates().size() > 0) {
            // Display a progress dialog
            final ProgressDialog progressDialog = ProgressDialog.show(this,
                    null, null, true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // Cancel pending states changes and close the activity
                    CategoryResource.cancel(CategoriesActivity.this);
                    dialog.dismiss();
                    categoryAdapter.clearStates();
                    setResult(RESULT_OK);
                    finish();
                }
            });

            // Consume each state and apply them
            final AtomicReference<Runnable> atomicRunnable = new AtomicReference<>();
            atomicRunnable.set(new Runnable() {
                @Override
                public void run() {
                    if (!progressDialog.isShowing()) {
                        // The dialog has been canceled, stop here
                        return;
                    }

                    final CategoryAdapter.State state =  categoryAdapter.getStates().peek();
                    if (state == null) {
                        // All states have been pushed to the server
                        Toast.makeText(CategoriesActivity.this, R.string.manage_categories_save_success, Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        progressDialog.dismiss();
                        finish();
                        return;
                    }

                    // Update the progress dialog message
                    progressDialog.setMessage(getString(R.string.manage_categories_saving, categoryAdapter.getStates().size()));

                    // Callback to apply the next states
                    final JsonHttpResponseHandler callback = new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            // Remove the state from the queue
                            categoryAdapter.getStates().poll();

                            // Apply the next state
                            atomicRunnable.get().run();
                        }

                        @Override
                        public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(CategoriesActivity.this, R.string.manage_categories_save_error, Toast.LENGTH_LONG).show();
                            categoryAdapter.clearStates();
                            progressDialog.dismiss();
                            finish();
                        }
                    };

                    // Calling server
                    switch (state.getType()) {
                        case ADD:
                            CategoryResource.add(CategoriesActivity.this, state.getName(), new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    // Update future states to use the real ID generated by the server
                                    categoryAdapter.updateCategoryId(state.getId(), response.optString("id"));
                                    callback.onSuccess(response);
                                }

                                @Override
                                public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, Throwable error) {
                                    callback.onFailure(statusCode, headers, responseBody, error);
                                }
                            });
                            break;

                        case DELETE:
                            CategoryResource.delete(CategoriesActivity.this, state.getId(), callback);
                            break;

                        case MOVE:
                            CategoryResource.update(CategoriesActivity.this, state.getId(), null, state.getPosition(), callback);
                            break;

                        case RENAME:
                            CategoryResource.update(CategoriesActivity.this, state.getId(), state.getName(), null, callback);
                            break;
                    }
                }
            });
            atomicRunnable.get().run();
            return;
        }

        setResult(RESULT_CANCELED);
        finish();
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
            case R.id.accept:
                save();
                return true;

            case R.id.add_category:
                final EditText input = new EditText(this);
                input.setHint(R.string.category_name);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.add_category)
                        .setView(input)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                categoryAdapter.add(input.getText().toString());
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
                return true;

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
