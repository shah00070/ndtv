/**
 Project      : Awaaz
 Filename     : RobotoCondensedBoldTextView.java
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
public class RobotoCondensedBoldTextView extends TextView {
    public static final String ROBOTO_CONDENSED_BOLD = "fonts/RobotoCondensed-Bold.ttf";

    /**
     * @param context
     */
    public RobotoCondensedBoldTextView(Context context) {
        super(context);
        init();
    }

    public RobotoCondensedBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoCondensedBoldTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (!isInEditMode()) {

            Typeface font = FontManager.getInstance().getFont(ROBOTO_CONDENSED_BOLD);
            if (font != null) {
                setTypeface(font);
            }
        }
    }
}
