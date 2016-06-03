package com.ndtv.core.ui;

/**
 * Created by Srihari S Reddy on 16/12/14.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.ndtv.core.BuildConfig;
import com.ndtv.core.R;
import com.ndtv.core.ads.utility.InterstitialAdHelper;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.TimeUtils;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Configuration;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.constants.ApplicationConstants.BuildType;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.util.ApplicationUtils;
import com.ndtv.core.util.GAHandler;
import com.ndtv.core.util.GsonRequest;
import com.ndtv.core.util.StringUtils;

import java.util.Date;

import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.makeLogTag;


/**
 * Created by Srihari S Reddy on 12/11/14.
 */
public class SplashActivity extends Activity implements BuildType {

    private static final String TAG = makeLogTag(SplashActivity.class);
    private static final String TAG_MSG = "SPLASH";
    String intentNotification = null;
    String payloadFromNotification = null;

    private NetworkImageView mAdImage;
    private ProgressBar mProgressBar;

    private long splashStart = new Date().getTime();
    SharedPreferences mPrefs;
    boolean cacheLaunch = false;
    private boolean mIsFromGcm, mIsAppRefresh;
    private boolean mInterstitialAdFailed = false;
    private int mDownloadCnt = 0;
    private boolean mIsConfigDownloaded;
    private boolean isTimeout = false;
    private static int MAX_HOUR = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        initViews();
        setProgressBarLocation();

        if (isTimeForSplashAd()) {
            showSplashAd();
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mIsFromGcm = extras.getBoolean(ApplicationConstants.BundleKeys.FROM_GCM, false);
                mIsAppRefresh = extras.getBoolean(ApplicationConstants.BundleKeys.APP_REFRESH, false);
            }

            if (!mIsAppRefresh) {
                if (isTimeForInterstitialAd())
                    displayInterstitialAd();
            }

            //launch the splash activity, if it's a root activity or if it is launched from Gcm Notification
            if (isTaskRoot() || mIsFromGcm || mIsAppRefresh) {
                downloadConfig();
            } else {
                //finish the activity , if root acctivity, i.e., BaseActivity is already present
                finish();
            }


        }

    }

    private boolean isTimeForInterstitialAd() {
        Boolean status = false;
        String currentDate = TimeUtils.getCurrentDate();
        Log.d(TAG_MSG, currentDate);
        PreferencesManager prefMgr = PreferencesManager.getInstance(this);
        if (prefMgr != null) {
            if (prefMgr.getSavedDate() != null) {
                if (!prefMgr.getSavedDate().equals(currentDate)) {
                    //Dates are different, so it's new day. Save the date and make the count zero
                    prefMgr.saveCurrentDate(currentDate);
                    prefMgr.setInterstialAdCount(0);
                    Log.d(TAG_MSG, "Dates Different");
                    Log.d(TAG_MSG, "" + prefMgr.getInterstitialAdCount());
                    status = true;
                } else {
                    int currentHour = TimeUtils.getCurrentHour();
                    Log.d(TAG_MSG, "hour: " + currentHour);
                    if (currentHour <= MAX_HOUR && prefMgr.getInterstitialAdCount() < 2) {
                        status = true;
                        Log.d(TAG_MSG, "Dates same,load ad");
                        Log.d(TAG_MSG, "" + prefMgr.getInterstitialAdCount());
                    } else {
                        status = false;
                        Log.d(TAG_MSG, "Dates same,don't load ad");
                        Log.d(TAG_MSG, "" + prefMgr.getInterstitialAdCount());
                    }

                }
            } else {
                Log.d(TAG_MSG, "first launch,load ad");
                prefMgr.saveCurrentDate(currentDate);
                prefMgr.setInterstialAdCount(0);
                status = true;
            }

        }
        return status;
    }


    private boolean isInterstialAdNotAvailable() {
        Boolean status = false;
        String currentDate = TimeUtils.getCurrentDate();
        PreferencesManager prefMgr = PreferencesManager.getInstance(this);
        if (prefMgr != null) {
            if (prefMgr.getSavedDate() != null) {
                if (prefMgr.getSavedDate().equals(currentDate)) {
                    int currentHour = TimeUtils.getCurrentHour();
                    Log.d(TAG_MSG, "hour: " + currentHour);
                    if (currentHour <= MAX_HOUR && prefMgr.getInterstitialAdCount() >= 2 || isTimeout || mIsAppRefresh) {
                        Log.d(TAG_MSG, "Interstial Ad not available");
                        Log.d(TAG_MSG, "Timer" + isTimeout);
                        status = true;
                    }
                }

            }

        }
        return status;
    }

    private void displayInterstitialAd() {
        Log.d(TAG_MSG, "Ad request started"); //    /6499/example/interstitial    /6253334/dfp_example_ad/interstitial
        InterstitialAdHelper.getInstance().initInterstitial(SplashActivity.this, getResources().getString(R.string.interstitial_ad_id), new InterstitialAdHelper.InterstitialAdListener() {
            @Override
            public void onInterstitialAdLoaded() {
                Log.d(TAG_MSG, "Ad Loaded");
                mInterstitialAdFailed = false;

            }

            @Override
            public void onInterstitialAdFailed() {
                Log.d(TAG_MSG, "Ad failed");
                mInterstitialAdFailed = true;
                if (mIsConfigDownloaded) {
                    //decrement the ad counter and save it in the preferences
                    Log.d(TAG_MSG, "Ad Failed,config downloaded");
//                    decrementCounter();
                    handleSplashAds(mAdImage);
                    launchHomeScreen();
                }
            }

            @Override
            public void onInterstitialAdClosed() {
                incrementCounter();
            }
        });
        InterstitialAdHelper.getInstance().loadInterstitialAd();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //check if conig downloaded
                if (mIsConfigDownloaded) {
                    //showSplashAds, if available
                    handleSplashAds(mAdImage);
//                    showInterstialAd();
                    isTimeout = false;
                    LOGD(TAG_MSG, "Time out,but Config Downloaded");
//                    incrementCounter();
                    launchHomeScreen();

                } else {
                    isTimeout = true;
                }


            }
        }, 6000);

    }

    private synchronized void incrementCounter() {
        if (PreferencesManager.getInstance(this) != null) {
            int count = PreferencesManager.getInstance(this).getInterstitialAdCount() + 1;
            PreferencesManager.getInstance(this).setInterstialAdCount(count);
            Log.d("Ad launch Count", "Count:" + PreferencesManager.getInstance(this).getInterstitialAdCount());
        }

    }

    private synchronized void decrementCounter() {
//        mDownloadCnt++;
        if (PreferencesManager.getInstance(this) != null) {
            int count = PreferencesManager.getInstance(this).getInterstitialAdCount() - 1;
            PreferencesManager.getInstance(this).setInterstialAdCount(count);
        }

    }

    private void initViews() {
        mAdImage = (NetworkImageView) findViewById(R.id.splashAd);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

    }

    private void setProgressBarLocation() {
        int margin = Utility.getScreenHeight(this) / 2;
        int minMargin = Utility.getScreenHeight(this) / 4;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mProgressBar.getLayoutParams();
        params.bottomMargin = margin - minMargin / 3;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Added to fix the crash, caused on pressing home button, when spalsh ad is visible
        if (PreferencesManager.getInstance(this).isAppFirstLaunch())
            triggerBackPress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GAHandler.getInstance(this).SendScreenView(ApplicationUtils.APPLICATION_LAUNCH);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        Log.d(TAG_MSG, "Spalsh Activity Ending");

    }

    private void showSplashAd() {
        //this is added to avoid the count increment, when onResume() of BaseActivity is called after showing splash ads
        PreferencesManager.getInstance(this).setAppFirstLaunch(true);
        handleSplashAds(mAdImage);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                triggerBackPress();
            }
        }, Integer.parseInt(PreferencesManager.getInstance(this).getAdDuration()) * 1000);
    }

    private void triggerBackPress() {
        try {
            if (!isFinishing())
                super.onBackPressed();
            Log.d("AD", "Trigeer on BackPressed");
        } catch (Exception e) {

        }
    }

    private Boolean isTimeForSplashAd() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean(ApplicationConstants.PreferenceKeys.SPLASH_AD))
                return true;
            else
                return false;
        }
        return false;
    }


    private void downloadConfig() {
        Log.d(TAG_MSG, "Config request started");
        GsonRequest<Configuration> gsonRequest = new GsonRequest<Configuration>
                (getConfigUrl(), Configuration.class, null, new Response.Listener<Configuration>() {

                    @Override
                    public void onResponse(final Configuration mConfig) {
                        Log.d(TAG, "Config file @ " + getConfigUrl() + " Download successful");
                        Log.d(TAG_MSG, "Config downloaded");

                        mConfig.cleanNavigation(SplashActivity.this);

                        ConfigManager.getInstance().setConfiguration(mConfig);
                        setSplashAdData();
                        saveConfig(mConfig);

                        if (!cacheLaunch) {

                            Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                            Log.d(TAG_MSG, "Config Launch");
                            startActivity(i);
                            finish();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Config file Download Failed " + error.getMessage());
                        Log.d(TAG_MSG, "Config download failed");
                        if (!cacheLaunch) {
                            Toast.makeText(getApplicationContext(), "No Connectivity", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });

        VolleyRequestQueue.getInstance(this).addToRequestQueue(gsonRequest);

    }

    void saveConfig(Configuration c) {
        if (PreferencesManager.getInstance(this) != null)
            PreferencesManager.getInstance(this).saveConfig(c);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (!TextUtils.isEmpty(extras.getString(ApplicationConstants.BundleKeys.DEFAULT_NOTI_NAV))) {
                payloadFromNotification = extras.getString(ApplicationConstants.BundleKeys.DEFAULT_NOTI_NAV);
                int gcmNotificationId = getIntent().getIntExtra(ApplicationConstants.GCMKeys.GCM_NOTIFICATION_ID, 0);
                String Buildflavor = getPackageName();
                if (Buildflavor.equalsIgnoreCase(NDTVINDIA)) {
                    intentNotification = StringUtils.encodeString(payloadFromNotification);
                } else
                    intentNotification = payloadFromNotification;

                Utility.cancelNotification(gcmNotificationId, getApplicationContext());
            }
        }
        mPrefs = getPreferences(MODE_PRIVATE);
        //Configuration c = null;
        if (PreferencesManager.getInstance(this) != null) {
            c = PreferencesManager.getInstance(this).getConfig();
        }

        if (c != null) {

            ConfigManager.getInstance().setConfiguration(c);
            cacheLaunch = true;
            LOGD(TAG, "Config from cache");
            if (!mIsFromGcm && !mIsAppRefresh) {
                //This is done to show splash ads, on first launch
                if (PreferencesManager.getInstance(this) != null)
                    PreferencesManager.getInstance(this).setAppLaunchCount(0);
            }

            //Launch Activity , after showing splash Ad

            mIsConfigDownloaded = true;

            if (isInterstialAdNotAvailable()) {
                if (!mIsAppRefresh)
                    handleSplashAds(mAdImage);
                launchHomeScreen();
            }
        }
    }


    private void launchHomeScreen() {
        long intentDelay;
        final Handler handler = new Handler();
        if (TimeUtils.isValidDate(this) && PreferencesManager.getInstance(this).getAdStatus()
                && !TextUtils.isEmpty(PreferencesManager.getInstance(this).getAdImage())) {
//            intentDelay =(2* ApplicationConstants.SPLASH_SHOW_TIME);
            intentDelay = (Integer.parseInt(PreferencesManager.getInstance(this).getAdDuration()) * 1000);
            Log.d("SplashAd", "" + intentDelay);
        } else {
            intentDelay = splashStart + ApplicationConstants.SPLASH_SHOW_TIME - new Date().getTime() + ApplicationConstants.SPLASH_SHOW_TIME;

            Log.d("NormalLaunch", "" + intentDelay);
        }
        Log.d(TAG_MSG, "Launch home screen called & DownloadCount:" + mDownloadCnt);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (null != intentNotification)
                    i.putExtra(ApplicationConstants.BundleKeys.DEFAULT_NOTI_NAV, intentNotification);
                startActivity(i);
                finish();
            }
        }, intentDelay);


    }

    protected void setSplashAdData() {
        String splashAdKey = Utility.getSplashAdKey(this);
//        String splashAdKey = "http://1-dot-feedslug.appspot.com/ad/a320_50.png";
        PreferencesManager prefMngr = PreferencesManager.getInstance(this);
        ConfigManager configMngr = ConfigManager.getInstance();
        boolean adStatus = getAdStatus(configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_STATUS));
        prefMngr.setSplashAdData(adStatus, configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_START_DATE),
                configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_END_DATE), configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_DURATION),
                configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_FREQUENCY), configMngr.getCustomApiUrl(splashAdKey),
                configMngr.getSplashAdLocation(splashAdKey));

    }

    private void handleSplashAds(final NetworkImageView adImage) {
        //launch the homeActivity from here

        if (null != adImage) {
            PreferencesManager prefMngr = PreferencesManager.getInstance(SplashActivity.this);

            if (TimeUtils.isValidDate(this) && prefMngr.getAdStatus()
                    && !TextUtils.isEmpty(prefMngr.getAdImage())) {
                if ((prefMngr.getAppLaunchCount() % Integer.parseInt(prefMngr.getAdFrequency())) == 0) {
                    adImage.setImageUrl(prefMngr.getAdImage(), VolleyRequestQueue.getInstance(SplashActivity.this).getImageLoader());
                    prefMngr.setAppLaunchCount(0);
                    setLocation(adImage, prefMngr.getAdLocation());
                    adImage.setVisibility(View.VISIBLE);


                    Log.d("AD", "Visible");


                } else {
                    adImage.setVisibility(View.GONE);

                    Log.d("AD", "InVisible1");
                }

            } else {
                adImage.setVisibility(View.GONE);

                Log.d("AD", "InVisible2");
            }
        }
    }

    private String getConfigUrl() {
        if (BuildConfig.DEBUG) {
            return getString(R.string.config_url_debug);
        } else {
            return getString(R.string.config_url);
        }
    }

    /**
     * @param status
     * @return
     */
    private boolean getAdStatus(String status) {
        if ("1".equalsIgnoreCase(status))
            return true;
        return false;
    }

    /**
     * @param adImage
     */
    private void setLocation(NetworkImageView adImage, String location) {
        if (!TextUtils.isEmpty(location)) {
            int marginLeft = Integer.parseInt(location.split(",")[0]);
            int marginTop = Integer.parseInt(location.split(",")[1]);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAdImage.getLayoutParams();
            params.setMargins(marginLeft, marginTop, 0, 0);
            adImage.setLayoutParams(params);
        }
    }


}
