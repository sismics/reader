package com.sismics.reader.ui.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.widget.ListView;

import com.sismics.reader.R;

/**
 * Subclassing of SwipeRefreshLayout to handle our custom case.
 *
 * @author bgamard.
 */
public class ArticlesSwipeRefreshLayout extends SwipeRefreshLayout {
    public ArticlesSwipeRefreshLayout(Context context) {
        super(context);
    }

    public ArticlesSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        ListView listView = (ListView) findViewById(R.id.articleList);
        return listView != null && listView.getChildCount() > 0
                && (listView.getFirstVisiblePosition() > 0 || listView.getChildAt(0)
                .getTop() < listView.getPaddingTop());
    }
}
