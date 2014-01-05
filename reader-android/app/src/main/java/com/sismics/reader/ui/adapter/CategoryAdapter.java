package com.sismics.reader.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.reader.R;
import com.sismics.reader.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for categories list.
 * 
 * @author bgamard
 */
public class CategoryAdapter extends BaseAdapter {

    /**
     * Items in list.
     */
    private List<Category> items;

    /**
     * Context.
     */
    private Context context;

    /**
     * AQuery.
     */
    private AQuery aq;

    /**
     * Constructor.
     * @param context Context
     * @param items Categories
     */
    public CategoryAdapter(Context context, List<Category> items) {
        this.context = context;
        this.items = items;
        this.aq = new AQuery(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Category item = getItem(position);
        
        // Inflating the right layout
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layout = R.layout.manage_list_item_category;
            view = vi.inflate(layout, null);
        }
        
        // Recycling AQuery
        aq.recycle(view);

        aq.id(R.id.title).text(item.getTitle());
        
        return view;
    }

    @Override
    public int getCount() {
        return items.size();
    }
    
    @Override
    public Category getItem(int position) {
        try {
            return items.get(position);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId().hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * Move an item.
     * @param from From position
     * @param to To this position
     */
    public void move(int from, int to) {
        Category category = items.remove(from);
        items.add(to, category);
        notifyDataSetChanged();
    }

    /**
     * A category.
     */
    public static class Category {

        private String id;
        private String title;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
