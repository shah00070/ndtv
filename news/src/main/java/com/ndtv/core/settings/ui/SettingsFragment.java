package com.ndtv.core.settings.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Navigation;
import com.ndtv.core.config.model.Section;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.settings.adapter.SettingsPagerAdapter;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.ui.widgets.SlidingTabLayout;

import java.util.List;

/**
 * Created by sangeetha on 13/2/15.
 */
public class SettingsFragment extends BaseFragment {

    private static final String TAG = "SettingsFragment ";
    private ViewPager mSettingsPager;
    private SlidingTabLayout mSlidingTabLayout;

    private int mNavigationPosition;
    private int mSectionPos;
    private BannerAdFragment.AdListener mAdUpdateListener;


    public static interface SettingsConstants {
        String OTHER_APPS = "list-other-apps";
        String NOTIFICATIONS = "notifications";
        String SIGN_IN = "signin";
        String FEEDBACK = "feedback";
        String PAGE_TYPE = "page";
        String SECTION_TITLE = "section_title";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractArguments();
        if(mAdUpdateListener != null)
            mAdUpdateListener.hideIMBannerAd();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mSettingsPager = (ViewPager) view.findViewById(R.id.section_viewpager);
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
    }

    private void extractArguments() {
        if (null != getArguments())
            mNavigationPosition = getArguments().getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS);
        mSectionPos = getArguments().getInt(ApplicationConstants.BundleKeys.SECTION_POSITION);//Sri189
    }

    @Override
    public void setScreenName() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createSettingsPagerAndTab();
    }

    private void createSettingsPagerAndTab() {
        Navigation navigation = ConfigManager.getInstance().getNavigation(mNavigationPosition);
        if (navigation != null) {
            List<Section> sectionsList = navigation.getSectionList();
            if (null != sectionsList && sectionsList.size() > 0) {
                for (Section section : sectionsList) {
                    if (SettingsConstants.OTHER_APPS.equalsIgnoreCase(section.type)) {
                        sectionsList.remove(section);
                        break;
                    }
                }
            }

            SettingsPagerAdapter settingsPagerAdapter = new SettingsPagerAdapter(getChildFragmentManager(),
                    sectionsList, mNavigationPosition, getActivity());
            mSettingsPager.setAdapter(settingsPagerAdapter);
            mSlidingTabLayout.setViewPager(mSettingsPager);
            mSettingsPager.setCurrentItem(mSectionPos);
            //mSettingsPager.setCurrentItem(0);
        }
    }

    public Fragment getCurrentFragment() {
        Fragment fragment = null;
        if (mSettingsPager != null) {
            SettingsPagerAdapter settingsPagerAdapter = (SettingsPagerAdapter) mSettingsPager.getAdapter();
            fragment = settingsPagerAdapter.getCurrentFragment();
        }
        return fragment;
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
}
