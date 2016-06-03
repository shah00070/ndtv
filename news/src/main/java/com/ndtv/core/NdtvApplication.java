package com.ndtv.core;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.crashlytics.android.Crashlytics;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.util.ImageLoader;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.radio.services.LiveRadioService;
import com.ndtv.core.radio.services.LiveRadioServiceNewApi;
import com.ndtv.core.video.ui.CastPreference;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.comscore.analytics.comScore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Srihari S Reddy on 30/12/14.
 */
public class NdtvApplication extends Application {

    public final static String CRASHLYTICS_KEY_CRASHES = "are_crashes_enabled";
    public RequestQueue mRequestQueue;
    private static NdtvApplication appInstance;
    private ExecutorService mSharedPreferanceExecutor, mMultiThreadExecutor, mDatabaseThreadExecutor;
    static final float EXECUTOR_POOL_SIZE_PER_CORE = 1.5f;
    private static VideoCastManager mVideoCastMgr = null;
    private static DataCastManager mDataCastMgr = null;
    private com.ndtv.core.common.util.util.ImageLoader mImageLoader;
    private static String APPLICATION_ID;
    private String CONSUMER_KEY;
    private String CONSUMER_SECRET;
    private String COMSCORE_CUSTOMERC2;
    private String COMSCORE_PUBLISHER_SECRET;
    private boolean triggerRefresh=false;

    // uncaught exception handler variable
    private Thread.UncaughtExceptionHandler mdefaultUEH;
    private final int RADIO_NOTIFICATION_ID = 1;

    public static NdtvApplication getInstance() {
        return appInstance;
    }

    public static NdtvApplication getApplication(Context context) {
        return (NdtvApplication) context.getApplicationContext();
    }

    public static NdtvApplication getApplication() {
        return appInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        CONSUMER_KEY = getString(R.string.twitter_consumer_key);
        CONSUMER_SECRET = getString(R.string.twitter_consumer_secret);



//        Fabric.with(this, new Crashlytics());
        //Including TwitterCore kit
        TwitterAuthConfig authConfig = new TwitterAuthConfig(CONSUMER_KEY, CONSUMER_SECRET);
        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));
        // TODO set crash sharing on user preferences/external file
        Crashlytics.setBool(CRASHLYTICS_KEY_CRASHES, true);
        createRequestQueue();
        APPLICATION_ID = getString(R.string.cc_app_id);
        createVideoCastManager();
        mImageLoader = new com.ndtv.core.common.util.util.ImageLoader(this);
        mImageLoader.setFadeInImage(true);
        mdefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        //set up handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        //set the LiveRadioPlayStatus to false, when app is killed by the system due to memory issue
        if (PreferencesManager.getInstance(getApplicationContext()) != null) {
            PreferencesManager.getInstance(getApplicationContext()).setLiveRadioPlayStatus(false);
            PreferencesManager.getInstance(this).setLiveRadioStopped(false);
        }

        COMSCORE_CUSTOMERC2 = getString(R.string.com_score_account_id);
        COMSCORE_PUBLISHER_SECRET = getString(R.string.comscore_publisher_secret);

        // Initialize comScore Application Tag library
        comScore.setAppContext(this.getApplicationContext());
         // Include any of the comScore Application Tag library initializ
        comScore.setCustomerC2(COMSCORE_CUSTOMERC2);
        comScore.setPublisherSecret(COMSCORE_PUBLISHER_SECRET);

    }


    //handler listener
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            //to clear radio notification & stop service if playing,when app is crashed.
            if (isServiceRunning(LiveRadioService.class.getName()) || isServiceRunning(LiveRadioServiceNewApi.class.getName())) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(RADIO_NOTIFICATION_ID);
//                stopRadioService();
            }
            //Manualy send the crash log to the crashlytics
            Log.e("Crash", "UncaughtException", ex);
            mdefaultUEH.uncaughtException(thread, ex);
            //throws the exception & shows crash dialog
            Crashlytics.logException(ex);


        }
    };

    public void createRequestQueue() {
        mRequestQueue = VolleyRequestQueue.getInstance(this).getRequestQueue();
    }

    public ExecutorService getMultiThreadExecutorService() {
        if (null == mMultiThreadExecutor || mMultiThreadExecutor.isShutdown()) {
            final int numThreads = Math.round(Runtime.getRuntime().availableProcessors() * EXECUTOR_POOL_SIZE_PER_CORE);
            mMultiThreadExecutor = Executors.newFixedThreadPool(numThreads);
            //Log.d(LOG_TAG, "MultiThreadExecutor created with " + numThreads + " threads");
        }
        return mMultiThreadExecutor;
    }

    public ExecutorService getPreferenceExecutor() {
        if (null == mSharedPreferanceExecutor || mSharedPreferanceExecutor.isShutdown()) {
            mSharedPreferanceExecutor = Executors.newSingleThreadExecutor();
        }
        return mSharedPreferanceExecutor;
    }

    public static VideoCastManager getVideoCastManager(Context context) {

        if (null == mVideoCastMgr) {
            mVideoCastMgr = VideoCastManager.initialize(context, APPLICATION_ID, null, null);
            mVideoCastMgr.enableFeatures(
                    VideoCastManager.FEATURE_NOTIFICATION |
                            VideoCastManager.FEATURE_WIFI_RECONNECT |
                            VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                            VideoCastManager.FEATURE_DEBUGGING);

        }
        // mVideoCastMgr.setContext(context);

        String destroyOnExitStr = Utils.getStringFromPreference(context,
                CastPreference.TERMINATION_POLICY_KEY);
        mVideoCastMgr.setStopOnDisconnect(null != destroyOnExitStr
                && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));

        return mVideoCastMgr;
    }


    private void createVideoCastManager() {
        mVideoCastMgr = VideoCastManager.initialize(getApplicationContext(), APPLICATION_ID, null, null);
        mVideoCastMgr.enableFeatures(
                VideoCastManager.FEATURE_NOTIFICATION |
                        VideoCastManager.FEATURE_LOCKSCREEN |
                        VideoCastManager.FEATURE_WIFI_RECONNECT |
                        VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                        VideoCastManager.FEATURE_DEBUGGING);
        String destroyOnExitStr = Utils.getStringFromPreference(getApplicationContext(),
                PreferencesManager.TERMINATION_POLICY_KEY);
        mVideoCastMgr.setStopOnDisconnect(null != destroyOnExitStr
                && PreferencesManager.STOP_ON_DISCONNECT.equals(destroyOnExitStr));

    }

    public static VideoCastManager getCastManager() {
        if (mVideoCastMgr == null) {
            throw new IllegalStateException("Application has not been started");
        }
        return mVideoCastMgr;
    }

    public static DataCastManager getDataCastManager(Context context) {
        if (null == mDataCastMgr) {
            mDataCastMgr = DataCastManager.initialize(context, APPLICATION_ID, null, null);
            mDataCastMgr.enableFeatures(
                    VideoCastManager.FEATURE_NOTIFICATION |
                            VideoCastManager.FEATURE_LOCKSCREEN |
                            VideoCastManager.FEATURE_WIFI_RECONNECT |
                            VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                            VideoCastManager.FEATURE_DEBUGGING);

        }
        // mDataCastMgr.setContext(context);
//TODO CC
//        String destroyOnExitStr = Utils.getStringFromPreference(context,
//                CastPreference.TERMINATION_POLICY_KEY);
//        mVideoCastMgr.setStopOnDisconnect(null != destroyOnExitStr
//                && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));

        return mDataCastMgr;
    }

    public ImageLoader getmImageLoader() {
        return mImageLoader;
    }

    public ExecutorService getDatabaseThreadExecutorService() {
        if (null == mDatabaseThreadExecutor || mDatabaseThreadExecutor.isShutdown()) {
            mDatabaseThreadExecutor = Executors.newSingleThreadExecutor();
        }
        return mDatabaseThreadExecutor;
    }

    public boolean isServiceRunning(String serviceClassName) {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equalsIgnoreCase(serviceClassName)) {
                return true;
            }
        }
        return false;
    }

    public boolean needsRefresh(){
    return triggerRefresh;
    }
    public void setNeedsRefresh(boolean refresh){
        triggerRefresh=refresh;
    }
}
