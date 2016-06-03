package com.ndtv.core.ads.utility;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.formats.NativeContentAd;

import static com.ndtv.core.util.LogUtils.LOGD;

/**
 * Loads and displays NativeContentAd.
 */
public class NativeContentAdLoader {
    private String adUnitId;
    private NativeContentAd contentAd;
    private NativeAdManager.NativeContentAdListener mNativeAdListener;

    /**
     * Creates a {@code NativeContentAdLoader}.
     *
     * @param adUnitId  The ad unit ID used to request ads.
     * @param mListener
     */
    public NativeContentAdLoader(String adUnitId, NativeAdManager.NativeContentAdListener mListener) {
        this.adUnitId = adUnitId;
        LOGD("NATIVE_AD", "AdId:" + adUnitId);
        mNativeAdListener = mListener;
    }

    /**
     * Loads a NativeContentAd and displays its assets inside the views contained by an
     */
    public void loadAd(final Context context) {
        // If an ad previously loaded, reuse it instead of requesting a new one.
        if (contentAd != null) {
            return;
        }

        AdLoader adLoader = new AdLoader.Builder(context, adUnitId)  // "/6499/example/native"
                .forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                    @Override
                    public void onContentAdLoaded(NativeContentAd contentAd) {
                        LOGD("NATIVE_AD", "Native Ad success");
                        NativeContentAdLoader.this.contentAd = contentAd;
                        mNativeAdListener.onContentAdLoaded(contentAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        LOGD("NATIVE_AD", "ErrorCode:" + errorCode);
                        mNativeAdListener.onContentAdFailed();
                    }
                }).build();
        if(adLoader != null){
           try {
                adLoader.loadAd(new PublisherAdRequest.Builder().build());
            }catch (Exception e){
                LOGD("NATIVE_AD","Error in adLoader.loadAd "+e.toString());
            }
        }
    }
}
