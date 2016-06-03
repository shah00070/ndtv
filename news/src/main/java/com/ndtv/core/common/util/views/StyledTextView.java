package com.ndtv.core.common.util.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ndtv.core.R;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nagaraj
 */
public class StyledTextView extends TextView {
    private String TAG = StyledTextView.class.getSimpleName();

    private static Map<String, Typeface> mHashMap = new ConcurrentHashMap<String, Typeface>();

    public StyledTextView(Context context) {
        super(context);
        init();
    }

    public StyledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public StyledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomFonts);
        String customFont = a.getString(R.styleable.CustomFonts_customTypeface);

        if (!isInEditMode())
            setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf = null;
        try {
            tf = mHashMap.get(asset);
            if (tf == null) {
                tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/" + asset);
                mHashMap.put(asset, tf);
            }
        } catch (Exception e) {
            // If no typeface attribute is specify in xml.
            return false;
        }
        setTypeface(tf);
        return true;
    }

    public static void clearData() {
        if (null != mHashMap) {
            mHashMap.clear();
            mHashMap = null;
        }
    }

    private void init() {
        if (!isInEditMode())
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf"));
    }
}
