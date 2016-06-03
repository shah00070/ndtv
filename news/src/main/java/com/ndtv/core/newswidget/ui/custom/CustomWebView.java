package com.ndtv.core.newswidget.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by root on 10/3/15.
 */
public class CustomWebView extends WebView {

    public CustomWebView(Context context) {
        super(context);
    }

    public CustomWebView(Context context,AttributeSet attributeSet){
        super(context,attributeSet);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }
}
