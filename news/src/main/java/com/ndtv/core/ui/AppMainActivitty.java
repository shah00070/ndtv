package com.ndtv.core.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.R;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.SplashAdManager;
import com.ndtv.core.common.util.TimeUtils;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.util.UiUtility;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.share.FacebookHelper;
import com.ndtv.core.share.GooglePlusHelper;
import com.ndtv.core.share.ShareItem;
import com.ndtv.core.video.ui.VideoDetailFragment;

/**
 * Created by laveen on 18/2/15.
 */
public class AppMainActivitty extends ActionBarActivity implements ApplicationConstants.SocialShare {

    protected Toolbar mActionBarToolbar;

    protected FacebookHelper mFbHelper;

    protected GooglePlusHelper mGpHelper;

    public static final int RC_SIGN_IN = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFbHelper = FacebookHelper.getInstance(this);
        mFbHelper.oncreate(savedInstanceState);
        mFbHelper.setOnFBSignedInListener(null);

        mGpHelper = GooglePlusHelper.getInstance(this);
        mGpHelper.setOnSignedInListener(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFbHelper != null)
            mFbHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
            //Splash ads code,
            if (this.getClass() == UiUtility.getCurrentActivity()) {
                SplashAdManager splashAdManager = SplashAdManager.getSplashAdMngrInstance(this);

                splashAdManager.increaseLaunchCount(AppMainActivitty.this);
            }
        UiUtility.setCurrentActivity(this.getClass());
        if (mFbHelper != null) {
            mFbHelper.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFbHelper != null)
            mFbHelper.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        UiUtility.activityStopped();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFbHelper != null) {
            mFbHelper.onDestroy();
            mFbHelper.clear();
        }
        UiUtility.setCurrentActivity(null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarToolbar = getActionBarToolbar();
    }

    public Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    public void setTitle(CharSequence charsequence) {
        if (charsequence != null) {
            super.setTitle(charsequence);
            return;
        } else {
            super.setTitle(getString(R.string.app_name));
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == GP_RESOLVE_ERR_REQUEST_CODE || requestCode == RC_SIGN_IN)) {
            if (mGpHelper != null)
                mGpHelper.onActivityResult(requestCode, resultCode, data);
        }
    }
}
