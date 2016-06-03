package com.ndtv.core.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Navigation;
import com.ndtv.core.config.model.Section;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.cricket.ui.SpecialFragment;
import com.ndtv.core.cricket.ui.WebViewFragment;
import com.ndtv.core.livetv.ui.LiveTvScheduleFragment;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.ui.NewsWidgetFragment;
import com.ndtv.core.notifications.ui.NotificationsFragment;
import com.ndtv.core.radio.ui.LiveRadioFragment;
import com.ndtv.core.ui.NewsListingFragment;
import com.ndtv.core.ui.PhotoListingFragment;
import com.ndtv.core.ui.PlaceHolderFragment;
import com.ndtv.core.video.ui.VideosListingFragment;

import java.util.List;

//import com.july.ndtv.radio.ui.LiveRadioFragment;

/**
 * Created by Srihari S Reddy on 23/12/14.
 */

public class SectionPagerAdapter extends FragmentPagerAdapter implements ApplicationConstants.SectionType {

    private final int mNavigationPos;
    private int mSectionPosition;
    private List<Section> mSections;
    private Fragment mCurrentFragment;

    public SectionPagerAdapter(FragmentManager fm, List<Section> sections, int navigationPos) {
        super(fm);
        mSections = sections;
        mNavigationPos = navigationPos;
    }

    public SectionPagerAdapter(FragmentManager fm, List<Section> sections, int navigationPos, int sectionPos) {
        super(fm);
        mSections = sections;
        mNavigationPos = navigationPos;
        mSectionPosition = sectionPos;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment;
        switch (mSections.get(position).type.toLowerCase()) {
            case NEWS:

                String status = ConfigManager.getInstance().getCustomApiUrl(Constants.BREAKING_WIDGET_STATUS);

                if (status.equalsIgnoreCase("1") && ConfigManager.getInstance().isWidgetAvilableForNavigationAndSection(mNavigationPos, position)
                        /*&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT*/)
                    fragment = NewsWidgetFragment.newInstance(mSections.get(position).url, position, mSections.get(position).getTitle(), mSections.get(position).order, mNavigationPos);
                else
                    fragment = NewsListingFragment.newInstance(mSections.get(position).url, position, mSections.get(position).getTitle(), mSections.get(position).order, mNavigationPos);
                break;
            case PHOTO:
                fragment = PhotoListingFragment.newInstance(mSections.get(position).url, position, mSections.get(position).getTitle(), mNavigationPos);
                break;
            case AUDIO:
                fragment = LiveRadioFragment.newInstance(mSections.get(position).url, position, mSections.get(position).getTitle(), mSections.get(position).schedule, mNavigationPos);
                break;
            case VIDEO:
                fragment = VideosListingFragment.getInstance(mSections.get(position).url, mSections.get(position).getTitle(), mNavigationPos, position);
                break;
            case PLAYER:
                fragment = LiveTvScheduleFragment.getInstance(mSections.get(position).getTitle(), mSections.get(position).getScheduleUrl(), mSections.get(position).getPlayUrl(), mNavigationPos, position);
                break;
            case MICRO:
                Navigation navigation = ConfigManager.getInstance().getNavigation(mNavigationPos);
                String cricketContent = ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CustomApiType.CRICKET_CONTENT_DETAIL);
                if (navigation != null && navigation.title.equalsIgnoreCase("Cricket") && !TextUtils.isEmpty(cricketContent)) {
                    fragment = SpecialFragment.newInstance(mSections.get(position).getTabList(), position, mSections.get(position).getTitle(), mNavigationPos, cricketContent);
                } else {
                    fragment = SpecialFragment.newInstance(mSections.get(position).getTabList(), position, mSections.get(position).getTitle(), mNavigationPos);
                }
                break;
            case PAGE:
                fragment = WebViewFragment.newInstance(mSections.get(position).url, position, mSections.get(position).getTitle(), mNavigationPos, false);
                break;
            case NOTIFICATION:
                fragment = NotificationsFragment.newInstance(position, mSections.get(position).getTitle(), mNavigationPos);
                break;
            default:
                fragment = PlaceHolderFragment.newInstance(mSections.get(position).getTitle());
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        if (mSections != null)
            return mSections.size();
        else
            return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mSections.get(position).getTitle();
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
