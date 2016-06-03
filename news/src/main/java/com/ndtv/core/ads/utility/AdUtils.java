package com.ndtv.core.ads.utility;

import com.ndtv.core.config.ConfigManager;

/**
 * Created by veena on 17/8/15.
 */
public class AdUtils {

    public static boolean isStoriesInterstitialEnabled() {
        String status = ConfigManager.getInstance().getCustomApiStatus(AdConstants.ADS_STORIES_INTERSTITIAL_ID);
        if (status == null) {
            return false;
        }
        if (AdConstants.ENABLED.equalsIgnoreCase(status))
            return true;
        return false;
    }


    public static int getStoriesInterstitialAdFrequency() {
        if (ConfigManager.getInstance().getCustomApiFrequency(AdConstants.ADS_STORIES_INTERSTITIAL_ID) != null)
            return Integer.valueOf(ConfigManager.getInstance().getCustomApiFrequency(AdConstants.ADS_STORIES_INTERSTITIAL_ID));
        else
            return 0;
    }

    public static boolean isPhotosInterstitialEnabled() {
        String status = ConfigManager.getInstance().getCustomApiStatus(AdConstants.ADS_PHOTO_INTERSTITIAL_ID);
        if (status == null) {
            return false;
        }
        if (AdConstants.ENABLED.equalsIgnoreCase(status))
            return true;
        return false;
    }

    public static int getPhotoInterstitialAdFrequency() {
        if (ConfigManager.getInstance().getCustomApiFrequency(AdConstants.ADS_PHOTO_INTERSTITIAL_ID) != null) {
            return Integer.valueOf(ConfigManager.getInstance().getCustomApiFrequency(AdConstants.ADS_PHOTO_INTERSTITIAL_ID));
        } else
            return 0;
    }


    public static boolean isLaunchInterstitialEnabled() {
        String status = ConfigManager.getInstance().getCustomApiStatus(AdConstants.ADS_LAUNCH_INTERSTITIAL_ID);
        if (status == null) {
            return false;
        }
        if (AdConstants.ENABLED.equalsIgnoreCase(status))
            return true;
        return false;
    }
}
