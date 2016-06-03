package com.ndtv.core.ads.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.ndtv.core.R;
import com.ndtv.core.ads.utility.AdConstants;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Navigation;
import com.ndtv.core.config.model.Section;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.util.GAHandler;

import java.util.List;

import static com.ndtv.core.util.LogUtils.LOGD;

/**
 * Created by veena on 2/2/15.
 */
public class BannerAdFragment extends Fragment {

    private static final String LOG_TAG = "BANNER_AD";
    public static final String MOBILE_SERVER_NAME = "http://mrp.rubiconproject.com/";
    public static final String RFM_PUB_ID = "111782";//"111008";
    public static final String RFM_APP_ID = "99476A307EA501320B1B22000B3510F7";  //"281844F0497A0130031D123139244773";

    private PublisherAdView mAdView;
    private AdListener mListener;
    public AdListener mAdUpdateListener;
    private ImageView mCloseBtn;
    private boolean mIsPhotos;
    private boolean mIsLiveTv;
    private boolean mIsVideo;

    public interface AdListener {

        void loadBannerAd(int navigationPos, int sectionPos, String contentUrl, boolean isPhotos, int photoIndex, boolean isLiveTv, boolean isVideo);

        void hideIMBannerAd();

        void showIMBannerAd(boolean mIsPhotos, boolean mIsLiveTv, boolean mIsVideo);

    }

    public BannerAdFragment() {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdUpdateListener = (AdListener) activity;
    }

    public void setAdListener(AdListener listener) {
        mListener = listener;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ad, container, false);

        if (getArguments() != null) {
            mIsPhotos = getArguments().getBoolean(AdConstants.IS_PHOTOS);
            mIsLiveTv = getArguments().getBoolean(AdConstants.IS_LIVETV);
            mIsVideo = getArguments().getBoolean(AdConstants.IS_VIDEO);
        }


        if (mIsPhotos) {
            mCloseBtn = (ImageView) view.findViewById(R.id.ad_close_btn);
            if (mCloseBtn != null) {
                mCloseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAdUpdateListener != null)
                            mAdUpdateListener.hideIMBannerAd();
                    }
                });
            }
        }
        return view;
    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mAdView != null)
            mAdView.pause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null)
            mAdView.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdUpdateListener != null)
            mAdUpdateListener.hideIMBannerAd();
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null)
            mAdView.destroy();
    }

    public void loadIMBannerAd(final String siteId, String contentUrl, final Navigation navigation, final Section section, final int photoIndex) {
        LOGD(LOG_TAG, "Banner Ad Id:" + siteId);

        if (mAdView != null) {
            mAdView.destroy();
        }

        if (getActivity() == null)
            return;
        mAdView = new PublisherAdView(getActivity());


        if (mAdView != null) {
            mAdView.setAdUnitId(siteId); //"/1068322/News_Default"
            mAdView.setAdSizes(AdSize.BANNER);
            mAdView.setAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                    LOGD(LOG_TAG, "Ad failed:" + getErrorReason(errorCode));
                    if (mListener != null)
                        mListener.hideIMBannerAd();
                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    LOGD(LOG_TAG, "Ad loaded");

                    if(PreferencesManager.getInstance(getActivity()).getIsPhotoFullScreen()){
                        if (mListener != null)
                            mListener.hideIMBannerAd();
                        return;
                    }
                    if((photoIndex == -1) || (photoIndex > -1 && PreferencesManager.getInstance(getActivity()).getCurrentPhotoIndex(ApplicationConstants.PreferenceKeys.CURRENT_IMAGE_INDEX) == photoIndex))
                        showBannerAd();
                }

                private String getErrorReason(int errorCode) {
                    switch (errorCode) {
                        case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                            return "Internal error";
                        case AdRequest.ERROR_CODE_INVALID_REQUEST:
                            return "Invalid request";
                        case AdRequest.ERROR_CODE_NETWORK_ERROR:
                            return "Network Error";
                        case AdRequest.ERROR_CODE_NO_FILL:
                            return "No fill";
                        default:
                            return "Unknown error";
                    }
                }
            });
        }

        mAdView.loadAd(new PublisherAdRequest.Builder().build());
    }

    private void showBannerAd() {
        if (null != getView()) {
            FrameLayout frame = (FrameLayout) getView().findViewById(R.id.adView);
            if (null != mAdView) {
                frame.removeAllViews();
                mAdView.setVisibility(View.VISIBLE);
                frame.addView(mAdView);

                if (mIsPhotos && mCloseBtn != null) {
                    mCloseBtn.setVisibility(View.VISIBLE);
                }

                if (mListener != null)
                    mListener.showIMBannerAd(mIsPhotos, mIsLiveTv, mIsVideo);
            }
        }
    }

    public void loadAd(int navigationPos, int sectionPos, String contentUrl, int photoIndex) {
        Section section = null;
        if (navigationPos == -1 && sectionPos == -1) {
            if (ConfigManager.getInstance().getCustomApiUrl(AdConstants.DEFAULT_DFP_ID) != null)
                loadIMBannerAd(ConfigManager.getInstance().getCustomApiUrl(AdConstants.DEFAULT_DFP_ID), contentUrl, null, null, photoIndex);
        } else {
            Navigation navigation = ConfigManager.getInstance().getNavigation(navigationPos);
            if(navigation != null) {
                List<Section> sectionList = navigation.getSectionList();
                if (null != sectionList && sectionList.size() > sectionPos) {
                    section = sectionList.get(sectionPos);
                }
            }

            if (null != section && !TextUtils.isEmpty(section.dfp_ad_site_id)) {  // && !TextUtils.isEmpty(section.ad_site_id)
                loadIMBannerAd(section.dfp_ad_site_id, contentUrl, navigation, section, photoIndex);
            } else if (null != navigation && null != navigation.dfp_ad_site_id && !TextUtils.isEmpty(navigation.dfp_ad_site_id)) {
                loadIMBannerAd(navigation.dfp_ad_site_id, contentUrl, navigation, section, photoIndex);
            } else {
                String siteId = ConfigManager.getInstance().getCustomApiUrl(AdConstants.DEFAULT_DFP_ID);
                if (null != siteId && !TextUtils.isEmpty(siteId))
                    loadIMBannerAd(siteId, contentUrl, navigation, section, photoIndex);
            }
        }
    }


    /*public static interface AdConstants {
        String ENABLED = "1";
        String DISABLED = "0";

        String ADS_BANNER_AD_STATUS_NEWSLIST = "ads_banner_ad_status";
        String DEFAULT_DFP_ID = "ads_DFP_default_site_id";

        String ADS_INMOBI_NATIVE_AD_STATUS = "ads_native_status";
        String NATIVE_ADS_POSITION = "ads_native_position";
        String ADS_INMOBI_NATIVE_FREQ_CAP = "ads_native_frequency_cap";
        String NATIVE_AD_SITE_ID = "ads_DFP_default_site_id";

        String NATIVE_AD_STATUS_FORPHOTOS = "ads_native_photos_status";
        String NATIVE_INTERSTITIAL_AD_FREQUENCY = "AdsInterstitialCount";
        String NATIVE_INTERSTITIAL_AD_ID = "ads_interstitial_DFP_id"; //Changed native ad id field for lollypop release.

        String NEWS_IN_60_SITE_ID = "news_in_60_secs_ads_native_site_id";
        String NEWS_IN_60_AD_FREQUENCY = "news_in_60_secs_ads_native_position";
        String NEWS_IN_60_INMOBI_FREQ_CAP = "news_in_60_secs_ads_native_frequency_cap";

        String IS_PHOTOS = "IS_PHOTOS";
        String AD_FRAGMENT_TAG = "AD_FRAGMENT";
    }*/

}

