package com.ndtv.core.search.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.ndtv.core.R;
import com.ndtv.core.ads.utility.AdUtils;
import com.ndtv.core.ads.utility.InterstitialAdHelper;
import com.ndtv.core.common.util.NewsManager;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.ui.adapters.DetailPagerAdapter;
import com.ndtv.core.ui.widgets.DetailFragment;

/**
 * Created by Ram Prakash on 10/4/15.
 */
public class NewsDetailSearchFragment extends DetailFragment {

    /* private String mId;
     private String mTitle;
     private String mStoryImage;
     private String mContentUrl;
     private int mTotalItemCount;*/
    private int mPosition;
    private int mPagerPosition = 0;

    public static NewsDetailSearchFragment newInstance(int position) {   //String id, String title, String storyImage, String contentUrl,int totalItemCount
        NewsDetailSearchFragment newsDetailSearchFragment = new NewsDetailSearchFragment();
        Bundle args = new Bundle();
       /* args.putString("id", id);
        args.putString("Title", title);
        args.putString("StoryImage", storyImage);
        args.putString("ContentUrl", contentUrl);
        args.putInt("TotalCount",totalItemCount);*/
        args.putInt("Position", position);
        newsDetailSearchFragment.setArguments(args);
        return newsDetailSearchFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mPosition = bundle.getInt("Position");
        /*mId=bundle.getString("id");
        mTitle = bundle.getString("Title");
        mStoryImage = bundle.getString("StoryImage");
        mContentUrl = bundle.getString("ContentUrl");
        mTotalItemCount=bundle.getInt("TotalCount");*/

    }

    @Override
    public void onPageSelected(int position) {
        pageCount++;
        Log.d("INTERSTITIAL_HELPER", "Page Count:" + pageCount);
        int adFreq = AdUtils.getStoriesInterstitialAdFrequency();
        if (adFreq!=0) {
            if (pageCount % adFreq == 0 && AdUtils.isStoriesInterstitialEnabled()) {
                InterstitialAdHelper.getInstance().showInterstitialAd();
            }
        }
        setCurrentPosition(position);
        mPagerPosition = position;
       /* if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(mNavigationPos, mSectionPos, null, false);
        }*/

        if (mActivity != null) {
            mActivity.loadCommentCount();
        }


        boolean isBackFromComment = PreferencesManager.getInstance(getActivity()).getIsBackFromCommentList(IS_BACK_FROM_COMMENT);
        if (!isBackFromComment) {
            if (NewsManager.getNewsInstance().mNewsSearchItems.size() > position) {
                getComments(NewsManager.getNewsInstance().mNewsSearchItems.get(position).identifier);
            }
        } else {
            String currNewsIdentifier = PreferencesManager.getInstance(getActivity()).getNewsIdentifierToHandleBackPress(CURRENT_NEWS_IDENTIFIER);

            if (getIdentifier() != null && getIdentifier().equals(currNewsIdentifier)) {
                PreferencesManager.getInstance(getActivity()).setIsBackFromCommentList(IS_BACK_FROM_COMMENT, false);
                if (NewsManager.getNewsInstance().mNewsSearchItems.size() > position) {
                    getComments(NewsManager.getNewsInstance().mNewsSearchItems.get(position).identifier);
                }
            }
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        boolean isfromSearchNewsDetail = true;
        mViewPager = (ViewPager) view.findViewById(R.id.detail_viewpager);
        mAdapter = new DetailPagerAdapter(getChildFragmentManager(), isfromSearchNewsDetail);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mPosition);
        mViewPager.setOnPageChangeListener(this);
        onPageSelected(mPosition);
    }

    public void notifyAdapter() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public String getContentLink() {
        if (NewsManager.getNewsInstance().mNewsSearchItems.size() > mPagerPosition) {
            return NewsManager.getNewsInstance().mNewsSearchItems.get(mPagerPosition).link;
        } else {
            return " ";
        }
    }

    @Override
    public String getNewsItemID() {
        if (NewsManager.getNewsInstance().mNewsSearchItems.size() > mPagerPosition) {
            return NewsManager.getNewsInstance().mNewsSearchItems.get(mPagerPosition).id;
        } else {
            return " ";
        }
    }

    @Override
    public String getNewsCategory() {
        if (NewsManager.getNewsInstance().mNewsSearchItems.size() > mPagerPosition) {
            return NewsManager.getNewsInstance().mNewsSearchItems.get(mPagerPosition).category;
        } else {
            return " ";
        }
    }

    @Override
    public String getTitle() {
        if (NewsManager.getNewsInstance().mNewsSearchItems.size() > mPagerPosition) {
            return NewsManager.getNewsInstance().mNewsSearchItems.get(mPagerPosition).getTitle();
        } else {
            return " ";
        }
    }

    @Override
    public String getIdentifier() {
        if (NewsManager.getNewsInstance().mNewsSearchItems.size() > mPagerPosition) {
            return NewsManager.getNewsInstance().mNewsSearchItems.get(mPagerPosition).identifier;
        } else {
            return " ";
        }
    }

    public String getSectionTitle() {
        return " ";
    }
}
