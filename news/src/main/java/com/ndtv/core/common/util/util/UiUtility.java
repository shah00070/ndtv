package com.ndtv.core.common.util.util;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by sangeetha on 19/2/15.
 */
public class UiUtility {
    public static Class<? extends FragmentActivity> sPreviousActivity = null;
    public static Class<? extends Fragment> sCurrentFragment = null;
    private static boolean mActivityStopped;
    private static boolean mAppRefreshed;

    public static void setCurrentActivity(Class<? extends FragmentActivity> previousActivity) {
        sPreviousActivity = previousActivity;
    }

    public static Class<? extends Activity> getCurrentActivity() {
        return sPreviousActivity;
    }

    //Added to fix Audio Focus issue in Webkit Pages, do not use it in any other place.
    public static void setCurrentFragment(Class<? extends Fragment> currentFragment) {
        sCurrentFragment = currentFragment;
    }

    public static Class<? extends Fragment> getsCurrentFragment() {
        return sCurrentFragment;
    }

    public static boolean isActivityStopped() {
        return mActivityStopped;
    }

    public static void activityResumed() {
        mActivityStopped = false;
    }

    public static void activityStopped() {
        mActivityStopped = true;
    }

    public static boolean isAppRefresh() {
        return mAppRefreshed;
    }

    public static void setAppREfresh(boolean value) {
        mAppRefreshed = value;
    }


}
