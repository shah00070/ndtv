package com.ndtv.core.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.ndtv.core.R;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Navigation;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.ui.adapters.SectionPagerAdapter;
import com.ndtv.core.ui.widgets.SlidingTabLayout;
import com.ndtv.core.ui.widgets.ZoomOutPageTransformer;


public class HomeFragment extends Fragment implements ApplicationConstants.SectionType, ApplicationConstants.NavigationType, ApplicationConstants.BundleKeys {

    public static final String LOG_TAG = "HomeFragment";
    private String mPreviosTitle;
    protected Navigation mNavigation;
    protected SlidingTabLayout mSlidingTabLayout;
    protected ViewPager mViewPager;
    private FragmentPagerAdapter mSectionPagerAdapter;
    private int defSec;
    private int myInt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        myInt = bundle.getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, 0);
        defSec = bundle.getInt(ApplicationConstants.BundleKeys.SECTION_POS, 0);
        mNavigation = ConfigManager.getInstance().getNavigation(myInt);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.section_viewpager);
        setPagerAdapter();

        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                String serachTabsPos = PreferencesManager.getInstance(getActivity()).getSearchTabsSectionPos(PreferencesManager.SEARCH_TABS_POS);

                if (serachTabsPos != null) {
                    String[] serachTabsPosArr = serachTabsPos.split("_");
                    for (int i = 0; i < serachTabsPosArr.length; i++) {

                        String[] posArr = serachTabsPosArr[i].split("-");

                        if (posArr[0].equals("news")) {
                            if (position == Integer.parseInt(posArr[1])) {
                                PreferencesManager.getInstance(getActivity()).setCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN, 0);
                            }
                        }

                        if (posArr[0].equals("photo")) {
                            if (position == Integer.parseInt(posArr[1])) {
                                PreferencesManager.getInstance(getActivity()).setCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN, 1);
                            }
                        }

                        if (posArr[0].equals("video")) {
                            if (position == Integer.parseInt(posArr[1])) {
                                PreferencesManager.getInstance(getActivity()).setCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN, 2);
                            }
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


/*
    @Override
    public void onResume() {
        super.onResume();

        if (mHideActionToolBarInterface != null && PreferencesManager.getInstance(getActivity()).isFromDeepLinking()) {
            mPreviosTitle = mHideActionToolBarInterface.getActionBartitle();
            mHideActionToolBarInterface.setActionBarTitle("");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPreviosTitle != null && PreferencesManager.getInstance(getActivity()).isFromDeepLinking()) {
            mHideActionToolBarInterface.setActionBarTitle(mPreviosTitle);
            //PreferencesManager.getInstance(getActivity()).setIsFromDeepLinking(false); // reset the flag
        }
    }
*/

    //override this method in child class in order to provide different adapter for ViewPager
    protected void setPagerAdapter() {
        mSectionPagerAdapter = new SectionPagerAdapter(getChildFragmentManager(), mNavigation.section, myInt);
        if (mNavigation.type.equals(PHOTOHOME) || mNavigation.type.equals(VIDEOHOME))
            mViewPager.setBackgroundColor(getResources().getColor(R.color.black));
        if (mNavigation.type.equals(LIVETVHOME))
            mViewPager.setBackgroundColor(getResources().getColor(R.color.live_tv_schedule_background));
        mViewPager.setAdapter(mSectionPagerAdapter);
        if (mSectionPagerAdapter.getCount() > defSec) {
            mViewPager.setCurrentItem(defSec);
        }
    }

    public Fragment getCurrentFragment() {
        Fragment currentPage = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.section_viewpager + ":" + mViewPager.getCurrentItem());
        return currentPage;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Added here, to fix the issue that was causing in Radio because of PageTransformer
        mViewPager.setPageTransformer(false, new ZoomOutPageTransformer());
        ((BaseActivity) getActivity()).getActionBarToolbar().setTitle(mNavigation.getTitle());
        try {
            ((BaseActivity) getActivity()).markNavigationItemSelected(ConfigManager.getInstance().getConfiguration().getNavIndex(mNavigation.title));
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }

    }
}
