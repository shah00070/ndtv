/**
 * Project      : Awaaz
 * Filename     : GcmUtility.java
 * Author       : anudeep
 * Comments     :
 * Copyright    : © Copyright NDTV Convergence Limited 2011
 * Developed under contract by Robosoft Technologies
 * History      : NA
 */

package com.ndtv.core.gcm;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.ndtv.core.R;
import com.ndtv.core.common.util.AsyncTask;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.views.AvatarDrawable;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.constants.ApplicationConstants.BuildType;
import com.ndtv.core.constants.ApplicationConstants.BundleKeys;
import com.ndtv.core.constants.ApplicationConstants.PreferenceKeys;
import com.ndtv.core.now.NowClient;
import com.ndtv.core.ui.SplashActivity;
import com.ndtv.core.util.ApplicationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author anudeep
 */
public class GcmUtility implements PreferenceKeys, BuildType {
    private static final String TAG = "GCM";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static final float DEFAULT_ASPECT_RATIO = 1.33F;
    String SENDER_ID = ServerUtilities.SENDER_ID;
    public static String regid = "";

    private static Integer NOTIFICATION_TEXT_COLOR = null;

    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Context context, Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                if (activity != null && !activity.isFinishing())
                    GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                return false;
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service, if there
     * is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        Log.d("Token", registrationId);
        if (TextUtils.isEmpty(registrationId)) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";

        }
        return registrationId;

    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(GcmUtility.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    public static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion + "reg Id is : " + regId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use
     * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
     * since the device sends upstream messages to a server that echoes back the
     * message using the 'from' address in the message.
     */
    public static void sendRegistrationIdToBackend(Context context) {
        ServerUtilities.register(context, getRegistrationId(context));
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    public static void registerInBackground(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    String Buildflavor = context.getPackageName();
                    if (Buildflavor.equalsIgnoreCase(NDTVNEWS)) {
                        InstanceID instanceID = InstanceID.getInstance(context);
                        regid = instanceID.getToken(ServerUtilities.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                        //GCM register() has been deprecated, and it is recommended to use InstanceID to perform general GCM registration management.
                        //regid = GoogleCloudMessaging.getInstance(context).register(ServerUtilities.SENDER_ID);
                    } else if (Buildflavor.equalsIgnoreCase(NDTVINDIA)) {
                        InstanceID instanceID = InstanceID.getInstance(context);
                        regid = instanceID.getToken(ServerUtilities.SENDER_ID_HINDI, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                        // regid = GoogleCloudMessaging.getInstance(context).register(ServerUtilities.SENDER_ID_HINDI);
                    } else if (Buildflavor.equalsIgnoreCase(NDTVPRIME)) {
                        InstanceID instanceID = InstanceID.getInstance(context);
                        regid = instanceID.getToken(ServerUtilities.SENDER_ID_PRIME, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                        //regid = GoogleCloudMessaging.getInstance(context).register(ServerUtilities.SENDER_ID_PRIME);
                    }

                    msg = "Device registered, registration ID=" + regid;
                    if (!TextUtils.isEmpty(regid)) {
                        GcmUtility.storeRegistrationId(context, regid);
                        PreferencesManager.getInstance(context).setPushStatus(true);
                    }
                    // You should send the registration ID to your server over
                    // HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    GcmUtility.sendRegistrationIdToBackend(context);

                    // For this demo: we don't need to send it because the
                    // device will send
                    // upstream messages to a server that echo back the message
                    // using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.

                    NowClient nowClient = NowClient.getInstance(context);
                    nowClient.init(regid);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d(TAG, msg);//Sri189
//                 PreferencesManager.getInstance(context.getApplicationContext()).setPushStatus(true);
//                 mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    public static void unregisteInBackGround(final Context context, final String regId) {

        if (Utility.isInternetOn(context)) {

            new AsyncTask<Void, Void, Void>() {
                /*
                 * (non-Javadoc)
                 *
                 * @see
                 * com.july.ndtv.common.util.AsyncTask#doInBackground(Params[])
                 */
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        GoogleCloudMessaging.getInstance(context).unregister();


                        ServerUtilities.unregister(context, regId);
                        PreferencesManager.getInstance(context).setPushStatus(false);
                        GcmUtility.storeRegistrationId(context, "");
                        Log.d(TAG, "unregistered");
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                    return null;
                }

                /*
                 * (non-Javadoc)
                 *
                 * @see
                 * com.july.ndtv.common.util.AsyncTask#onPostExecute(java.lang
                 * .Object )
                 */
                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    // Toast.
                }
            }.execute();
        }
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void generateNotification(Context context, String message, String nav) {
        int icon = R.drawable.ic_ndtv_notfctn;
        long when = System.currentTimeMillis();
        //**add this line for multiple payloads**
        final int requestID = (int) System.currentTimeMillis();
        final int notificationId = ((int) System.currentTimeMillis() % 1000000);


        // Send Broadcast if App receives a Notification do display breaking
        // news Pop - Up
        sendBreakingNewsBroadcast(context, message, notificationId);

        if (Utility.isBelowJellyBean()) {
            //generate collapsed notification for ICS devices
            createNotifiationForICS(context, message, icon, when, nav, notificationId, requestID);
            Log.d("GCM_start", requestID + "" + notificationId + "" + message);
        } else {
            createNotificationForJBAndAbove(context, message, icon, when, nav, notificationId, requestID);
            Log.d("GCM_start", requestID + "" + notificationId + "" + message);
        }


    }

    public static void generateImageNotification(Context context, String message, String nav, Bitmap bitmap, String imageUrl) {
        int icon = R.drawable.ic_ndtv_notfctn;
        long when = System.currentTimeMillis();
        //**add this line for multiple payloads**
        final int requestID = (int) System.currentTimeMillis();

        final int notificationId = ((int) System.currentTimeMillis() % 1000000);

        // Send Broadcast if App receives a Notification do display breaking
        // news Pop - Up
        sendBreakingNewsBroadcast(context, message, notificationId);

        if (Utility.isBelowJellyBean()) {
            //generate collapsed notification for ICS devices
            createNotifiationForICS(context, message, icon, when, nav, notificationId, requestID);
            Log.d("GCM_start", requestID + "" + notificationId + "" + message);
        } else {
            createPictureNotificatnForJBAndAbove(context, message, bitmap, icon, when, nav, notificationId, requestID, imageUrl);
            Log.d("GCM_start", requestID + "" + notificationId + "" + message);
        }


    }

    //Registering the share icon of notification tray
    private static void setBtnListeners(final RemoteViews notificationView, int requestId, String message, Context context, int notificationId) {
        Intent shareBtnIntent = new Intent(context, GcmTransparentActivity.class);
        shareBtnIntent.putExtra(ApplicationConstants.GCMKeys.BREAKING_NEWS, message);
        shareBtnIntent.putExtra(ApplicationConstants.GCMKeys.GCM_NOTIFICATION_ID, notificationId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestId, shareBtnIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.share, pendingIntent);
        Log.d("GCM_start", requestId + "" + notificationId + "" + message);
    }


    private static void sendBreakingNewsBroadcast(Context context, String message, int notificationId) {

        String Buildflavor = context.getPackageName();

        if (Buildflavor.equalsIgnoreCase("com.ndtv.india")) {
            Intent intent = new Intent(BREAKING_NEWS_HINDI);
            intent.putExtra(MESSAGE, message);
            intent.putExtra(ApplicationConstants.GCMKeys.GCM_NOTIFICATION_ID, notificationId);
            context.sendBroadcast(intent);
        } else if (Buildflavor.equalsIgnoreCase("com.mm.ndtv")) {
            Intent intent = new Intent(BREAKING_NEWS_PRIME);
            intent.putExtra(MESSAGE, message);
            intent.putExtra(ApplicationConstants.GCMKeys.GCM_NOTIFICATION_ID, notificationId);
            context.sendBroadcast(intent);

        } else {
            Intent intent = new Intent(BREAKING_NEWS);
            intent.putExtra(MESSAGE, message);
            intent.putExtra(ApplicationConstants.GCMKeys.GCM_NOTIFICATION_ID, notificationId);
            context.sendBroadcast(intent);
        }
    }

    private static void sendPendeningInent(Context context, int requestID, int notificationId, String nav, Notification notification, NotificationManager notificationManager) {

        Intent notificationIntent;
        notificationIntent = new Intent(context, SplashActivity.class);//Sritapana189
        notificationIntent.putExtra(BundleKeys.NORMAL_PUSH, 1);
        notificationIntent.putExtra(BundleKeys.DEFAULT_NOTI_NAV, nav);
        notificationIntent.putExtra(BundleKeys.FROM_GCM, true);
        notificationIntent.putExtra(ApplicationConstants.GCMKeys.GCM_NOTIFICATION_ID, notificationId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);


        PendingIntent contentIntent = PendingIntent.getActivity(context, requestID, notificationIntent, 0); //Modified
        notification.contentIntent = contentIntent;
        if (PreferencesManager.getInstance(context.getApplicationContext()).getNotificationSettings(NOTIFICATION_SOUND)) {
            // notification.setLatestEventInfo(context, title, message, intent);
            // notification.defaults |= Notification.DEFAULT_SOUND;
            notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.ndtv);
            // notification.defaults |= Notification.DEFAULT_LIGHTS;
            // notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (PreferencesManager.getInstance(context.getApplicationContext()).getNotificationSettings(
                NOTIFICATION_VIBRATION)) {
            // notification.setLatestEventInfo(context, title, message, intent);
            // notification.defaults |= Notification.DEFAULT_SOUND;
            // notification.defaults |= Notification.DEFAULT_LIGHTS;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(notificationId, notification);
    }


    private static void createNotifiationForICS(Context context, String message, int icon, long when, String nav, int notificationId, int requestID) {
        final RemoteViews collapsedView;
        Notification notification;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        collapsedView = generateCollapsedView(context, message);
        setNotificationTextColor(collapsedView);
        notification = buildNotificationForICS(context, message, icon, when);
        notification.contentView = collapsedView;
        sendPendeningInent(context, requestID, notificationId, nav, notification, notificationManager);
        Log.d("GCM", requestID + "" + notificationId + "" + message);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void createNotificationForJBAndAbove(Context context, String message, int icon, long when, String nav, int notificationId, int requestID) {
        final RemoteViews expandedView, collapsedView;
        Notification notification;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        expandedView = generateExpandedView(context, message, when, notificationId, requestID);
        collapsedView = generateCollapsedView(context, message);
        NotificationCompat.Builder builder = buildNotificationForJBAndAbove(context, message, icon, when);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setPriority(Notification.PRIORITY_MAX);
        notification = builder.build();
        notification.contentView = collapsedView;
        notification.bigContentView = expandedView;
        sendPendeningInent(context, requestID, notificationId, nav, notification, notificationManager);
        Log.d("GCM", requestID + "" + notificationId + "" + message);


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void createPictureNotificatnForJBAndAbove(Context context, String message, Bitmap bitmap, int icon, long when, String nav, int notificationId, int requestID, String imageUrl) {
        final RemoteViews pictureView, collapsedView;
        Notification notification;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        pictureView = generatePictureView(context, message, when, notificationId, requestID, bitmap);
        collapsedView = generateCollapsedView(context, message);
        NotificationCompat.Builder builder = buildNotificationForJBAndAbove(context, message, icon, when);
        builder.setStyle(new NotificationCompat.BigPictureStyle());
        builder.setPriority(Notification.PRIORITY_MAX);
        notification = builder.build();
        notification.contentView = collapsedView;
        notification.bigContentView = pictureView;
        sendPendeningInent(context, requestID, notificationId, nav, notification, notificationManager);
        Log.d("GCM", requestID + "" + notificationId + "" + message);
    }

    private static RemoteViews generateExpandedView(Context context, String message, long when, int notificationId, int requestID) {
        final RemoteViews expandedView;
        if (ApplicationUtils.isInverseSet(context)) {
            expandedView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout_inv);
        } else {
            expandedView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
        }

        setDataForJBAndAbove(context, message, expandedView, when);
        setShareBtns(context, expandedView);
        setNotificationTextColor(expandedView);
        setBtnListeners(expandedView, requestID, message, context, notificationId);
        return expandedView;

    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static RemoteViews generatePictureView(Context context, String message, long when, int notificationId, int requestID, Bitmap bitmap) {
        final RemoteViews pictureView;
        if (ApplicationUtils.isInverseSet(context)) {
            pictureView = new RemoteViews(context.getPackageName(), R.layout.picture_style_notification_inv);
        } else {
            pictureView = new RemoteViews(context.getPackageName(), R.layout.picture_style_notification);
        }
        setDataForJBAndAbove(context, message, pictureView, when);
//        Bitmap scaledBitmap = getScaledBitmap(context, bitmap);
        pictureView.setImageViewBitmap(R.id.picture_view, bitmap);

        pictureView.setTextViewCompoundDrawables(R.id.share, R.drawable.w_gcm_share, 0, 0, 0);

        setNotificationTextColor(pictureView);

        setBtnListeners(pictureView, requestID, message, context, notificationId);

        return pictureView;

    }

    private static RemoteViews generateCollapsedView(Context context, String message) {
        final RemoteViews collapsedView;
        if (ApplicationUtils.isInverseSet(context)) {
            collapsedView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_ics_inv);
        } else {
            collapsedView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_ics_layout);
        }
        if (Utility.isLollypopAndAbove()) {
            int iconSize = Utility.getSizeBasedOnDensity(context);
            Bitmap roundedIcon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_launcher);
            collapsedView.setImageViewBitmap(R.id.image_logo, Utility.drawableToBitmap(new AvatarDrawable(Utility.getResizedBitmap(roundedIcon, iconSize, iconSize))));
        } else {
            collapsedView.setImageViewResource(R.id.image_logo, R.drawable.ic_launcher);
        }
        collapsedView.setTextViewText(R.id.text, message);
        return collapsedView;
    }

    private static void setDataForJBAndAbove(Context context, String message, final RemoteViews expandedView, long when) {
        //create a rounded icon for lollypop devices
        if (Utility.isLollypopAndAbove()) {
            int iconSize = Utility.getSizeBasedOnDensity(context);
            Bitmap roundedIcon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_launcher);
            expandedView.setImageViewBitmap(R.id.image_logo, Utility.drawableToBitmap(new AvatarDrawable(Utility.getResizedBitmap(roundedIcon, iconSize, iconSize))));

        } else {
            expandedView.setImageViewResource(R.id.image_logo, R.drawable.ic_launcher);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        String time = dateFormat.format(new Date(when));
        expandedView.setTextViewText(R.id.current_time, time);
        expandedView.setTextViewText(R.id.title, context.getResources().getString(R.string.app_name));
        expandedView.setTextViewText(R.id.text, message);


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setShareBtns(Context context, final RemoteViews expandedView) {
        if (!Utility.isLollypopAndAbove()) {
            if (ApplicationUtils.isInverseSet(context)) {
                expandedView.setTextViewCompoundDrawables(R.id.share, R.drawable.b_gcm_share, 0, 0, 0);
            } else {
                expandedView.setTextViewCompoundDrawables(R.id.share, R.drawable.w_gcm_share, 0, 0, 0);
            }
        } else {
            if (ApplicationUtils.isInverseSet(context)) {
                expandedView.setTextViewCompoundDrawables(R.id.share, R.drawable.w_gcm_share, 0, 0, 0);
            } else {
                expandedView.setTextViewCompoundDrawables(R.id.share, R.drawable.b_gcm_share, 0, 0, 0);
            }


        }
    }

    private static Notification buildNotificationForICS(Context context, String message, int icon, long when) {
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context);
        mNotifyBuilder
                .setSmallIcon(icon)
                .setWhen(when)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(message);
        return mNotifyBuilder.build();
    }

    private static NotificationCompat.Builder buildNotificationForJBAndAbove(Context context, String message, int icon, long when) {
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context);
        Bitmap largeIcon = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_launcher)).getBitmap();
        mNotifyBuilder
                .setWhen(when)
                .setSmallIcon(icon)
                .setLargeIcon(largeIcon)
                .setWhen(when)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(message);

        return mNotifyBuilder;
    }


    private static void setNotificationTextColor(RemoteViews remoteView) {
        if (NOTIFICATION_TEXT_COLOR != null) {
            // if color is fetched and is not null then set color,
            // otherwise takes the default color. fix after 4.03
            remoteView.setTextColor(R.id.text, NOTIFICATION_TEXT_COLOR);

        }
    }

    private static Bitmap getScaledBitmap(Context context, Bitmap bitmap) {
        int screenWidth = Utility.getScreenWidth(context);
        //Calculate height based on aspect ratio
        int height = (int) ((float) screenWidth / DEFAULT_ASPECT_RATIO);
        return Bitmap.createScaledBitmap(bitmap, screenWidth, height, true);
    }


    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
