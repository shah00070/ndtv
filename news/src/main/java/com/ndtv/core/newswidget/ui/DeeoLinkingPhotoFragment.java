package com.ndtv.core.newswidget.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.util.URLUtility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Albums;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.ui.BaseActivity;
import com.ndtv.core.ui.adapters.AlbumPagerAdapter;
import com.ndtv.core.ui.widgets.BaseFragment;

/**
 * Created by Harisha B on 24/2/15.
 */
public class DeeoLinkingPhotoFragment extends BaseFragment implements ViewPager.OnPageChangeListener{

    private static final String LOG_TAG = "Photo Deeplinking";
    private Albums mAlbums;
    private ViewPager mViewPager;
    private AlbumPagerAdapter mAdapter;
    //From DeepLinking(Twitter, google..)
    private Boolean mIsDeepLinkFlag;
    private String mPreTitle;
    private String albumUrl;
    private BannerAdFragment.AdListener mAdUpdateListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //From DeepLinking(Twitter, google..)
        extractArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.photo_fragment_detail, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.detail_viewpager);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setScreenName() {
        if (albumUrl != null)
            setScreenName(LOG_TAG + " - " + albumUrl);
        else
            setScreenName(LOG_TAG);

    }

    private void extractArguments() {
        if (getArguments() != null)
            mIsDeepLinkFlag = getArguments().getBoolean(ApplicationConstants.BundleKeys.IS_DEEPLINK_URL);
    }

    /*private String generateDeeplinkUrl() {
        String photoDetailApi = ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CustomApiType.PHOTO_DETAIL_API);
        String appLink = getArguments().getString(Constants.DEEP_LINK_URL);

        String albumId = ConfigManager.getDeeplinkingId(appLink);
        String argKeys[] = {ApplicationConstants.UrlKeys.DEEPLINK_PHOTO_ALBUM_ID},
                albumValues[] = {albumId};

        String photoAlbumUrl = URLUtility.getFinalUrl(argKeys, albumValues, photoDetailApi, getActivity());
        return photoAlbumUrl;
    }*/

/*    @Override
    public void onResume() {
        super.onResume();
        mPreTitle = mHideActionToolBarInterface.getActionBartitle();
        mHideActionToolBarInterface.setActionBarTitle("");
    }

    @Override
    public void onPause() {
        super.onPause();
        mHideActionToolBarInterface.setActionBarTitle(mPreTitle);
    }*/

    /*public void downloadAlbum(String photoAlbumUrl, Context context) {
        NewsWidgetManager.getInstance().downloadAlbum(context, photoAlbumUrl, new Response.Listener<Albums>() {
            @Override
            public void onResponse(Albums albums) {
                setAdapter(albums);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity(), volleyError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    public void setAdapter(Albums albums) {
//        mAlbums = albums;
        mAdapter = new AlbumPagerAdapter(getChildFragmentManager(), mAlbums.results.getPhotos(), false, 0, 0, "");
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
//        mViewPager.setOnPageChangeListener(this);
        //This is to handle back press, if came from Twitter,Facebook,..
//        if (DeepLinkingManager.getInstance() != null && DeepLinkingManager.getInstance().isFromDeepLink())
//            mViewPager.setCurrentItem(DeepLinkingManager.getInstance().geItemPos());
//        else {
        if (NewsWidgetManager.getInstance().getmFullPhotoClickPosition() > 0) {
            mViewPager.setCurrentItem(NewsWidgetManager.getInstance().getmFullPhotoClickPosition());
        } else {
            mViewPager.setCurrentItem(0);
            DeeoLinkingPhotoFragment.this.onPageSelected(0);
        }

//        }

    }

    public Albums getAlbums() {
        return mAlbums;
    }

    public void setAlbums(Albums albums){
        mAlbums = albums;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setAdapter(mAlbums);

        /*if (getArguments() != null) {
            if (mIsDeepLinkFlag)
                albumUrl = getArguments().getString(ApplicationConstants.BundleKeys.DEEP_LINK_URL);
            else
                albumUrl = generateDeeplinkUrl();
            downloadAlbum(albumUrl);
        }*/

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        PreferencesManager.getInstance(getActivity()).setCurrentPhotoIndex(ApplicationConstants.PreferenceKeys.CURRENT_IMAGE_INDEX, position);
        if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(-1, -1, null, true, position, false, false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
