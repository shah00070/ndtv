package com.ndtv.core.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.BuildConfig;
import com.ndtv.core.R;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Configuration;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.deeplinking.io.DeepLinkingManager;

import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by deepti on 27/8/15.
 */
public class ConfigUtils {
    private static final String TAG = makeLogTag(ApplicationUtils.class);

    public static void refreshAppConfig(final Context ctx){
        DeepLinkingManager.getInstance().downloadConfig(ctx, getConfigUrl(ctx), new Response.Listener<Configuration>() {
            @Override
            public void onResponse(Configuration mConfig) {

                mConfig.cleanNavigation(ctx);

                ConfigManager.getInstance().setConfiguration(mConfig);
                setSplashAdData(ctx);
                saveConfig(mConfig, ctx);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Config file Download Failed " + volleyError.getMessage());
            }
        });
    }

    /**
     * @param status
     * @return
     */
    private static boolean getAdStatus(String status) {
        if ("1".equalsIgnoreCase(status))
            return true;
        return false;
    }
    private static String getConfigUrl(Context ctx) {
        if (BuildConfig.DEBUG) {
            return ctx.getString(R.string.config_url_debug);
        } else {
            return ctx.getString(R.string.config_url);
        }
    }
    private static void saveConfig(Configuration c, Context ctx) {
        if (PreferencesManager.getInstance(ctx) != null)
            PreferencesManager.getInstance(ctx).saveConfig(c);
    }
    protected static void setSplashAdData(Context ctx) {
        String splashAdKey = Utility.getSplashAdKey(ctx);
//        String splashAdKey = "http://1-dot-feedslug.appspot.com/ad/a320_50.png";
        PreferencesManager prefMngr = PreferencesManager.getInstance(ctx);
        ConfigManager configMngr = ConfigManager.getInstance();
        boolean adStatus = getAdStatus(configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_STATUS));
        prefMngr.setSplashAdData(adStatus, configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_START_DATE),
                configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_END_DATE), configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_DURATION),
                configMngr.getCustomApiUrl(ApplicationConstants.PreferenceKeys.SPLASH_AD_FREQUENCY), configMngr.getCustomApiUrl(splashAdKey),
                configMngr.getSplashAdLocation(splashAdKey));

    }
}
