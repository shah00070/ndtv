package com.ndtv.core.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndtv.core.R;
import com.ndtv.core.ads.utility.AdUtils;
import com.ndtv.core.ads.utility.InterstitialAdHelper;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.model.Photos;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.ui.adapters.AlbumPagerAdapter;
import com.ndtv.core.ui.widgets.BaseFragment;

import java.util.List;

import static com.ndtv.core.util.LogUtils.makeLogTag;


public class FullPhotoFragment extends BaseFragment {


    private static final String TAG = makeLogTag(FullPhotoFragment.class);

    private ViewPager mViewPager;
    private int mPosition;
    private String section;
    private List<Photos> mPhotoList;
    private Photos photoItem;
    private AlbumPagerAdapter mAdapter;

    public static FullPhotoFragment newInstance(int position, List<Photos> photosList, String section) {

        FullPhotoFragment albumFrgmnt = new FullPhotoFragment();
        albumFrgmnt.mPhotoList = photosList;
        albumFrgmnt.mPosition = position;
        albumFrgmnt.section = section;
        return albumFrgmnt;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof HomeActivity)
            ((HomeActivity) getActivity()).lockDrawer();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        bIsFullPhotoFragment = true;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fullimage_pager, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.fullimage_viewpager);
        mAdapter = new AlbumPagerAdapter(getChildFragmentManager(), mPhotoList, true, 0, 0, "");
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mPosition);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                PreferencesManager.getInstance(getActivity()).setCurrentPhotoIndex(ApplicationConstants.PreferenceKeys.CURRENT_IMAGE_INDEX, position);
                mPageCount++;
                Log.d("INTERSTITIAL_HELPER", "Photo Count:" + mPageCount);
                if (AdUtils.getPhotoInterstitialAdFrequency() != 0) {
                    if (mPageCount % AdUtils.getPhotoInterstitialAdFrequency() == 0 && AdUtils.isPhotosInterstitialEnabled())
                        InterstitialAdHelper.getInstance().showInterstitialAd();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void setScreenName() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void notifyFullScreenAdapter() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() instanceof HomeActivity)
            ((HomeActivity) getActivity()).unLockDrawer();
    }
}
