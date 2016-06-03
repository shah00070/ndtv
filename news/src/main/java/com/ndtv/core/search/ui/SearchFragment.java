package com.ndtv.core.search.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndtv.core.R;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.search.adapter.SearchSectionPagerAdapter;
import com.ndtv.core.ui.BaseActivity;
import com.ndtv.core.ui.widgets.SlidingTabLayout;

/**
 * Created by Ram Prakash on 8/4/15.
 */
public class SearchFragment extends Fragment {

    protected SlidingTabLayout mSlidingTabLayout;
    protected ViewPager mViewPager;
    private FragmentPagerAdapter mSearchPagerAdapter;
    private String mSearchText;
    public BaseActivity mActivity;


    public static Fragment newInstance(String searchText) {
        Fragment searchFragment = new SearchFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("Search_Text", searchText);
        searchFragment.setArguments(bundle);
        return searchFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PreferencesManager.getInstance(getActivity()).setCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN, 0);
        PreferencesManager.getInstance(getActivity()).setSearchTabsSectionPos(PreferencesManager.SEARCH_TABS_POS, null);
        PreferencesManager.getInstance(getActivity()).setIsFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER, true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        mSearchText = bundle.getString("Search_Text");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.section_viewpager);
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        setPagerAdapter();
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    protected void setPagerAdapter() {
        mSearchPagerAdapter = new SearchSectionPagerAdapter(getChildFragmentManager(), getActivity(), mSearchText);
        mViewPager.setAdapter(mSearchPagerAdapter);
        int searchPos = PreferencesManager.getInstance(getActivity()).getCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN);
        mViewPager.setCurrentItem(searchPos);
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int currPos = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                PreferencesManager.getInstance(getActivity()).setCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN, currPos);
            }
        });
    }


    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }
}


