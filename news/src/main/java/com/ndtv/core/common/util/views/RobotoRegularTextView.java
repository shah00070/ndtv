/**
 Project      : Awaaz
 Filename     : RobotoRegularTextView.java
 Author       : praveenk
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ndtv.core.common.util.FontManager;

/**
 * @author praveenk
 */
public class RobotoRegularTextView extends TextView {
    public static final String ROBOTO_REGULAR = "fonts/Roboto-Regular.ttf";

    /**
     * @param context
     */
    public RobotoRegularTextView(Context context) {
        super(context);
        init();
    }

    public RobotoRegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoRegularTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!isInEditMode()) {

            Typeface font = FontManager.getInstance().getFont(ROBOTO_REGULAR);
            if (font != null) {
                setTypeface(font);
            }
        }
    }
}
