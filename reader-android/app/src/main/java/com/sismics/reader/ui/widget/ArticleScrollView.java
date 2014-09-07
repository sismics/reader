package com.sismics.reader.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.sismics.parallaxscroll.ParallaxScrollView;

/**
 * ScrollView used to view an article.
 *
 * @author bgamard.
 */
public class ArticleScrollView extends ParallaxScrollView {

    private OnScrollChangedListener onScrollChangedListener;
    private int down = 0;
    private int up = 0;

    public ArticleScrollView(Context context) {
        super(context);
    }

    public ArticleScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int w, int h, int ow, int oh) {
        super.onScrollChanged(w, h, ow, oh);

        if (onScrollChangedListener != null) {
            if (oh < h) {
                down += h - oh;
                up = 0;
            } else {
                down = 0;
                up += oh - h;
            }

            if (down > 50 && h > onScrollChangedListener.getDeadHeight()) {
                onScrollChangedListener.onScrollDown();
            } else if (up > 50 || h < onScrollChangedListener.getDeadHeight()) {
                onScrollChangedListener.onScrollUp();
            }
        }
    }

    /**
     * Setter of onScrollChangedListener.
     * @param onScrollChangedListener OnScrollChangedListener
     */
    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        this.onScrollChangedListener = onScrollChangedListener;
    }

    /**
     * Listen for scroll direction changes.
     */
    public interface OnScrollChangedListener {

        /**
         * Dead zone on top of the scroll view where scroll change is not triggered.
         */
        public int getDeadHeight();

        /**
         * The view is scrolled up.
         */
        public void onScrollUp();

        /**
         * The view is scrolled down.
         */
        public void onScrollDown();
    }
}
