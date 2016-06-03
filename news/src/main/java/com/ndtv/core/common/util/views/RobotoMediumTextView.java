/**
 Project      : Awaaz
 Filename     : RobotoMediumTextView.java
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
public class RobotoMediumTextView extends TextView {
    public static final String ROBOTO_MEDIUM = "fonts/Roboto-Medium.ttf";

    /**
     * @param context
     */
    public RobotoMediumTextView(Context context) {
        super(context);
        init();
    }

    public RobotoMediumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoMediumTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!isInEditMode()) {

            Typeface font = FontManager.getInstance().getFont(ROBOTO_MEDIUM);
            if (font != null) {
                setTypeface(font);
            }
        }
    }
}
