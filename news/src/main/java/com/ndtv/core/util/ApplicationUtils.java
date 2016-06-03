package com.ndtv.core.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.ConfigManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Srihari S Reddy on 15/01/15.
 */
public class ApplicationUtils {

    private static final String TAG = makeLogTag(ApplicationUtils.class);

    public static final String FEED_DATE_FORMAT = "MMMMM d, y h:m a";//January 16, 2015 04:47 PM
    public static final String SQLITE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";//2015-01-23 11:22:00
    public static final String OUT_DATE_FORMAT = "MMMM d, y hh:mm a Z";//2015-01-23 11:22:00
    public static final TimeZone TIMEZONE_IST = TimeZone.getTimeZone("Asia/Calcutta");
    public static final SimpleDateFormat sqliteDateFormat = new SimpleDateFormat(SQLITE_DATE_FORMAT);
    public static final SimpleDateFormat feedDateFormat = new SimpleDateFormat(FEED_DATE_FORMAT);
    public static final SimpleDateFormat outDateFormat = new SimpleDateFormat(OUT_DATE_FORMAT);
    public static final int ANIMATION_FADE_IN_TIME = 250;
    public static final long TIMEZONE_OFFSET = getTimeZoneOffset(TIMEZONE_IST);
    public static final String DEFAULT_COMMENT = "...";
    public static final String APPLICATION_LAUNCH = "ApplicationLaunch";

    private static long getTimeZoneOffset(TimeZone timezoneIst) {
        Calendar calendar = Calendar.getInstance();
        TimeZone fromTimeZone = calendar.getTimeZone();
        return timezoneIst.getOffset(Calendar.getInstance().getTimeInMillis()) - fromTimeZone.getOffset(Calendar.getInstance().getTimeInMillis());
    }


    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isICSandAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static  boolean isDeviceFitForExoplayer()
    {
        if(Build.MANUFACTURER.toLowerCase().contains("sony")&&(Build.MODEL.equalsIgnoreCase("C2305")))
        {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
        }
        if(Build.MANUFACTURER.equalsIgnoreCase("Micromax") || Build.MANUFACTURER.equalsIgnoreCase("Karbonn") || (Build.MANUFACTURER.equalsIgnoreCase("Xolo"))|| (Build.MANUFACTURER.equalsIgnoreCase("Lava") || Build.MANUFACTURER.equalsIgnoreCase("Coolpad")))
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
        else
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static String getVideoFormat() {
        if (isICSandAbove()) {
            return "mp4";
        } else {
            return "rtsp";
        }
    }


    public static String buildUrl(String url, int pageNumber, String pageSize) {
        url = url.replace("@page", pageNumber + "");
        url = url.replace("@size", pageSize);
        return url;
    }

    public static String setVideoFormat(String url) {
        url = url.replace("@video_format", getVideoFormat());
        return url;
    }

    public static String date4Sql(String strDate) {

        if (TextUtils.isEmpty(strDate)) return "";

        if (strDate != null) {
            try {
                return sqliteDateFormat.format(feedDateFormat.parse(strDate));
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return "";
    }

    public static CharSequence date4App(String date, Context ctx) {
        CharSequence cs;
        if (TextUtils.isEmpty(date)) return "";

        try {
            Long feedTime = (sqliteDateFormat.parse(date)).getTime();
            if ((new Date().getTime() + TIMEZONE_OFFSET - (feedTime)) > DateUtils.MINUTE_IN_MILLIS * ConfigManager.timeStampCap) {
                return "";
            }
            cs =DateUtils.getRelativeTimeSpanString(feedTime, new Date().getTime() + TIMEZONE_OFFSET, DateUtils.MINUTE_IN_MILLIS);
            if((cs.toString().toLowerCase().trim().equalsIgnoreCase("0 minutes ago"))||(cs.toString().toLowerCase().trim().equalsIgnoreCase("in 0 minutes")))
            {
                cs ="10 seconds ago";
            }
            return cs;
        } catch (ParseException e) {
            LogUtils.LOGD(TAG, "could not parse date");
            e.printStackTrace();
        }
        return "";


    }

    public static String date4Sql(Date date) {
        return sqliteDateFormat.format(date);
    }

    public static boolean isInverseSet(Context mContext) {
        String NOTIFICATION_INVERSE = "notification_inverse";
        PreferencesManager prefManager = PreferencesManager.getInstance(mContext.getApplicationContext());
        //if (prefManager.getNotificationSettings(NOTIFICATION_INVERSE))
        if (prefManager.getInverseNotificationSettings(NOTIFICATION_INVERSE))
            return true;
        return false;
    }
    public static boolean isAppRefreshEnabled(Context mContext) {
        String APP_REFRESH_ENABLE="app_refresh_enable";
        PreferencesManager prefManager = PreferencesManager.getInstance(mContext.getApplicationContext());
        if (prefManager.getNotificationSettings(APP_REFRESH_ENABLE))
            return true;
        return false;
    }

}
