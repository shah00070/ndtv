package com.ndtv.core.ads.utility;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.formats.NativeContentAd;
import com.ndtv.core.ui.NewsListingFragment;

import static com.ndtv.core.util.LogUtils.LOGD;

/**
 * Created by veena on 12/2/15.
 */
public class NativeAdManager {

    private static NativeAdManager sNativeAdManager;
    public NativeContentAd mContentAd;

    public interface NativeContentAdListener {
        void onContentAdLoaded(NativeContentAd contentAd);

        void onContentAdFailed();
    }

    public static NativeAdManager getNewInstance() {
        if (sNativeAdManager == null)
            sNativeAdManager = new NativeAdManager();
        return sNativeAdManager;
    }

    public void downloadNativeAd(Context context, final NewsListingFragment.NativeAdListener listener, String nativeAdSiteId) {
        NativeContentAdLoader contentAdLoader;
        contentAdLoader = new NativeContentAdLoader(nativeAdSiteId, new NativeContentAdListener() { // "/1068322/native_ios_news"
            @Override
            public void onContentAdLoaded(NativeContentAd contentAd) {
                if (contentAd != null)
                    mContentAd = contentAd;
                if (listener != null)
                    listener.onAdLoadSuccess(contentAd);
            }

            @Override
            public void onContentAdFailed() {
                LOGD("Native_AD", "Native ad failed");
                if (listener != null)
                    listener.onAdLoadSuccess(null);
            }
        });
        if (context != null)
            contentAdLoader.loadAd(context);

    }
}
