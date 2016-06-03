package com.ndtv.core.ads.utility;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.ndtv.core.ui.SplashActivity;

import static com.ndtv.core.util.LogUtils.LOGD;

/**
 * Created by veena on 13/8/15.
 */
public class InterstitialAdHelper {

    private static String TAG = "INTERSTITIAL_HELPER";
    private static InterstitialAdHelper mInterstitialHelper;

    private PublisherInterstitialAd mPublisherInterstitialAd;

    public static interface InterstitialAdListener{
        void onInterstitialAdLoaded();
        void onInterstitialAdFailed();
        void onInterstitialAdClosed();
    }

    public static InterstitialAdHelper getInstance(){
        if(mInterstitialHelper == null)
            mInterstitialHelper = new InterstitialAdHelper();
        return mInterstitialHelper;
    }

    public void  initInterstitial(final Context context, String siteId, final InterstitialAdListener listener){
        mPublisherInterstitialAd = new PublisherInterstitialAd(context);
        mPublisherInterstitialAd.setAdUnitId(siteId);
        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d("SPLASH", "Ad closed");
                listener.onInterstitialAdClosed();
                if(!(context instanceof SplashActivity))
                    loadInterstitialAd();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                LOGD(TAG, "Interstitial ad failed:" + getErrorReason(errorCode));
                listener.onInterstitialAdFailed();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                LOGD("INTER_AD", "Interstitial ad loaded");
                listener.onInterstitialAdLoaded();
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

    public void loadInterstitialAd(){
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
        if(mPublisherInterstitialAd != null)
            mPublisherInterstitialAd.loadAd(adRequest);
    }

    public void showInterstitialAd(){
        if(mPublisherInterstitialAd != null && mPublisherInterstitialAd.isLoaded())
            mPublisherInterstitialAd.show();
    }

    public void stopInterstitialAd(){
        mPublisherInterstitialAd = null;
    }
}
