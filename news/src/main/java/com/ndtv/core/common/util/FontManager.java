/**
 Project      : Awaaz
 Filename     : FontManager.java
 Author       : praveenk
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;

import com.ndtv.core.common.util.views.RobotoCondensedRegularTextView;
import com.ndtv.core.common.util.views.RobotoRegularTextView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author praveenk
 */
public class FontManager extends BaseManager {

    private Map<String, Typeface> mFontsmap = new ConcurrentHashMap<String, Typeface>();

    private static FontManager sFontManager;

    private FontManager() {

    }

    public static synchronized FontManager getInstance() {
        if (sFontManager == null) {
            sFontManager = new FontManager();
        }
        return sFontManager;

    }

    public void loadFonts(final Context context) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... params) {
                /*mFontsmap.put(RobotoBoldTextView.ROBOTO_BOLD,
						Typeface.createFromAsset(context.getAssets(), RobotoBoldTextView.ROBOTO_BOLD));

				mFontsmap.put(RobotoCondensedBoldTextView.ROBOTO_CONDENSED_BOLD, Typeface.createFromAsset(
                        context.getAssets(), RobotoCondensedBoldTextView.ROBOTO_CONDENSED_BOLD));*/

                mFontsmap.put(RobotoCondensedRegularTextView.ROBOTO_CONDENSED_REGULAR, Typeface.createFromAsset(
                        context.getAssets(), RobotoCondensedRegularTextView.ROBOTO_CONDENSED_REGULAR));

				/*mFontsmap.put(RobotoCondensedTextView.ROBOTO_CONDENSED,
						Typeface.createFromAsset(context.getAssets(), RobotoCondensedTextView.ROBOTO_CONDENSED));

				mFontsmap.put(RobotoMediumTextView.ROBOTO_MEDIUM,
						Typeface.createFromAsset(context.getAssets(), RobotoMediumTextView.ROBOTO_MEDIUM));*/

                mFontsmap.put(RobotoRegularTextView.ROBOTO_REGULAR,
                        Typeface.createFromAsset(context.getAssets(), RobotoRegularTextView.ROBOTO_REGULAR));
                return null;
            }
        }.execute();
    }

    @Override
    public void cleanUp() {
        if (null != mFontsmap) {
            mFontsmap.clear();
            mFontsmap = null;
        }
        sFontManager = null;
    }

    public Typeface getFont(String fontKey) {
        return mFontsmap.get(fontKey);

    }

}
