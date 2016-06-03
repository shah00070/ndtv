package com.ndtv.core.ads.utility;

/**
 * Created by veena on 17/8/15.
 */
public interface AdConstants {

    String ENABLED = "1";
    String DISABLED = "0";

    String ADS_BANNER_AD_STATUS_NEWSLIST = "ads_banner_ad_status";
    String DEFAULT_DFP_ID = "ads_DFP_default_site_id";

    String ADS_INMOBI_NATIVE_AD_STATUS = "ads_native_status";
    String NATIVE_ADS_POSITION = "ads_native_position";
    String ADS_INMOBI_NATIVE_FREQ_CAP = "ads_native_frequency_cap";
    String NATIVE_AD_SITE_ID = "ads_native_site_id"; //"ads_DFP_default_site_id";

    String NATIVE_AD_STATUS_FORPHOTOS = "ads_native_photos_status";
    String NATIVE_INTERSTITIAL_AD_FREQUENCY = "AdsInterstitialCount";
    String NATIVE_INTERSTITIAL_AD_ID = "ads_interstitial_DFP_id"; //Changed native ad id field for lollypop release.

    String NEWS_IN_60_SITE_ID = "news_in_60_secs_ads_native_site_id";
    String NEWS_IN_60_AD_FREQUENCY = "news_in_60_secs_ads_native_position";
    String NEWS_IN_60_INMOBI_FREQ_CAP = "news_in_60_secs_ads_native_frequency_cap";

    String ADS_INTERSTITIAL_STATUS = "ads_interstitial_status";
    String ADS_INTERSTITIAL_FREQUENCY_CAP = "ads_interstitial_frequency_cap";
    String ADS_LAUNCH_INTERSTITIAL_ID = "ads_launch_interstitial_id";
    String ADS_STORIES_INTERSTITIAL_ID = "ads_stories_interstitial_id";
    String ADS_PHOTO_INTERSTITIAL_ID = "ads_photo_interstitial_id";

    String IS_PHOTOS = "IS_PHOTOS";
    String IS_LIVETV = "IS_LIVETV";
    String IS_VIDEO = "IS_VIDEO";
    String AD_FRAGMENT_TAG = "AD_FRAGMENT";
}
