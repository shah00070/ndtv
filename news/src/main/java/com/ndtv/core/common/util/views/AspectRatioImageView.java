package com.ndtv.core.common.util.views;

import android.content.Context;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by sangeetha on 3/7/15.
 */
public class AspectRatioImageView extends NetworkImageView {
    private static final float DEFAULT_ASPECT_RATIO = 1.33F;

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = this.getMeasuredWidth();
        int height = this.getMeasuredHeight();
        //Calculate height based on aspect ratio
        height = (int) ((float) width / DEFAULT_ASPECT_RATIO);


        this.setMeasuredDimension(width, height);
    }
}
