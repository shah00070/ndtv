package com.ndtv.core.common.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ndtv.core.R;
import com.ndtv.core.config.model.Photos;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.gcm.GcmUtility;
import com.ndtv.core.livetv.dto.Program;
import com.ndtv.core.share.ShareItem;
import com.ndtv.core.util.Encrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by sangeetha on 30/1/15.
 */
public class Utility {

    public static final int ANIMATION_FADE_IN_TIME = 250;
    //    private static String PAGE_SIZE = "";
//    private static String PAGE_NUM = "";
    private static final String DEFAULT_PAGE_SIZE = "10";
    private static final String DEFAULT_PAGE_NUM = "1";

    public static boolean isInternetOn(Context context) {
        if (null != context) {
            ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null != conn) {
                NetworkInfo networkInfo = conn.getActiveNetworkInfo();
                if (networkInfo != null)
                    return networkInfo.isConnected();
            }
        }
        return false;
    }

    public static String decodeString(String encoded) {
        if (TextUtils.isEmpty(encoded)) {
            return null;
        }
        try {
            return new String(encoded.getBytes(Charset.forName("ISO-8859-1")), "UTF-8")/*
                                                                                         * new
																						 * String
																						 * (
																						 * encoded
																						 * .
																						 * getBytes
																						 * (
																						 * "ISO8859-9"
																						 * )
																						 * ,
																						 * HTTP
																						 * .
																						 * UTF_8
																						 * )
																						 */;
        } catch (Exception e) {
            e.printStackTrace();
            return encoded;
        }
    }

    public static String getNewsDigestFinalCount(String countString, int currectCount) {
        try {
            int count = Integer.parseInt(countString);
            return String.valueOf(count + currectCount);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return countString;
        }
    }

    public static String getFormattedLoopCount(Long loopCount) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String formattedLoopCount = formatter.format(loopCount);
        return formattedLoopCount;
    }

    @SuppressLint("NewApi")
    public static int getScreenWidth(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size.x;
        } else {
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            return display.getWidth();
        }
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        int height = display.heightPixels;
        return height;
    }

    public static String getExtraSubjectForTwitter(ShareItem item, Context ctx) {
        if (!TextUtils.isEmpty(item.title) && !TextUtils.isEmpty(item.link)) {
            return Html.fromHtml(
                    MessageFormat.format("{0}<br\">{1}<br\"><a href=\"{2}\">{3}</a>", item.title,
                            item.desc, item.link, item.link)).toString();
        } else if (!TextUtils.isEmpty(item.desc)) {
            return item.desc;
        }
        return "";
    }

    public static String getExtraSubjectNoDesc(ShareItem item, Context ctx) {
        if (!TextUtils.isEmpty(item.title) && !TextUtils.isEmpty(item.link)) {
            return Html.fromHtml(
                    MessageFormat.format("{0}<br\"><br\">{1}<br\"><a href=\"{2}\">{3}</a><br\"><br\">{4}<br\">{5}<br\">{6}", item.title,
                            item.desc, item.link, item.link, ctx.getString(R.string.minus), ctx.getString(R.string.share_msg),
                            ctx.getString(R.string.minus))).toString();
        } else if (!TextUtils.isEmpty(item.desc)) {
            return item.desc;
        }
        return "";
    }

    /**
     * @returns URL with launguage_id,pageNum,size and extra params.
     */
    public static String getFinalUrl(String oldStrings[], String newStrings[], String url, Context ctx) {
        String newUrl = getFinalUrl(url, ctx);
        int size = oldStrings.length;
        if (size == newStrings.length) {
            for (int i = 0; i < size; i++) {
                if (oldStrings[i] != null && newStrings[i] != null) {
                    if (newUrl.contains(oldStrings[i])) {
                        newUrl = newUrl.replace(oldStrings[i], newStrings[i]);
                    }
                }
            }
        }
        return getEncodedUrl(newUrl);
    }

    /**
     * @returns URL with launguage_id,pageNum,size and extra params.
     */

    public static String getFinalUrl(String oldStrings[], String newStrings[], String url, Context ctx, String pageNum) {
        String newUrl = getFinalUrl(url, ctx, pageNum);
        int size = oldStrings.length;
        if (size == newStrings.length) {
            for (int i = 0; i < size; i++) {
                if (oldStrings[i] != null && newStrings[i] != null) {
                    if (newUrl.contains(oldStrings[i])) {
                        newUrl = newUrl.replace(oldStrings[i], newStrings[i]);
                    }
                }
            }
        }
        return getEncodedUrl(newUrl);
    }

    /**
     * @returns URL with launguage_id,pageNum and size
     */
    public static String getFinalUrl(String url, Context context) {
        String keys[] = new String[]{ApplicationConstants.UrlKeys.PAGE_NUMBER, ApplicationConstants.UrlKeys.PAGE_SIZE};
        String values[] = new String[]{DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE};
        String finalUrl = url;
        int size = keys.length;
        if (null != finalUrl) {
            if (size > 0 && size == values.length) {
                for (int i = 0; i < size; i++) {
                    if (finalUrl.contains(keys[i])) {
                        finalUrl = finalUrl.replace(keys[i], values[i]);
                    }
                }
            }
            return getEncodedUrl(finalUrl);
        } else {
            return "";
        }

    }

    /**
     * @returns URL with launguage_id,pageNum and size
     */
    public static String getFinalUrl(String url, Context context, final String pageNum) {
        String keys[] = new String[]{ApplicationConstants.UrlKeys.PAGE_NUMBER, ApplicationConstants.UrlKeys.PAGE_SIZE,
                ApplicationConstants.UrlKeys.VIDEO_FORMAT};
        String values[] = new String[]{pageNum, DEFAULT_PAGE_SIZE, getVideoFormat()};
        String finalUrl = url;
        int size = keys.length;
        if (null != finalUrl) {
            if (size > 0 && size == values.length) {
                for (int i = 0; i < size; i++) {
                    if (finalUrl.contains(keys[i])) {
                        finalUrl = finalUrl.replace(keys[i], values[i]);
                    }
                }
            }
            return getEncodedUrl(finalUrl);
        } else {
            return "";
        }
    }

    public static String getEncodedUrl(String url) {
        try {
            URL urlTemp = new URL(url);
            URI uri = new URI(urlTemp.getProtocol(), urlTemp.getUserInfo(), urlTemp.getHost(), urlTemp.getPort(),
                    urlTemp.getPath(), urlTemp.getQuery(), urlTemp.getRef());
            urlTemp = uri.toURL();
            url = urlTemp.toString();
            url = getVideoEncodedUrl(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getVideoEncodedUrl(String url) {
        if (!TextUtils.isEmpty(url) && url.contains(ApplicationConstants.UrlKeys.VIDEO_FORMAT))
            return url.replace(ApplicationConstants.UrlKeys.VIDEO_FORMAT, getVideoFormat());
        return url;
    }

    public static String getVideoFormat() {
        return "mp4";
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void showSoftKeyBoard(final EditText view) {
        (new Handler()).postDelayed(new Runnable() {

            public void run() {
                view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN, 0, 0, 0));
                view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP, 0, 0, 0));
            }
        }, 200);
    }

    public static String getExtraSubject(ShareItem item, Context ctx) {
        if (!TextUtils.isEmpty(item.title) && !TextUtils.isEmpty(item.link)) {
            return Html.fromHtml(
                    MessageFormat.format("<a href=\"{0}\">{1}</a><br\"><br\">{2}<br\">{3}<br\">{4}", item.link,
                            item.link, ctx.getString(R.string.minus), ctx.getString(R.string.share_msg),
                            ctx.getString(R.string.minus))).toString();
        } else if (!TextUtils.isEmpty(item.desc)) {
            return item.desc;
        }
        return "";
    }

    public static boolean isJellyBeanAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isLollypopAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isBelowJellyBean() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN;
    }


    /*LIVE TV AND RADIO*/
    public static Date getProgramTime(Program program) {
        Date showDate = null;
        if (!TextUtils.isEmpty(program.timestamp)) {
            Calendar deviceCalendar = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            try {
                showDate = format.parse(program.timestamp);

                Calendar showCalendar = Calendar.getInstance();
                showCalendar.setTime(showDate);
                showCalendar.set(Calendar.YEAR, deviceCalendar.get(Calendar.YEAR));
                showCalendar.set(Calendar.MONTH, deviceCalendar.get(Calendar.MONTH));
                showCalendar.set(Calendar.DATE, deviceCalendar.get(Calendar.DATE));

               /* if (program.isNextDay()) {
                    showCalendar.set(Calendar.DATE, deviceCalendar.get(Calendar.DATE) + 1);
                } else {
                    showCalendar.set(Calendar.DATE, deviceCalendar.get(Calendar.DATE));
                }
*/
                showDate = showCalendar.getTime();


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return showDate;
    }

    /*LIVE TV AND RADIO END*/
//GCM
    public static String getString(InputStream inStream) throws IOException {
        StringBuilder builder = new StringBuilder();/* 0x10000 */
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream), 8192);
        String line;
        while ((line = buffReader.readLine()) != null)
            builder.append(line);
        return builder.toString();
    }


    public static void CancelNotification(Context ctx) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancelAll();
    }

    public static void cancelNotification(int id, Context ctx) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(id);
    }


    public static int getTimeZoneOffset() {
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getRawOffset() / 1000;
    }

    public static TimeZone getCurrentTimeZone() {
        return TimeZone.getDefault();
    }

    public static int getDeviceDensity(Context ctx) {
        return ctx.getResources().getDisplayMetrics().densityDpi;
    }

    public static String getSplashAdKey(Context ctx) {
        String key = "";
        switch (getDeviceDensity(ctx)) {
            case DisplayMetrics.DENSITY_LOW:
                key = ApplicationConstants.CustomApiType.ADS_IMAGE_LDPI;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                key = ApplicationConstants.CustomApiType.ADS_IMAGE_MDPI;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                key = ApplicationConstants.CustomApiType.ADS_IMAGE_HDPI;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                key = ApplicationConstants.CustomApiType.ADS_IMAGE_XHDPI;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                key = ApplicationConstants.CustomApiType.ADS_IMAGE_XXHDPI;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                key = ApplicationConstants.CustomApiType.ADS_IMAGE_XXXHDPI;
                break;
            default:
                key = ApplicationConstants.CustomApiType.ADS_IMAGE_HDPI;
                break;
        }
        return key;
    }

    public static String getFeedbackExtraSubject(Context ctx, String feedback) {

        String manufacturername = getManufacturerName();
        manufacturername = manufacturername.substring(0, 1).toUpperCase() + manufacturername.substring(1);

        return MessageFormat.format("{0}{1}{2} {3} {4}{5}{6} {7}{8}{9} {10}{11}{12} {13}{14}{15} {16}{17}{18}{19}{20}{21}", feedback, ctx.getString(R.string.new_line),
                ctx.getString(R.string.device_info), manufacturername, getModel(), "\n", ctx.getString(R.string.network_info),
                getNetworkType(ctx), "\n", ctx.getString(R.string.device_os), getOsVersion(), "\n",
                ctx.getString(R.string.app_version), getApplicationVersion(ctx), "\n", ctx.getString(R.string.network_name),
                getNetworkProviderName(ctx), "\n", ctx.getString(R.string.devicetoken_info), Encrypt.encrypt(GcmUtility.getRegistrationId(ctx)), "\n", getExtraSubject(ctx));

    }

    private static String getExtraSubject(Context ctx) {
        String manufacturername = getManufacturerName();
        manufacturername = manufacturername.substring(0, 1).toUpperCase() + manufacturername.substring(1);
        return MessageFormat.format("{0}{1} {2} {3}{4}{5} {6} {7}", "----\n", ctx.getString(R.string.sent_via), ctx.getString(R.string.app_name), "Android App\n----",
                ctx.getString(R.string.new_line), ctx.getString(R.string.sent_from), manufacturername, getModel());
    }

    /**
     * @param ctx
     * @return App version.
     */
    public static String getApplicationVersion(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * @return OS version.
     */
    private static String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * @return device model
     */
    private static String getModel() {
        return android.os.Build.MODEL;
    }

    /**
     * @return device Manufacturer Name
     */
    private static String getManufacturerName() {
        return android.os.Build.MANUFACTURER;
    }


    private static String getNetworkType(Context context) {
        NetworkInfo info = getNetWorkInfo(context);
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                return context.getString(R.string.wifi);
            else if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                return context.getString(R.string.mobile);
        }
        return "-";  //not connected
    }

    private static NetworkInfo getNetWorkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info;
    }

    private static String getNetworkProviderName(Context context) {
        String provider = "-";
        NetworkInfo info = getNetWorkInfo(context);
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                provider = context.getString(R.string.wifi_carrier);
            else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                provider = manager.getNetworkOperatorName();
                provider = provider.substring(0, 1).toUpperCase() + provider.substring(1);
            }
        }
        return provider;
    }

    public void mapAlbumPhotos(List<Photos> photoList) {
        HashMap<String, String> photoIdMap = new HashMap<String, String>();

        for (int i = 0; i < photoList.size(); i++) {
            photoIdMap.put("PH" + i, photoList.get(i).getId());
        }
    }

    public String getPhotoId(String key) {
        //for ()
        return null;
    }

    public static LayoutAnimationController getListanimation() {
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(10);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(70);
        set.addAnimation(animation);

        return new LayoutAnimationController(set, 0.5f);
    }

    public static String getVersionName(Context context) {

        try {
            return context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public static int getSizeBasedOnDensity(Context ctx) {
        int size;
        switch (getDeviceDensity(ctx)) {

            case DisplayMetrics.DENSITY_MEDIUM:
                size = ApplicationConstants.GCMKeys.ICON_MDPI_SIZE;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                size = ApplicationConstants.GCMKeys.ICON_HDPI_SIZE;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                size = ApplicationConstants.GCMKeys.ICON_XHDPI_SIZE;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                size = ApplicationConstants.GCMKeys.ICON_XXHDPI_SIZE;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                size = ApplicationConstants.GCMKeys.ICON_XXXHDPI_SIZE;
                break;
            default:
                size = ApplicationConstants.GCMKeys.ICON_HDPI_SIZE;
                break;
        }
        return size;
    }


    public static Bitmap getResizedBitmap(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static String createDownSizeImgUrl(String url, float width, float height) {
        return url + "?downsize=" + width + ":" + height;
    }


}
