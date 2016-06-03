package com.ndtv.core.common.util;

import android.content.Context;
import android.content.Intent;

import com.ndtv.core.NdtvApplication;
import com.ndtv.core.common.util.util.UiUtility;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.ui.SplashActivity;

/**
 * Created by sangeetha on 1/6/15.
 */
public class SplashAdManager {
    private static SplashAdManager sSplashAdMngr;
    private Context mContext;
    private boolean mIsFromShare = false;


    public static synchronized SplashAdManager getSplashAdMngrInstance(Context context) {
        if (null == sSplashAdMngr) {
            sSplashAdMngr = new SplashAdManager();
        }
        return sSplashAdMngr;
    }

    private SplashAdManager() {

    }


    public void increaseLaunchCount(Context context) {
        mContext = context;
        if (!mIsFromShare) {

            PreferencesManager prefMngr = PreferencesManager.getInstance(mContext);
            //this is added to avoid the count increment, when onResume() of BaseActivity is called after showing splash ads
            if (!prefMngr.isAppFirstLaunch()) {
                if (UiUtility.isActivityStopped()) {
                    int launchCount = prefMngr.getAppLaunchCount() + 1;
                    prefMngr.setAppLaunchCount(launchCount);
                    if ((launchCount % Integer.parseInt(prefMngr.getAdFrequency())) == 0 && TimeUtils.isValidDate(mContext)
                            && prefMngr.getAdStatus()) {

                        showSplashAd();
                    }
                }
                UiUtility.activityResumed();
            } else
                prefMngr.setAppFirstLaunch(false);
        } else{
            mIsFromShare = false;
            if(UiUtility.isActivityStopped())
                UiUtility.activityResumed();
        }

    }

    private void showSplashAd() {
        Intent intent = new Intent(mContext, SplashActivity.class);
        intent.putExtra(ApplicationConstants.PreferenceKeys.SPLASH_AD, true);
        mContext.startActivity(intent);
    }

    public void signInBtnClicked(boolean value) {
        mIsFromShare = value;
    }
}


