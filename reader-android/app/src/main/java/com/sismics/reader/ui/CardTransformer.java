package com.sismics.reader.ui;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Launcher-inspired page transformer.
 *
 * @author bgamard.
 */
public class CardTransformer implements ViewPager.PageTransformer {

    private final float scalingStart;

    public CardTransformer(float scalingStart) {
        super();
        this.scalingStart = 1 - scalingStart;
    }

    @Override
    public void transformPage(View page, float position) {

        if (position >= 0) {
            final int w = page.getWidth();
            float scaleFactor = 1 - scalingStart * position;

            page.setAlpha(1 - position);
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setTranslationX(w * (1 - position) - w);
        }
    }
}