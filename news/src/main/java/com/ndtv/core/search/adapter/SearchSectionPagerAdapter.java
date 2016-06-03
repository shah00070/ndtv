package com.ndtv.core.search.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ndtv.core.R;
import com.ndtv.core.search.ui.NewsListingSearchFragment;
import com.ndtv.core.search.ui.PhotoListingSearchFragment;
import com.ndtv.core.search.ui.TvShowsSearchFragment;
import com.ndtv.core.search.ui.VideoListingSearchFragment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Ram Prakash on 8/4/15.
 */
public class SearchSectionPagerAdapter extends FragmentPagerAdapter {
    private static final int NEWS = 0;
    private static final int PHOTO = 1;
    private static final int VIDEO = 2;
    private static final int TV_SHOWS = 3;

    private List<String> mTabSectionList;
    private String mSearchText;
    public int currentPos;
    private Context context;


    public SearchSectionPagerAdapter(FragmentManager fm, Context context, String searchText) {
        super(fm);
        mSearchText = searchText;
        this.context = context;
        mTabSectionList = Arrays.asList(context.getResources().getStringArray(
                R.array.search_headers));
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment;
        switch (position) {   //mTabSectionList.get(position).toLowerCase()
            case NEWS:
                fragment = NewsListingSearchFragment.newInstance(mSearchText);
                currentPos = NEWS;
                //((NewsListingSearchFragment) fragment).setSearchText(mSearchText);
                break;
            case PHOTO:
                fragment = PhotoListingSearchFragment.newInstance(mSearchText);
                currentPos = PHOTO;
                //((PhotoListingSearchFragment) fragment).setSearchText(mSearchText);
                break;
            case VIDEO:
                fragment = VideoListingSearchFragment.newInstance(mSearchText);
                currentPos = VIDEO;
                // ((VideoListingSearchFragment) fragment).setSearchText(mSearchText);
                break;
            case TV_SHOWS:
                fragment = TvShowsSearchFragment.newInstance(mSearchText);
                currentPos = TV_SHOWS;
                // ((VideoListingSearchFragment) fragment).setSearchText(mSearchText);
                break;
            default:
                fragment = NewsListingSearchFragment.newInstance(mSearchText);
                currentPos = NEWS;
                //((NewsListingSearchFragment) fragment).setSearchText(mSearchText);  //News Listing
        }
        return fragment;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    @Override
    public int getCount() {
        return mTabSectionList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabSectionList.get(position);
    }
}
