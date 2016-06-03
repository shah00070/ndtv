package com.ndtv.core.ui.widgets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.ads.utility.AdConstants;
import com.ndtv.core.ads.utility.AdUtils;
import com.ndtv.core.ads.utility.InterstitialAdHelper;
import com.ndtv.core.ads.utility.NativeAdManager;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.PhotoFeed;
import com.ndtv.core.config.model.PhotoResults;
import com.ndtv.core.config.model.Photos;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.deeplinking.io.DeepLinkingManager;
import com.ndtv.core.ui.BaseActivity;
import com.ndtv.core.ui.FullPhotoFragment;
import com.ndtv.core.ui.NewsListingFragment;
import com.ndtv.core.ui.adapters.AlbumPagerAdapter;
import com.ndtv.core.util.BitmapCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chandan kumar on 26/01/15.
 */
public class AlbumDetailFragment extends BaseFragment implements ApplicationConstants.PreferenceKeys, ViewPager.OnPageChangeListener {

    static final String LOG_TAG = "AlbumDetailFragment";
    protected int mNavigationPos;
    protected int mSectionPos;
    private ViewPager mViewPager;
    private int mPosition;
    protected PhotoResults photoList;
    protected String section;
    private AlbumPagerAdapter mAdapter;
    private Photos photoItem;
    private List<Photos> mPhotos;
    private int mPreviousAdPoSition;
    private int mAdFreqCount;
    protected List<Photos> photoListWithoutAds = new ArrayList<>();
    private IVideoCastConsumer mCastConsumer;
    private boolean isCastConnected = false;
    private ArrayList<PhotoFeed> photoFeedArrayList;
    private CharSequence mCurActionBarTitle;
    private BaseActivity mActivity;
    //DeepLinking
    private String mAlbumLinkUrl;
    private View rootView;
    private BannerAdFragment.AdListener mAdUpdateListener;
    protected MenuItem mMediaRouteMenuItem;
    public VideoCastManager mCastManager;
    //private VideoCastManager mVideoCastManager;

    private int adFreq;
    private boolean bIsFromBckground = false;
    private boolean bIsFromSearch;

    public static AlbumDetailFragment newInstance(PhotoResults photoList, String section, int navigationPosition, int pos, boolean bIsFromSearch) {

        AlbumDetailFragment albumFrgmnt = new AlbumDetailFragment();
        albumFrgmnt.photoList = photoList;
        albumFrgmnt.photoListWithoutAds.addAll(photoList.getPhotos());
        albumFrgmnt.section = section;
        albumFrgmnt.mNavigationPos = navigationPosition;
        albumFrgmnt.mSectionPos = pos;
        albumFrgmnt.bIsFromSearch = bIsFromSearch;
        return albumFrgmnt;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseCastManager.checkGooglePlayServices(getActivity());
        getActivity().invalidateOptionsMenu();
        mAlbumLinkUrl = photoList.getLink();
        mCastManager = NdtvApplication.getCastManager();
        mCastManager.reconnectSessionIfPossible();
        mPageCount = 0;
        initAds();
//        requestNativeAd();//Native Ad's
    }

    private void initAds(){
        if(AdUtils.isPhotosInterstitialEnabled()){
            adFreq = AdUtils.getPhotoInterstitialAdFrequency();
            String adId = ConfigManager.getInstance().getCustomApiUrl(AdConstants.ADS_PHOTO_INTERSTITIAL_ID);  //"/6253334/dfp_example_ad/interstitial"
            if(getActivity() != null) {
                InterstitialAdHelper.getInstance().initInterstitial(getActivity(), adId, new InterstitialAdHelper.InterstitialAdListener() {
                    @Override
                    public void onInterstitialAdLoaded() {

                    }

                    @Override
                    public void onInterstitialAdFailed() {

                    }

                    @Override
                    public void onInterstitialAdClosed() {
                        Log.d("INTERSTITIAL_HELPER", "onAdClosed called");
                        if(getCurrentFragment() != null && !(getCurrentFragment() instanceof FullPhotoFragment) && !bIsFromBckground && !bIsFullPhotoFragment)
                            mPageCount--;
                    }
                });
                InterstitialAdHelper.getInstance().loadInterstitialAd();
            }
        } else{
            requestNativeAd();//Native Ad's
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
        mCurActionBarTitle = mActivity.getActionBarToolbar().getTitle();
        // mVideoCastManager = ((BaseActivity) activity).mCastManager;
        mActivity.getActionBarToolbar().setTitle(section);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_chrome_cast, menu);
        mMediaRouteMenuItem = mCastManager.
                addMediaRouterButton(menu, R.id.media_route_menu_item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.photo_fragment_detail, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initChromeCast();
        setAdRecurringPosition();
        //requestNativeAd();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == 1001) {
            Toast.makeText(getActivity(), R.string.share_success, Toast.LENGTH_LONG).show();
        }
    }

    public String getAlbumTitle() {
        return Html.fromHtml(photoList.getTitle()).toString();
    }

    public String getAlbumId() {
        return Html.fromHtml(photoList.getId()).toString();
    }

    public void setAlbumLinkUrl(String url) {
        mAlbumLinkUrl = url;
    }

    public String getAlbumLinkUrl() {
        return Html.fromHtml(mAlbumLinkUrl).toString();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCastManager.decrementUiCounter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCastManager) {
            mCastManager.clearContext(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mViewPager = (ViewPager) rootView.findViewById(R.id.detail_viewpager);
        mAdapter = new AlbumPagerAdapter(getChildFragmentManager(), photoList.getPhotos(), false, mNavigationPos, mSectionPos, section);
        mViewPager.setAdapter(mAdapter);
        int currentIndex = PreferencesManager.getInstance(getActivity()).getCurrentPhotoIndex(CURRENT_IMAGE_INDEX);
        mViewPager.setCurrentItem(currentIndex);
        mViewPager.setOnPageChangeListener(this);
        if(currentIndex == 0)
            AlbumDetailFragment.this.onPageSelected(currentIndex);

        mCastManager = NdtvApplication.getCastManager();
        mCastManager.incrementUiCounter();
    }

    @Override
    public void setScreenName() {
        String navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPos);
        if (navigation != null)
            setScreenName(navigation + " - " + section + " - Album: " + getAlbumId());
    }


    private void setAdRecurringPosition() {
        mPreviousAdPoSition = Integer.valueOf(ConfigManager.getInstance().getCustomApiUrl(AdConstants.NATIVE_INTERSTITIAL_AD_FREQUENCY)) - 1;
    }

    private boolean isNativeAdsForPhotosEnabled() {
        String status = ConfigManager.getInstance().getCustomApiUrl(AdConstants.NATIVE_AD_STATUS_FORPHOTOS);
        if (AdConstants.ENABLED.equalsIgnoreCase(status))
            return true;
        return false;
    }

    private void requestNativeAd() {
        String nativeSiteID = ConfigManager.getInstance().getCustomApiUrl(AdConstants.NATIVE_INTERSTITIAL_AD_ID);

        if (isNativeAdsForPhotosEnabled() && nativeSiteID != null) {
            NativeAdManager.getNewInstance().downloadNativeAd(getActivity(), new NewsListingFragment.NativeAdListener() {
                @Override
                public void onAdLoadSuccess(NativeContentAd nativeContentAd) {
                    if (nativeContentAd != null) {
                        photoItem = new Photos();
                        photoItem.nativeContentAd = nativeContentAd;
                        photoItem.isAdPage = true;

                        modifyAlbumForLoadingNativeAd();
                    }
                }
            }, nativeSiteID);
        }
    }

    private void modifyAlbumForLoadingNativeAd() {
        mPhotos = photoList.photos;
        if (photoItem != null && getActivity() != null) {
            boolean itemAdded = false;

            if (mPhotos.size() > mPreviousAdPoSition) {

                mPhotos.add(mPreviousAdPoSition, photoItem);
                itemAdded = true;
//                getCurrentFragment();
                Fragment currentFragment = getCurrentFragment();
                if (currentFragment != null && currentFragment instanceof FullPhotoFragment)
                    ((FullPhotoFragment) currentFragment).notifyFullScreenAdapter();
                mAdapter.notifyDataSetChanged();
            }
            //For Next Ad Position
            if (itemAdded) {
                mAdFreqCount = Integer.valueOf(ConfigManager.getInstance().getCustomApiUrl(AdConstants.NATIVE_INTERSTITIAL_AD_FREQUENCY));
                mPreviousAdPoSition += mAdFreqCount;
                requestNativeAd();// Loads Next Ad
            }
        }
    }

    private Fragment getCurrentFragment() {
        if (getActivity() != null) {
            Fragment currentFragment = null;
            if (DeepLinkingManager.getInstance() != null && DeepLinkingManager.getInstance().isFromDeepLink())
                currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.deep_link_frame_body);
            else
                currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);

//            if (currentFragment != null && currentFragment instanceof FullPhotoFragment)
//                ((FullPhotoFragment) currentFragment).notifyFullScreenAdapter();
            return currentFragment;
        }
        return null;
    }

    private void initChromeCast() {
        //mCastManager = NdtvApplication.getVideoCastManager(getActivity());
        setupCastListener();
        mCastManager.addVideoCastConsumer(mCastConsumer);
        // mCastManager.reconnectSessionIfPossible();
    }

    private void setupCastListener() {
        mCastConsumer = new VideoCastConsumerImpl() {

            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata,
                                               String sessionId, boolean wasLaunched) {
                android.util.Log.d(LOG_TAG, "onApplicationLaunched() is reached");
                isCastConnected = true;
                loadMedia();
            }

            @Override
            public void onApplicationDisconnected(int errorCode) {
                super.onApplicationDisconnected(errorCode);
                android.util.Log.d(LOG_TAG, "onApplicationDisconnected() is reached with errorCode: " + errorCode);
                isCastConnected = false;
            }
        };

    }

    private void loadMedia() {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, "NDTV Photos");
        String url = mPhotos.get(mViewPager.getCurrentItem()).getFullimage();
        android.util.Log.d(LOG_TAG, "Content URL sending to chromecast " + url);
        MediaInfo mediaInfo = new MediaInfo.Builder(url).setContentType("image/*").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).build();
        try {
            mCastManager.loadMedia(mediaInfo, true, 0);
        } catch (Exception e) {
            if (e.getMessage() != null)
                Log.d(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity.getActionBarToolbar().setTitle(mCurActionBarTitle);
        mCastManager.disconnect();
        BitmapCache.getInstance((this).getFragmentManager()).clearCache();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d("INTERSTITIAL_HELPER", "onPageSelected called");
        Log.d("INTERSTITIAL_HELPER", "FromBG:" + bIsFromBckground + " FullPhotoFragment:" + bIsFullPhotoFragment);

        if(bIsFromBckground) {
            if(bIsFullPhotoFragment) {
                mPageCount++;
                bIsFullPhotoFragment = false;
            }
            else
                bIsFromBckground = false;
        }
        else
            mPageCount++;

        Log.d("INTERSTITIAL_HELPER", "Photo Count:" + mPageCount);
        if(adFreq > 0 && mPageCount % adFreq == 0 && AdUtils.isPhotosInterstitialEnabled()){ //
            InterstitialAdHelper.getInstance().showInterstitialAd();
        }

        PreferencesManager.getInstance(getActivity()).setCurrentPhotoIndex(ApplicationConstants.PreferenceKeys.CURRENT_IMAGE_INDEX, position);
        PreferencesManager.getInstance(getActivity()).setIsExpanded(PreferencesManager.IS_EXPANDED, false);

        if (mAdUpdateListener != null) {
            if (photoList.getPhotos().get(position) != null && photoList.getPhotos().get(position).isAdPage) {
                mAdUpdateListener.hideIMBannerAd();
            } else {
                if(bIsFromSearch){
                    mAdUpdateListener.loadBannerAd(-1, -1, null, true, position, false, false);
                }
                else
                    mAdUpdateListener.loadBannerAd(mNavigationPos, mSectionPos, null, true, position, false, false);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("INTERSTITIAL_HELPER", "onStop called");
        bIsFromBckground = true;
    }
}
