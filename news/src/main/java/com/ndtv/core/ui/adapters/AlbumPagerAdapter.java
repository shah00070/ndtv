package com.ndtv.core.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ndtv.core.config.model.Photos;
import com.ndtv.core.ui.FullPhotoDetailFragment;
import com.ndtv.core.ui.PhotoDetailFragment;

import java.util.List;

import static com.ndtv.core.util.LogUtils.LOGV;
import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Chandan kumar on 29/01/15.
 */
public class AlbumPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = makeLogTag(AlbumPagerAdapter.class);
    private final int mNavigationPos;
    private final int mSectionPos;
    private List<Photos> photoList;
    private boolean isFullScreen;
    private String section;
    // private int totalPhotoCount;

    public AlbumPagerAdapter(FragmentManager fm, List<Photos> photoList, boolean isFullScreen, int navigationPos, int sectionPos, String section) {
        super(fm);
        this.photoList = photoList;
        this.isFullScreen = isFullScreen;
        this.section = section;
        this.mNavigationPos = navigationPos;
        this.mSectionPos = sectionPos;
        LOGV(TAG, "Setting Album pager Adapter with " + photoList.size() + " pages");
    }

    @Override
    public Fragment getItem(int i) {
        if (isFullScreen)
            return FullPhotoDetailFragment.newInstance(photoList, i);
        else
            return PhotoDetailFragment.newInstance(photoList, i, getCount(), mNavigationPos, mSectionPos, section);
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return photoList.size();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}

