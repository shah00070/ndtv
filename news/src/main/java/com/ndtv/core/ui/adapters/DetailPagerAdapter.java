package com.ndtv.core.ui.adapters;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;

import com.ndtv.core.common.util.NewsManager;
import com.ndtv.core.config.model.NewsItems;
import com.ndtv.core.deeplinking.io.OnDeepLinkingInterface;
import com.ndtv.core.ui.NewsDetailFragment;

import java.util.ArrayList;
import java.util.List;

import static com.ndtv.core.util.LogUtils.LOGV;
import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Srihari S Reddy on 26/01/15.
 */
public class DetailPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = makeLogTag(DetailPagerAdapter.class);
    private final int mNavigationPos;
    private final int mSectionPos;
    /*private String mId;
    private String mTitle;
    private String mStoryImage;
    private String mContentUrl;*/
    boolean mIsfromSearchNewsDetail;
    //private int mListSize;
    private List<NewsItems> mNewsSearchData = new ArrayList<>();


    private Cursor mCursor;
    public ArrayList<NewsItems> newsItemsArrayList;

    public DetailPagerAdapter(FragmentManager fm, Cursor cursor, int navigationPos, int sectionPos) {
        super(fm);
        LOGV(TAG, "Setting Detail pager Adapter with " + cursor.getCount() + " pages");
        mCursor = cursor;
        mNavigationPos = navigationPos;
        mSectionPos = sectionPos;
        newsItemsArrayList = new ArrayList<>();
        fillList();
    }

    public DetailPagerAdapter(FragmentManager fm, boolean isfromSearchNewsDetail) {
        super(fm);
        mIsfromSearchNewsDetail = isfromSearchNewsDetail;
        mNewsSearchData = NewsManager.getNewsInstance().mNewsSearchItems;
        mNavigationPos = 0;
        mSectionPos = 0;
    }

    @Override
    public Fragment getItem(int i) {
        if (mIsfromSearchNewsDetail) {
            return NewsDetailFragment.newInstance(mNewsSearchData.get(i).id, mNewsSearchData.get(i).title, mNewsSearchData.get(i).thumb_image, mNewsSearchData.get(i).device);
        } else {
            return NewsDetailFragment.newInstance(newsItemsArrayList.get(i).id, newsItemsArrayList.get(i).title, newsItemsArrayList.get(i).story_image, newsItemsArrayList.get(i).device, mNavigationPos, mSectionPos);

        }
    }

    @Override
    public int getCount() {
        if (mIsfromSearchNewsDetail) {
            return mNewsSearchData.size();
        } else {
            return newsItemsArrayList.size();
        }
    }

    private void fillList() {
        if ((mCursor != null) && (!mCursor.isClosed())) {
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {

                NewsItems ni = new NewsItems();
                ni.id = mCursor.getString(1);
                ni.title = mCursor.getString(2);
                ni.story_image = mCursor.getString(5);
                ni.device=mCursor.getString(mCursor.getColumnIndex("device"));
                ni.link = mCursor.getString(mCursor.getColumnIndex("link"));
                ni.type = mCursor.getString(13);
                ni.category = mCursor.getString(10);
                ni.identifier = mCursor.getString(11);
                ni.applink = mCursor.getString(12);
                if (ni.type == null)
                    ni.type = "";

//                if ((ni.type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_STORY) || ni.type.equalsIgnoreCase("")) || ((TextUtils.isEmpty(ni.applink) && ni.type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_CRICKET_SCORECARD))))
                if(TextUtils.isEmpty(ni.applink))
                    newsItemsArrayList.add(ni);
            }
        }
    }


    public int getActualPostionForNews(int position) {
        if ((mCursor != null) && (!mCursor.isClosed())) {
            mCursor.moveToPosition(position);
            String selectedNewsItemId = mCursor.getString(1);
            for (int index = 0; index < newsItemsArrayList.size(); index++) {
                if (selectedNewsItemId.equalsIgnoreCase(newsItemsArrayList.get(index).id)) {
                    return index;
                }
            }
            return position;
        }
        return position;
    }


}
