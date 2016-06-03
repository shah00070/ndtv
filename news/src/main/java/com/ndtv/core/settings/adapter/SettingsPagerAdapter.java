package com.ndtv.core.settings.adapter; /**
 Project      : Awaaz
 Filename     : SettingsPagerAdapter.java
 Author       : praveenk
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Section;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.cricket.ui.WebViewFragment;
import com.ndtv.core.settings.ui.AccountsFragment;
import com.ndtv.core.settings.ui.FeedBackFragment;
import com.ndtv.core.settings.ui.NotificationFragment;
import com.ndtv.core.settings.ui.SettingsFragment;
import com.ndtv.core.ui.PlaceHolderFragment;

import java.util.List;


/**
 * @author praveenk
 * @modified nagaraj
 */
public class SettingsPagerAdapter extends FragmentStatePagerAdapter {

    private List<Section> mSectionList;
    private int mNavigationPosition;
    private Context mContext;
    private Fragment mCurrentFragment;

    public SettingsPagerAdapter(FragmentManager fm, List<Section> sectionList, int navigationPosition, Context ctx) {
        super(fm);
        mSectionList = sectionList;
        mNavigationPosition = navigationPosition;
        mContext = ctx;

    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (null != mSectionList && mSectionList.size() > position) {

            if (SettingsFragment.SettingsConstants.NOTIFICATIONS.equalsIgnoreCase(mSectionList.get(position).type)) {
                if (ConfigManager.getInstance().isEnglish(mSectionList.get(position).title))
                    return mSectionList.get(position).getTitle();
                else
                    return mSectionList.get(position).title;

            }
            return mSectionList.get(position).getTitle();
        }
        return "";
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (null != mSectionList && mSectionList.size() > position) {
            if (SettingsFragment.SettingsConstants.SIGN_IN.equalsIgnoreCase(mSectionList.get(position).type)) {
                fragment = new AccountsFragment();
                final Bundle bundle = new Bundle();
                bundle.putString(SettingsFragment.SettingsConstants.SECTION_TITLE, mSectionList.get(position).getTitle());
                bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, mNavigationPosition);
                fragment.setArguments(bundle);
                return fragment;

            } else if (SettingsFragment.SettingsConstants.PAGE_TYPE.equalsIgnoreCase(mSectionList.get(position).type)) {
                fragment = new WebViewFragment().newInstance(mSectionList.get(position).getSectionUrl(), position, mSectionList.get(position).getTitle(), mNavigationPosition, true);
                return fragment;
            } else if (SettingsFragment.SettingsConstants.NOTIFICATIONS.equalsIgnoreCase(mSectionList.get(position).type)) {

                fragment = new NotificationFragment();
                final Bundle bundle = new Bundle();
                bundle.putString(SettingsFragment.SettingsConstants.SECTION_TITLE, mSectionList.get(position).getTitle());
                bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, mNavigationPosition);
                fragment.setArguments(bundle);
                return fragment;
            } else if (SettingsFragment.SettingsConstants.FEEDBACK.equalsIgnoreCase(mSectionList.get(position).type)) {
                fragment = new FeedBackFragment();
                final Bundle bundle = new Bundle();
                bundle.putString(SettingsFragment.SettingsConstants.SECTION_TITLE, mSectionList.get(position).getTitle());
                bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, mNavigationPosition);
                fragment.setArguments(bundle);
                return fragment;


            }
        }
        return new PlaceHolderFragment();

    }
    //			if (PAGE_TYPE.equalsIgnoreCase(mSectionList.get(position).type)) {
    //				final WebPageFragment webFragment = new WebPageFragment();
    //				final Bundle bundle = new Bundle();
    //				bundle.putInt(SECTION_POSITION, position);
    //				bundle.putInt(NAVIGATION_POS, mNavigationPosition);
    //				bundle.putBoolean(IS_SETTING, true);
    //				webFragment.setArguments(bundle);
    //				return webFragment;
    //			} else if (FEEDBACK.equalsIgnoreCase(mSectionList.get(position).type)) {
//				final FeedBackFragment fragment = new FeedBackFragment();
//				final Bundle bundle = new Bundle();
//				bundle.putInt(SECTION_POSITION, position);
//				bundle.putInt(NAVIGATION_POS, mNavigationPosition);
//				fragment.setArguments(bundle);
//				return fragment;
//			} else if (SIGN_IN.equalsIgnoreCase(mSectionList.get(position).type)) {
//				final AccountsFragment fragment = new AccountsFragment();
//				final Bundle bundle = new Bundle();
//				bundle.putInt(SECTION_POSITION, position);
//				bundle.putInt(NAVIGATION_POS, mNavigationPosition);
//				fragment.setArguments(bundle);
//				return fragment;
//			} else if (HELP.equalsIgnoreCase(mSectionList.get(position).type)) {
//				//return new HelpFragment();
//			} else if (CACHING.equalsIgnoreCase(mSectionList.get(position).type)) {
//				final AppSettingsFragment fragment = new AppSettingsFragment();
//				final Bundle bundle = new Bundle();
//				bundle.putInt(SECTION_POSITION, position);
//				bundle.putInt(NAVIGATION_POS, mNavigationPosition);
//				fragment.setArguments(bundle);
//				return fragment;
//			} else

//		return new PlaceHolderFragment();
//	}

    @Override
    public int getCount() {
        if (null != mSectionList)
            return mSectionList.size();
        return 0;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        ((Fragment) object).getFragmentManager()
                .beginTransaction()
                .remove(((Fragment) object))
                .commitAllowingStateLoss();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mCurrentFragment = (Fragment) object;
        super.setPrimaryItem(container, position, object);
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

}
