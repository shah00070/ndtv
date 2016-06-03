/*
  * Copyright (C) 2015 Rubicon Project. All rights reserved
 * 
 * @author: Rubicon Project.
 *  file for integrating RFM SDK with Admob SDK
 *  RFM SDK will be triggered via Admob Custom Events
 *  Google Play Services is required and utilizes com.google.android.gms.ads.mediation.customevent.*
 *  
 *  version: 2.0.0
 * 
 */
package com.rfm.extras.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.rfm.sdk.RFMAdRequest;
import com.rfm.sdk.RFMAdView;
import com.rfm.sdk.RFMAdViewListener;

//import com.google.ads.AdSize;
//import com.google.ads.mediation.MediationAdRequest;
//import com.google.ads.mediation.customevent.CustomEventBanner;
//import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
//import com.google.ads.mediation.customevent.CustomEventBannerListener;
//import com.google.ads.mediation.customevent.CustomEventInterstitial;
//import com.google.ads.mediation.customevent.CustomEventInterstitialListener;

public class RFMAdmobAdapter implements CustomEventBanner, RFMAdViewListener {
    private static final String LOG_TAG = "RFMAdmobAdapter";
    private CustomEventBannerListener customEventListener;
    private RFMAdView mbsAdView;
    private RFMAdRequest mAdRequest;

    ////RFM Placement Settings
    private static final String RFM_SERVER_NAME = "http://mrp.rubiconproject.com/";
    private static final String RFM_PUB_ID = "111782";//"111008";


    //MBSAdView listener methods

    /**
     * Sent when an ad request failed to load an ad. Client can choose to set the view to invisible
     * if it was set to visible.
     *
     * @param adView - MBSAdView instance that generated this method
     */

    @Override
    public void onAdFailed(RFMAdView adView) {
        Log.d(LOG_TAG, "RFM :onAdFailed ");
        this.mbsAdView.setVisibility(View.GONE);

        if (this.customEventListener != null) {
            //this.customEventListener.onFailedToReceiveAd();
            this.customEventListener.onAdFailedToLoad(222); // 222 is random code, can be customized
        }
    }

    /**
     * Sent when an ad request loaded an ad; this is a good opportunity to make the ad view
     * visible if it has been set to invisible till this time.
     *
     * @param adView - MBSAdView instance that generated this method
     */
    @Override
    public void onAdReceived(RFMAdView adView) {
        Log.d(LOG_TAG, "RFM :onAdReceived ");
        this.mbsAdView.setVisibility(View.VISIBLE);
        this.mbsAdView.displayAd();
        if (this.customEventListener != null) {
            //this.customEventListener.onReceivedAd(adView);
            this.customEventListener.onAdLoaded(adView);
        }
    }

    /**
     * Sent when the ad request has been processed.
     *
     * @param adView           -  MBSAdView instance that generated this method
     * @param requestUrl       - One of two possible values :
     *                         a) Request URL if the ad request from client was accepted by SDK and a request will be potentially
     *                         attempted.
     *                         b) error message if the ad request from client was denied by SDK. Sent typically when the ad view is in a
     *                         state where it cannot accept new ad requests.
     * @param adRequestSuccess -
     *                         a) true if the ad request from client was accepted by SDK and a request will be potentially
     *                         attempted.
     *                         b) false if the ad request from client was denied by SDK. Sent typically when the adview is in a
     *                         state where it cannot accept new ad requests.
     */
    @Override
    public void onAdRequested(RFMAdView adView, String requestUrl, boolean adRequestSuccess) {
        Log.d(LOG_TAG, "RequestRFMAd: " + requestUrl);
    }

    /**
     * Sent when user interaction with ad results in transition of view type from banner to full screen
     * landing view or vice-versa
     *
     * @param adView - MBSAdView instance that generated this method
     * @param event  - User interaction event of type MBSAdViewEvent
     */
    @Override
    public void onAdStateChangeEvent(RFMAdView adView, RFMAdViewEvent event) {
        Log.d(LOG_TAG, "RFM :onAdStateChangeEvent: " + event);
        switch (event) {
            case FULL_SCREEN_AD_WILL_DISPLAY:
                break;
            case FULL_SCREEN_AD_DISPLAYED:
                //	this.customEventListener.onPresentScreen();
                this.customEventListener.onAdOpened();
                break;

            case FULL_SCREEN_AD_WILL_DISMISS:
                break;
            case FULL_SCREEN_AD_DISMISSED:
                //nmpkoj omojthis.customEventListener.onDismissScreen();
                this.customEventListener.onAdClosed();
                break;
            case AD_DISMISSED:
                break;
        }
    }

    @Override
    public void didDisplayAd(RFMAdView arg0) {
        // TODO Auto-generated method stub
        Log.v(LOG_TAG, "Ad did display");
    }

    @Override
    public void didFailedToDisplayAd(RFMAdView arg0, String arg1) {
        // TODO Auto-generated method stub
        Log.v(LOG_TAG, "Failed to display ad");
    }

    @Override
    public void onAdResized(RFMAdView arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        Log.v(LOG_TAG, "Ad resized");
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void requestBannerAd(Context context,
                                CustomEventBannerListener listener, String serverParameter,
                                AdSize adSize, MediationAdRequest mediationAdRequest,
                                Bundle customEventExtras) {
        // TODO Auto-generated method stub
        Log.d(LOG_TAG, "custom event trigger, appId: " + serverParameter);
        this.customEventListener = listener;
        int adHeight = adSize.getHeight();
        int adWidth = adSize.getWidth();

        //Layout banner ad view
        if (this.mbsAdView == null) {
            this.mbsAdView = new RFMAdView(context, null, this);
            this.mbsAdView.setVisibility(View.GONE);
        }
        //Set Ad Request parameters
        if (this.mAdRequest == null)
            this.mAdRequest = new RFMAdRequest();

        this.mAdRequest.setRFMParams(RFM_SERVER_NAME, RFM_PUB_ID, serverParameter);

        if (adSize.isFullWidth()) {
            this.mAdRequest.setAdDimensionParams(-1, adHeight);
        } else {
            this.mAdRequest.setAdDimensionParams(adWidth, adHeight);
        }

		/*Optional Targeting Parameters*/
       	/*Uncomment and configure desired parameters*/
//		mAdRequest.setMBSAdMode(MBSConstants.MBS_AD_MODE_TEST);       	
//      mAdRequest.setMBSTestAdId("9130");
//      mAdRequest.setLocation(null); //Pass a valid Location object for location targeting
//      mAdRequest.setTargetingParams(null);//Pass a valid Key-Value HashMap for k-v targeting
        
        /*End Optional Targeting Parameters*/
        //Request Ad
        if (!this.mbsAdView.requestRFMAd(mAdRequest)) {
            //this.customEventListener.onFailedToReceiveAd();
            this.customEventListener.onAdFailedToLoad(11);
        }
    }
}