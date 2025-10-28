package com.jetsynthesys.rightlife.ui.affirmation;

import android.view.View;
import androidx.viewpager.widget.ViewPager;

public class AffirmationPageTransformer implements ViewPager.PageTransformer {

    private int mScaleOffset = 40;

    public void setScaleOffset(int mScaleOffset) {
        this.mScaleOffset = mScaleOffset;
    }

    public void transformPage(View page, float position) {
        float scaleFactor;
        float alphaFactor;
        int pageWidth = page.getWidth();

        if (position < -0.5f) {
            // Pages far to the left - hide completely
            page.setAlpha(0f);
            page.setTranslationX(-pageWidth * 3);

        } else if (position <= 0) {
            // Current page or transitioning left [-0.5, 0]

            // Calculate alpha - fade out as it goes left
            alphaFactor = 1f + position; // Will be 0.5 to 1.0
            page.setAlpha(alphaFactor);

            // Push the card more to the left
            float translationX = (pageWidth / 3f) * position;
            page.setTranslationX(translationX - (pageWidth * position * 0.5f)); // Extra push

            page.setRotation(45 * position);
            page.setScaleX(1f);
            page.setScaleY(1f);
            page.setTranslationY(0f);

        } else if (position <= 1) {
            // Page to the right [0, 1]
            scaleFactor = (pageWidth - mScaleOffset * position) / (float) pageWidth;
            scaleFactor = Math.max(scaleFactor, 0.75f);

            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            page.setTranslationX(-pageWidth * position);
            page.setTranslationY(mScaleOffset * 0.8f * position);

            alphaFactor = Math.max(0.5f, 1f - (position * 0.5f));
            page.setAlpha(alphaFactor);

            page.setRotation(-5 * position);

        } else {
            // Page is off-screen to the right
            page.setAlpha(0f);
        }
    }
}
/*
package com.jetsynthesys.rightlife.ui.affirmation;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class AffirmationPageTransformer implements ViewPager.PageTransformer {

    private int mScaleOffset = 40;

    public void setScaleOffset(int mScaleOffset) {
        this.mScaleOffset = mScaleOffset;
    }

    public void transformPage(View page, float position) {
        if (position <= 0.0f) {
            page.setTranslationX(0f);
            page.setRotation((45 * position));
            page.setTranslationX(((float) page.getWidth() / 3 * position));
        } else {
            float scale = (page.getWidth() - mScaleOffset * position) / (float) (page.getWidth());

            page.setScaleX(scale);
            page.setScaleY(scale);

            page.setTranslationX((-page.getWidth() * position));
            page.setTranslationY((mScaleOffset * 0.8f) * position);
        }
    }
}*/
