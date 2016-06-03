package com.ndtv.core.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.ndtv.core.config.ConfigManager;

/**
 * Created by Srihari S Reddy on 06/03/15.
 */
public class AppReviewHelper {

    final static String HAS_RATED = "has_rated";
    final static String RATE_PREF = "rate_preferences";
    static SharedPreferences sharedpreferences;


    public static void incrementCount(Context mContext) {
        if(mContext == null) return;
        sharedpreferences = mContext.getSharedPreferences(RATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        long launch_count = sharedpreferences.getLong("launch_count", 0);
        editor.putLong("launch_count", ++launch_count);
        editor.commit();
    }


    public static boolean shouldShowRatings(Context mContext) {

        if(mContext==null || ConfigManager.reviewCap ==0 ) return false;

        if (mContext.getSharedPreferences(RATE_PREF, Context.MODE_PRIVATE).getBoolean("HAS_RATED", false))
            return false;

        long launchCount=mContext.getSharedPreferences(RATE_PREF, Context.MODE_PRIVATE).getLong("launch_count", 0);

        if ( launchCount > ConfigManager.reviewCap && launchCount%2 ==0 ){
            return true;
        }

        return false;
    }

    public static void resetReviewCount(Context mContext) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(RATE_PREF, Context.MODE_PRIVATE).edit();
        editor.putLong("launch_count", 0).commit();

    }

    public static void setUserStatus(Context mContext) {
       SharedPreferences.Editor editor = mContext.getSharedPreferences(RATE_PREF, Context.MODE_PRIVATE).edit();
       editor.putBoolean("HAS_RATED", true).commit();

    }
}
