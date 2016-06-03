package com.ndtv.core.ui.widgets;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.ads.utility.AdConstants;
import com.ndtv.core.ads.utility.AdUtils;
import com.ndtv.core.ads.utility.InterstitialAdHelper;
import com.ndtv.core.common.util.NewsManager;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Comments;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.ui.BaseActivity;
import com.ndtv.core.ui.NewsDetailFragment;
import com.ndtv.core.ui.adapters.DetailPagerAdapter;

import static com.ndtv.core.util.LogUtils.LOGE;
import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Srihari S Reddy on 26/01/15.
 */
public class DetailFragment extends BaseFragment implements ViewPager.OnPageChangeListener, ApplicationConstants.CommentConstants, ApplicationConstants.PreferenceKeys {

    static final String LOG_TAG = "DetailFragment";
    private static int mNavigationPos;
    private static int mSectionPos;
    protected ViewPager mViewPager;
    private Cursor mCursor;
    private int mPosition;
    private String mSection;
    protected CharSequence mCurActionBarTitle;
    protected BaseActivity mActivity;
    protected DetailPagerAdapter mAdapter;
    protected BannerAdFragment.AdListener mAdUpdateListener;
    public int currentPos;
    private static final String TAG = makeLogTag(DetailFragment.class);

    public int pageCount = 0;

    public static DetailFragment newInstance(Cursor cursor, int position, String section, int navigationPos, int pos) {

        //TODO refactoring required
        DetailFragment f = new DetailFragment();

        f.mCursor = cursor;
        f.mPosition = position;
        f.mSection = section;
        mNavigationPos = navigationPos;
        mSectionPos = pos;
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().invalidateOptionsMenu();
        this.currentPos = mPosition;

        if(AdUtils.isStoriesInterstitialEnabled()){
            String adId = ConfigManager.getInstance().getCustomApiUrl(AdConstants.ADS_STORIES_INTERSTITIAL_ID); //"/6253334/dfp_example_ad/interstitial"
            if(getActivity() != null) {
                InterstitialAdHelper.getInstance().initInterstitial(getActivity(), adId, new InterstitialAdHelper.InterstitialAdListener() {
                    @Override
                    public void onInterstitialAdLoaded() {

                    }

                    @Override
                    public void onInterstitialAdFailed() {

                    }

                    @Override
                    public void onInterstitialAdClosed() {
                        if(getCurrentFragment() instanceof NewsDetailFragment){
                            Fragment page = getCurrentFragment();
                            if(((NewsDetailFragment)page).vWebView != null) {
                                try {
                                    Class.forName("android.webkit.WebView")
                                            .getMethod("onResume", (Class[]) null)
                                            .invoke(((NewsDetailFragment) page).vWebView, (Object[]) null);


                                } catch (Exception e) {
                                    LOGE(TAG, e.getMessage());
                                }
                            }
                        }
                    }
                });
                InterstitialAdHelper.getInstance().loadInterstitialAd();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
        mCurActionBarTitle = mActivity.getActionBarToolbar().getTitle();
        mActivity.getActionBarToolbar().setTitle(mSection);
        //Banner ads listener
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        mActivity.getActionBarToolbar().setTitle(mCurActionBarTitle);
        if (((ActionBarActivity) getActivity()).getSupportActionBar() != null)
            ((ActionBarActivity) getActivity()).getSupportActionBar().show();
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == 1001) {
            Toast.makeText(getActivity(), R.string.share_success, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.detail_viewpager);
        mAdapter = new DetailPagerAdapter(getChildFragmentManager(), mCursor, mNavigationPos, mSectionPos);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mAdapter.getActualPostionForNews(mPosition));
        mViewPager.setOnPageChangeListener(this);
        DetailFragment.this.onPageSelected(mAdapter.getActualPostionForNews(mPosition));
    }

    public String getContentLink() {
//        mCursor.moveToPosition(getCurrentPosition());
//        return mCursor.getString(9);
        if (mAdapter != null && mAdapter.newsItemsArrayList != null) {
            if (getCurrentPosition() < mAdapter.newsItemsArrayList.size())
                return mAdapter.newsItemsArrayList.get(getCurrentPosition()).device;
            else
                return null;
        } else
            return null;
    }

    public String getShareContentLink() {
        if (mAdapter != null && mAdapter.newsItemsArrayList != null) {
            if (getCurrentPosition() < mAdapter.newsItemsArrayList.size())
                return mAdapter.newsItemsArrayList.get(getCurrentPosition()).link;
            else
                return null;
        } else
            return null;
    }
    public String getNewsItemID() {
        if (getCurrentPosition() < mCursor.getCount()) {
//            mCursor.moveToPosition(getCurrentPosition());
//            return mCursor.getString(1);
            if (mAdapter != null && mAdapter.newsItemsArrayList != null) {
                if (getCurrentPosition() < mAdapter.newsItemsArrayList.size())
                    return mAdapter.newsItemsArrayList.get(getCurrentPosition()).id;
                else
                    return null;
            } else
                return null;
        } else {
            return null;
        }
    }

    public String getNewsCategory() {
//        mCursor.moveToPosition(getCurrentPosition());
//        return mCursor.getString(10);
        if (mAdapter != null && mAdapter.newsItemsArrayList != null) {
            if (getCurrentPosition() < mAdapter.newsItemsArrayList.size())
                return mAdapter.newsItemsArrayList.get(getCurrentPosition()).category;
            else
                return null;
        } else
            return null;
    }

    public String getTitle() {
//        mCursor.moveToPosition(getCurrentPosition());
//        return mCursor.getString(2);
        if (mAdapter != null && mAdapter.newsItemsArrayList != null) {
            if (getCurrentPosition() < mAdapter.newsItemsArrayList.size())
                return mAdapter.newsItemsArrayList.get(getCurrentPosition()).title;
            else
                return null;
        } else
            return null;
    }

    public String getIdentifier() {
//            mCursor.moveToPosition(getCurrentPosition());
//            return mCursor.getString(11);
        if (mAdapter != null && mAdapter.newsItemsArrayList != null) {
            if (getCurrentPosition() < mAdapter.newsItemsArrayList.size())
                return mAdapter.newsItemsArrayList.get(getCurrentPosition()).identifier;
            else
                return null;
        } else
            return null;

    }

    public String getSectionTitle() {
        return mSection;
    }

    public Fragment getCurrentFragment() {
        Fragment currentPage = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.detail_viewpager + ":" + mViewPager.getCurrentItem());
        return currentPage;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setScreenName() {

    }

    public int getCurrentPosition() {
        return currentPos;
    }

    public void setCurrentPosition(int currentPos) {
        this.currentPos = currentPos;
    }

    public boolean handleBackPress() {
        int i = mViewPager.getCurrentItem();
        if (mViewPager.getAdapter().getCount() > 0) {
            if (mViewPager.getAdapter().instantiateItem(mViewPager, i) instanceof NewsDetailFragment)
                return ((NewsDetailFragment) mViewPager.getAdapter().instantiateItem(mViewPager, i)).handleBackPress();
        }
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            if (mAdUpdateListener != null) {
//                mAdUpdateListener.loadBannerAd(mNavigationPos, mSectionPos, null, false, -1);
//            }
//        }
    }

    @Override
    public void onPageSelected(int position) {
        pageCount++;
        int adFreq = AdUtils.getStoriesInterstitialAdFrequency();
        if (adFreq!=0) {
            if (pageCount % adFreq == 0 && AdUtils.isStoriesInterstitialEnabled()) {
                InterstitialAdHelper.getInstance().showInterstitialAd();
            }
        }
        setCurrentPosition(position);

        if (mActivity != null) {
            mActivity.loadCommentCount();
        }
        boolean isBackFromComment = PreferencesManager.getInstance(getActivity()).getIsBackFromCommentList(IS_BACK_FROM_COMMENT);
        if (!isBackFromComment && getIdentifier() != null)
            getComments(getIdentifier());
        else {
            String currNewsIdentifier = PreferencesManager.getInstance(getActivity()).getNewsIdentifierToHandleBackPress(CURRENT_NEWS_IDENTIFIER);
            if (getIdentifier() != null && getIdentifier().equals(currNewsIdentifier)) {
                PreferencesManager.getInstance(getActivity()).setIsBackFromCommentList(IS_BACK_FROM_COMMENT, false);
                getComments(currNewsIdentifier);
            }
        }

        if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(mNavigationPos, mSectionPos, null, false, -1, false, false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void getComments(String storyIdentifier) {
        String url = ConfigManager.getInstance().getCustomApiUrl(GET_COMMENTS_API);
        String strToReplace[] = new String[]{"@identifier"};
        String strForStoryId = storyIdentifier;
        String replacement[] = new String[]{strForStoryId};
        url = Utility.getFinalUrl(strToReplace, replacement, url, getActivity());
        downloadComments(url);
    }

    protected void downloadComments(String url) {

        NewsManager.getNewsInstance().downloadComments(getActivity(), url, new CommentsDownloadListener() {


            @Override
            public void onDownloadFailed() {
                if (mActivity != null) {
                    mActivity.setCommentCount("0");
                }

            }

            @Override
            public void onDownloadComplete(Comments comments) {
                if (comments != null && mActivity != null) {
                    mActivity.setCommentCount(comments.pager.count_with_replies.toString());
                }
//                if (mAdUpdateListener != null) {
//                    mAdUpdateListener.loadBannerAd(mNavigationPos, mSectionPos, null, false, -1);
//                }
            }
        });
    }
}
