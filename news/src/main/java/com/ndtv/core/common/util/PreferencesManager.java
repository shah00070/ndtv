package com.ndtv.core.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.config.model.Configuration;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.radio.ui.LiveRadioFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * Created by sangeetha on 30/1/15.
 */
public class PreferencesManager implements LiveRadioFragment.LiveRadioConstants, ApplicationConstants.BundleKeys, ApplicationConstants.PreferenceKeys {
    private static final String SHARED_PREFS = "ndtv_shared_prefs";
    public static final String FTU_SHOWN_KEY = "ftu_shown";
    public static final String VOLUME_SELCTION_KEY = "volume_target";
    public static final String TERMINATION_POLICY_KEY = "termination_policy";
    public static final String STOP_ON_DISCONNECT = "1";
    public static final String CONTINUE_ON_DISCONNECT = "0";
    public static final String IS_SIGNED_IN = "from_accounts";
    public static final String CURRENT_ALBUM_POSITION = "curr_album_pos";
    public static final String CURRENT_SECTION_NAME = "curr_section_name";
    public static final String IS_BACK_FROM_ALBUM = "is_back_from_album";
    public static final String IS_EXPANDED = "is_expanded";
    public static final String CURRENT_TV_SHOWS = "tv_show_pos";
    public static final String CURRENT_SEARCH_SCREEN = "current_search_screen";
    public static final String IS_FROM_DRAWER = "is_from_drawer";
    public static final String SEARCH_TABS_POS = "serach_tabs_pos";
    public static final String IS_SEARCH_TABS = "is_search_tabs";
    public static final String CURRENT_DATE = "current_date";
    public static final String INTERSTIAL_AD_COUNT = "interstitial_ad_count";


    private SharedPreferences mSharedPrefs;
    private static PreferencesManager sInstance;
    private SharedPreferences.Editor mEditor;
    private Context mCtx;

    public synchronized static PreferencesManager getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new PreferencesManager(ctx);
        }
        return sInstance;
    }

    private PreferencesManager(Context ctx) {
        mSharedPrefs = ctx.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        mEditor = mSharedPrefs.edit();
        mCtx = ctx.getApplicationContext();
    }

    public synchronized SharedPreferences getSharedPref() {
        return mSharedPrefs;
    }


    public void saveConfig(Configuration c) {

        SharedPreferences.Editor prefsEditor = mSharedPrefs.edit();
        Gson gson = new Gson();
        String configJson = gson.toJson(c);
        prefsEditor.putString("APP_CONFIG", configJson);
        prefsEditor.commit();


    }

    public Configuration getConfig() {
        Gson gson = new Gson();
        String configJson = mSharedPrefs.getString("APP_CONFIG", "");
        if (configJson.equals(""))
            return null;
        return gson.fromJson(configJson, Configuration.class);
    }

    public void setLiveRadioUrl(final String url) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putString(RADIO_URL, url).commit();

            }
        });

    }

    public String getLiveRadioUrl() {

        return mSharedPrefs.getString(RADIO_URL, "");

    }

    public void setLiveRadioPlayStatus(final boolean value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(LIVE_RADIO_PLAY, value).commit();
            }
        });

    }

    public boolean isLiveRadioPlaying() {
        return mSharedPrefs.getBoolean(LIVE_RADIO_PLAY, false);
    }


    public void setLiveRadioStopped(final boolean value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(LIVE_RADIO_STOPPED, value).commit();
            }
        });
    }

    public boolean isLiveRadioStopped() {
        return mSharedPrefs.getBoolean(LIVE_RADIO_STOPPED, false);
    }

    public void setSectionPositionLiveRadio(final int sectionPosition) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putInt(LIVE_RADIO_SECTION_POSITION, sectionPosition).commit();
            }
        });
    }

    public int getSectionPositionLiveRadio() {
        synchronized (this) {
            return mSharedPrefs.getInt(LIVE_RADIO_SECTION_POSITION, 0);
        }
    }

    public void setErrorRadio(final boolean isError) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(LIVE_RADIO_ERROR, isError).commit();
            }
        });
    }


    public HashMap<String, String> loadMap() {
        HashMap<String, String> outputMap = new HashMap<String, String>();
        try {
            String jsonString = mSharedPrefs.getString("My_map", (new JSONObject()).toString());
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> keysItr = jsonObject.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                String value = (String) jsonObject.get(key);
                outputMap.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return outputMap;
    }

    public void setNewsIn60LauchInApp(final boolean fromService) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean("FROM_ALARM", fromService).commit();
            }
        });

    }


    public void saveMap(final HashMap<String, String> inputMap) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject(inputMap);
                String jsonString = jsonObject.toString();
//                mEditor.remove("My_map").commit();
                mEditor.putString("My_map", jsonString).commit();
            }
        });
    }

    public void setNotificationSettings(final String key, final boolean value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(key, value).commit();

            }
        });
    }

    public boolean getNotificationSettings(String key) {
        return mSharedPrefs.getBoolean(key, true);
    }

    public boolean getInverseNotificationSettings(String key) {
        return mSharedPrefs.getBoolean(key, false);
    }

    public void setIsBackFromAlbum(final String key, final boolean value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(key, value).commit();
            }
        });
    }

    public boolean getIsBackFromAlbum(String key) {
        return mSharedPrefs.getBoolean(key, false);
    }

    public void setNewsIdentifierToHandleBackPress(final String key, final String value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putString(key, value).commit();
            }
        });
    }

    public String getNewsIdentifierToHandleBackPress(String key) {
        return mSharedPrefs.getString(key, null);
    }

    public void setIsBackFromCommentList(final String key, final boolean value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(key, value).commit();
            }
        });
    }

    public boolean getIsBackFromCommentList(String key) {
        return mSharedPrefs.getBoolean(key, false);
    }

    public void setCurrentPhotoIndex(final String key, final int value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putInt(key, value).commit();
            }
        });
    }

    public int getCurrentPhotoIndex(String key) {
        return mSharedPrefs.getInt(key, 0);
    }

    public void setCurrentAlbumPosition(final String key, final int value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putInt(key, value).commit();
            }
        });
    }

    public int getCurrentAlbumPosition(String key) {
        return mSharedPrefs.getInt(key, 0);
    }

    public void setCurrentSectionName(final String key, final String value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putString(key, value).commit();
            }
        });
    }

    public String getCurrentSectionName(String key) {
        return mSharedPrefs.getString(key, null);
    }


    public void setIsExpanded(final String key, final boolean value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(key, value).commit();
            }
        });
    }

    public boolean getIsExpanded(String key) {
        return mSharedPrefs.getBoolean(key, false);
    }


    public boolean getNewsIn60LauchInApp() {
        return mSharedPrefs.getBoolean("FROM_ALARM", false);
    }

    public void setCurrentTimeZone(final String id) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putString(CURRENT_TIME_ZONE, id).commit();
            }
        });

    }

    public String getCurrentTimeZone() {
        return mSharedPrefs.getString(CURRENT_TIME_ZONE, TimeZone.getDefault().getID());
    }


    public void setPushStatus(final boolean isEnabled) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(PUSH_NOTIFICATION_STATUS, isEnabled).commit();
            }
        });
    }

    public boolean getPushStatus() {
        return mSharedPrefs.getBoolean(PUSH_NOTIFICATION_STATUS, true);

    }

    public void setAppLaunchCount(final int count) {

        mEditor.putInt(ApplicationConstants.PreferenceKeys.APP_LAUNCH_COUNT, count).apply();

    }

    public int getAppLaunchCount() {
        return mSharedPrefs.getInt(APP_LAUNCH_COUNT, 0);
    }

    public void setSplashAdData(final boolean status, final String startDate, final String endDate,
                                final String duration, final String frequency, final String url, final String location) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getPreferenceExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putString(SPLASH_AD_END_DATE, endDate);
                mEditor.putString(SPLASH_AD_START_DATE, startDate);
                mEditor.putString(SPLASH_AD_FREQUENCY, frequency);
                mEditor.putBoolean(SPLASH_AD_STATUS, status);
                mEditor.putString(SPLASH_AD_DURATION, duration);
                mEditor.putString(SPLASH_AD_IMAGE, url);
                mEditor.putString(SPLASH_AD_LOCATION, location);
                mEditor.commit();

            }
        });
    }


    public String getAdStratDate() {
        return mSharedPrefs.getString(SPLASH_AD_START_DATE, "");
    }

    public String getAdEndDate() {
        return mSharedPrefs.getString(SPLASH_AD_END_DATE, "");
    }

    public String getAdImage() {
        return mSharedPrefs.getString(SPLASH_AD_IMAGE, "");
    }

    public String getAdLocation() {
        return mSharedPrefs.getString(SPLASH_AD_LOCATION, "");
    }

    public String getAdFrequency() {
        return mSharedPrefs.getString(SPLASH_AD_FREQUENCY, "5");
    }

    public String getAdDuration() {
        return mSharedPrefs.getString(SPLASH_AD_DURATION, "5");
    }

    public boolean getAdStatus() {
        return mSharedPrefs.getBoolean(SPLASH_AD_STATUS, false);
    }

    public boolean isAppFirstLaunch() {
        return mSharedPrefs.getBoolean(FIRST_LAUNCH, false);

    }

    public void setAppFirstLaunch(final boolean value) {

        mEditor.putBoolean(FIRST_LAUNCH, value).commit();

    }

    public void setIsFromDeepLinking(boolean fromDeepLinking) {
        mEditor.putBoolean(ApplicationConstants.BundleKeys.IS_DEEPLINK_URL, fromDeepLinking).commit();
    }

    public boolean isFromDeepLinking() {
        return mSharedPrefs.getBoolean(ApplicationConstants.BundleKeys.IS_DEEPLINK_URL, false);
    }

    public static boolean isFtuShown(Context ctx) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(FTU_SHOWN_KEY, false);
    }

    public static void setFtuShown(Context ctx) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPref.edit().putBoolean(FTU_SHOWN_KEY, true).commit();
    }

    public void setIsPhotoFullscreen(boolean isPhotoFullscreen) {
        mEditor.putBoolean(IS_PHOTO_FULL_SCREEN, isPhotoFullscreen).commit();
    }

    public boolean getIsPhotoFullScreen() {
        return mSharedPrefs.getBoolean(IS_PHOTO_FULL_SCREEN, false);
    }

//    public void setAdCountValue(final String key, final int value) {
//        NdtvApplication app = NdtvApplication.getApplication(mCtx);
//        app.getMultiThreadExecutorService().submit(new Runnable() {
//            @Override
//            public void run() {
//                mEditor.putInt(key, value).commit();
//            }
//        });
//    }
//
//    public int getAdCountValue(String key) {
//        return mSharedPrefs.getInt(key, 0);
//    }

    public void setCurrentTvShowPos(final String key, final int value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putInt(key, value).commit();
            }
        });
    }

    public int getCurrentTvShowPos(String key) {
        return mSharedPrefs.getInt(key, 0);
    }

    public void setCurrentScreenForSearch(final String key, final int value) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putInt(key, value).commit();
            }
        });
    }

    public int getCurrentScreenForSearch(String key) {
        return mSharedPrefs.getInt(key, 0);
    }

    public void setIsFromNavigationDrawer(final String key, final boolean isFromDrawer) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(key, isFromDrawer).commit();
            }
        });
    }

    public boolean isFromNavigationDrawer(String key) {
        return mSharedPrefs.getBoolean(key, false);
    }

    public void setSearchTabsSectionPos(final String key, final String serachtabsPos) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putString(key, serachtabsPos).commit();
            }
        });
    }

    public String getSearchTabsSectionPos(String key) {
        return mSharedPrefs.getString(key, null);
    }

    public void setIsSerachTabs(final String key, final boolean isSearchTab) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putBoolean(key, isSearchTab).commit();
            }
        });
    }

    public boolean getIsSerachTabs(String key) {
        return mSharedPrefs.getBoolean(key, false);
    }

    public void saveCurrentNotificationId(final int notificationId) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putInt(ApplicationConstants.PreferenceKeys.NOTIFICATION_ID, notificationId).commit();
            }
        });
    }

    public int getSavedNotificaionId() {
        return mSharedPrefs.getInt(ApplicationConstants.PreferenceKeys.NOTIFICATION_ID, 0);
    }

    public void saveCurrentDate(final String date) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putString(CURRENT_DATE, date).commit();
            }
        });


    }

    public String getSavedDate() {
        return mSharedPrefs.getString(CURRENT_DATE, null);
    }

    public void setInterstialAdCount(final int count) {
        NdtvApplication app = NdtvApplication.getApplication(mCtx);
        app.getMultiThreadExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                mEditor.putInt(INTERSTIAL_AD_COUNT, count).commit();
            }
        });
    }

    public int getInterstitialAdCount() {
        return mSharedPrefs.getInt(INTERSTIAL_AD_COUNT, 0);
    }

}
