package com.ndtv.core.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeContentAd;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.ads.utility.AdConstants;
import com.ndtv.core.ads.utility.NativeAdManager;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.NewsItems;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.deeplinking.io.OnDeepLinkingInterface;
import com.ndtv.core.newswidget.ui.CirclePageIndicator;
import com.ndtv.core.newswidget.ui.custom.CustomListView;
import com.ndtv.core.provider.ContentProviderUtils;
import com.ndtv.core.provider.NewsContract;
import com.ndtv.core.sync.SyncHelper;
import com.ndtv.core.ui.adapters.NewsListAdapter;
import com.ndtv.core.ui.listener.EndlessScrollListener;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.ui.widgets.DetailFragment;
import com.ndtv.core.util.DetachableResultReceiver;

import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.LOGE;
import static com.ndtv.core.util.LogUtils.LOGV;

/**
 * Created by Srihari S Reddy on 13/01/15.
 */
public class NewsListingFragment extends BaseFragment implements com.ndtv.core.util.DetachableResultReceiver.Receiver, LoaderManager.LoaderCallbacks<Cursor>, CirclePageIndicator.RefreshCalledListener {

    private static final String TAG = "NewsListing ";
    private final AdNativeConsumptionListener mAdConsumptionListener;
    //    private final AdNativeConsumptionListener mAdConsumptionListener;

    protected boolean mIsFetching;
    protected SyncHelper mSyncHelper;
    protected boolean isCacheRequest = false;
    private int mPage = 1;
    protected String feedUrl;
    protected DetachableResultReceiver mReceiver;
    protected View rootView;

    protected NewsListAdapter mAdapter;
    private Cursor mCursor;
    protected CustomListView mListView;
    protected SwipeRefreshLayout mSwipeView;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private NewsListingFragment mNewsListingFragment;

    private int pos;
    private String section;
    private String navigation;
    protected int order;
    protected View mFooterView;
    protected boolean mIsAlreadyLoaded;
    /*This "mProgressBar" is used in both NewsWidgetFragment and NewsListingFragment  to indicate data is loading*/
    protected ProgressBar mProgressBar, mContProgressBar;
    private SectionNewsClickListener mSectionNewsClickListener;

    public interface SectionNewsClickListener {
        void onClickOfSectionNews(String url, String type);
    }

    private static final String[] PROJECTION = new String[]{
            NewsContract.NewsItems._ID,
            NewsContract.NewsItems.NEWS_ITEM_ID,
            NewsContract.NewsItems.NEWS_ITEM_TITLE,
            NewsContract.NewsItems.NEWS_ITEM_UPDATED_AT,
            NewsContract.NewsItems.NEWS_ITEM_THUMB_IMAGE,
            NewsContract.NewsItems.NEWS_ITEM_STORY_IMAGE,
            NewsContract.NewsItems.NEWS_ITEM_DEVICE,
            NewsContract.NewsItems.NEWS_ITEM_TAG,
            NewsContract.NewsItems.NEWS_ITEM_TAG_COLOR,
            NewsContract.NewsItems.NEWS_ITEM_LINK,
            NewsContract.NewsItems.NEWS_ITEM_CATEGORY,
            NewsContract.NewsItems.NEWS_ITEM_IDENTIFIER,
            NewsContract.NewsItems.NEWS_ITEM_APPLINK,
            NewsContract.NewsItems.NEWS_ITEM_TYPE
    };

    protected static final String SELECTION = new StringBuffer(NewsContract.NewsItems.NEWS_ITEM_SECTION).append("=?").toString();
    private NewsItems mAdNewsItem;
    private int mNavigationPos;
    private int mPreviousAdPoSition;
    private BannerAdFragment.AdListener mAdUpdateListener;

    protected void requestFeed(DetachableResultReceiver receiver, String url, int i) {
        mIsFetching = true;
        mIsAlreadyLoaded = true;

        if (i == 1 /*&& order == 1*/)
            clearData();

        mSyncHelper.requestFeed(receiver, url, i, isCacheRequest, navigation + section);
    }

    private void clearData() {
        LOGD(TAG, "Iam called cleardata");
        try {
            getActivity().getContentResolver().delete(NewsContract.NewsItems.CONTENT_URI, SELECTION, new String[]{navigation + section});
        } catch (Exception ex) {
            LOGE(TAG, ex.getMessage());
        }

    }

    @Override
    public boolean onRefreshcalled() {
        if (mSwipeView != null) {
            return mSwipeView.isRefreshing();
        } else {
            return false;
        }
    }

    public interface NativeAdListener {
        void onAdLoadSuccess(NativeContentAd nativeContentAd);
    }

    public static Fragment newInstance(String url, int position, String title, int order, int navigationPos) {
        NewsListingFragment newsListingFragment = new NewsListingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("com.july.ndtv.EXTRA_FEED_SLUG", url);
        bundle.putInt("position", position);
        bundle.putString("section", title);
        bundle.putInt("order", order);
        bundle.putInt("navigation_positon", navigationPos);
        newsListingFragment.setArguments(bundle);
        return newsListingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            feedUrl = bundle.getString("com.july.ndtv.EXTRA_FEED_SLUG");
            pos = bundle.getInt("position");
            section = bundle.getString("section");
            order = bundle.getInt("order");
            LOGD(TAG, "ia m order in oncreate" + order);
            mNavigationPos = bundle.getInt("navigation_positon");
        }
        navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPos);
        mCallbacks = this;
        mNewsListingFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_news_listing, container, false);
        mFooterView = inflater.inflate(R.layout.list_footer, null, false);
        mContProgressBar = (ProgressBar) mFooterView.findViewById(R.id.cont_loader);
        mSyncHelper = SyncHelper.getSyncHelper(getActivity());
        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        return rootView;
    }

    protected void setNewsAdapter() {

        if (mFooterView != null) {
            mContProgressBar.setVisibility(View.INVISIBLE);
            if (mListView.getFooterViewsCount() == 0)
                mListView.addFooterView(mFooterView);
        }


        if (mListView != null) {
            mAdapter = new NewsListAdapter(getActivity(), null, true, mAdConsumptionListener);

            mListView.setAdapter(mAdapter);
        } else {
            Toast.makeText(getActivity(), R.string.empty_string, Toast.LENGTH_SHORT).show();
        }
    }

    public NewsListingFragment() {
        mAdConsumptionListener = new AdNativeConsumptionListener();
    }

    public static <T> void initLoader(final int loaderId, final Bundle args, final LoaderManager.LoaderCallbacks<T> callbacks,
                                      final LoaderManager loaderManager) {
        final Loader<T> loader = loaderManager.getLoader(loaderId);
        if (loader != null && loader.isReset()) {
            LOGD(TAG, "restarting loader");
            loaderManager.restartLoader(loaderId, args, callbacks);
        } else {
            LOGD(TAG, "initing loader");
            loaderManager.initLoader(loaderId, args, callbacks);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = (CustomListView) rootView.findViewById(R.id.news_list);
        //mListView.setFastScrollEnabled(true);
        //mListView.setScrollbarFadingEnabled(true);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        setNewsAdapter();


        initLoader(pos, null, mCallbacks, getLoaderManager());

        swipeToRefresh(true);
        infiniteScroll();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCursor.getPosition() == position) {
                    ///mCursor.move()
                }
                if (mDetailFragmentListener != null)
                    position = position - mListView.getHeaderViewsCount();

                PreferencesManager.getInstance(getActivity()).setIsBackFromCommentList(ApplicationConstants.PreferenceKeys.IS_BACK_FROM_COMMENT, false);

                handleNewsItemClick(position);
            }
        });

        setAdRecurringPosition();
    }

    private void handleNewsItemClick(int position) {
        Cursor deeplinkCursor = mCursor;
        try {

            deeplinkCursor.moveToPosition(position);


            String newsItemType = deeplinkCursor.getString(13);
            String appLink = deeplinkCursor.getString(12);
            String id = deeplinkCursor.getString(1);

            if (!TextUtils.isEmpty(newsItemType) && !newsItemType.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_STORY)) {
                //            Toast.makeText(getActivity(), "AppLink:" + appLink, Toast.LENGTH_SHORT).show();
                if (appLink != null) {
                    ContentProviderUtils.updateReadStatus(getActivity(), id);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    } else
                        mAdapter.notifyDataSetChanged();
                }
            }

            if (!TextUtils.isEmpty(appLink)) {
                mSectionNewsClickListener.onClickOfSectionNews(appLink, newsItemType);
            } else
                mDetailFragmentListener.onAddDetailFragment(DetailFragment.newInstance(mCursor, position, section, mNavigationPos, pos), mNewsListingFragment.getClass().getName());
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
        }
    }

    private void requestNativeAd() {
        String nativeSiteID = ConfigManager.getInstance().getCustomApiUrl(AdConstants.NATIVE_AD_SITE_ID);

        if (isNativeAdEnabled() && nativeSiteID != null) {
            NativeAdManager.getNewInstance().downloadNativeAd(getActivity(), new NativeAdListener() {
                @Override
                public void onAdLoadSuccess(NativeContentAd nativeContentAd) {
                    boolean itemAdded = false;
                    if (mAdapter != null && nativeContentAd != null) {
                        mAdapter.setContentAd(nativeContentAd);
                        itemAdded = true;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        } else
                            mAdapter.notifyDataSetChanged();
                    }
                }
            }, nativeSiteID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mProgressBar != null && !mContProgressBar.isShown()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mIsFetching = true;
        }
        if (!mIsAlreadyLoaded) {
            requestFeed(mReceiver, feedUrl, mPage);
        } else {
            mIsFetching = false;
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
            }
        }
        boolean isFromNavDrawer = PreferencesManager.getInstance(getActivity()).isFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER);
        if (isFromNavDrawer) {
            PreferencesManager.getInstance(getActivity()).setCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN, 0);
            PreferencesManager.getInstance(getActivity()).setIsFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER, false);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (null != getActivity() && isVisibleToUser) {
            loadBannerAds();
            requestNativeAd();
        }
    }

    @Override
    public void setScreenName() {
        setScreenName(navigation + " - " + section);
    }

    @Override
    public void onReceiveResult(int i, Bundle bundle) {
        LOGV(TAG, "onReceiveResult for page " + mPage + " of " + feedUrl);

        if (mSwipeView.isRefreshing())
            mSwipeView.setRefreshing(false);
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
            mIsFetching = false;
        }

        if (!bundle.getBoolean("more", true)) {
            mListView.setOnScrollListener(null);
            mContProgressBar.setVisibility(View.INVISIBLE);
            //mListView.removeFooterView(mFooterView);
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        } else
            mAdapter.notifyDataSetChanged();
    }

    private String getSortString() {
        return NewsContract.NewsItems.NEWS_ITEM_POSITION + " asc";
              /*if (order == 1)
                  return NewsContract.NewsItems.NEWS_ITEM_POSITION + " asc";
              else
                  return NewsContract.SyncColumns.UPDATED + " desc";*/
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        LOGV(TAG, "ON Create Loader - " + NewsContract.NewsItems.CONTENT_URI);
        return new CursorLoader(getActivity(),
                NewsContract.NewsItems.CONTENT_URI,
                PROJECTION,
                SELECTION,
                new String[]{navigation + section},
                getSortString());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        LOGV(TAG, "ON Load Finished - " + NewsContract.NewsItems.CONTENT_URI);
        mAdapter.changeCursor(cursor);
        mCursor = cursor;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        LOGV(TAG, "ON Loader Reset - " + NewsContract.NewsItems.CONTENT_URI);
        mAdapter.changeCursor(null);
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        } else
            mAdapter.notifyDataSetChanged();
    }

    protected void swipeToRefresh(boolean isPageSelected) {

        //  new NewsWidgetFragment().refreshWidgetList();

        if (mAdUpdateListener != null)
            mAdUpdateListener.hideIMBannerAd();

        if (isPageSelected) {
            mSwipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
            mSwipeView.setColorSchemeResources(R.color.theme_primary, R.color.theme_accent, R.color.theme_primary_dark, R.color.theme_accent);
            mSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshData();
                }
            });
            mSwipeView.setVisibility(View.VISIBLE);
        } else {
            mSwipeView.setVisibility(View.GONE);
            mSwipeView.setOnRefreshListener(null);
        }

    }


    public void onRefreshData() {
        LOGV(TAG, "on Swipe Refresh");
        if (mContProgressBar != null)
            mContProgressBar.setVisibility(View.INVISIBLE);
        mSwipeView.setRefreshing(true);
        mPage = 1;
        infiniteScroll();
                          /*if (mProgressBar != null) {
                              mProgressBar.setVisibility(View.VISIBLE);
                          }*/
        requestFeed(mReceiver, feedUrl, mPage);
        //Load the Banner Ad, only if fragment is visible
        if (getUserVisibleHint())
            loadBannerAds();

    }

    protected void infiniteScroll() {
        mListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                mPage++;
                if (mSwipeView.isRefreshing())
                    mContProgressBar.setVisibility(View.INVISIBLE);
                else
                    mContProgressBar.setVisibility(View.VISIBLE);
                requestFeed(mReceiver, feedUrl, mPage);
            }
        });
    }

    /**
     * Returns true if Banner Ad is Disabled in the configuration file,
     * otherwise false.
     */
    private boolean isBannerAdDisabled() {
        String status = ConfigManager.getInstance().getCustomApiUrl(AdConstants.ADS_BANNER_AD_STATUS_NEWSLIST);
        if (AdConstants.DISABLED.equalsIgnoreCase(status))
            return true;
        return false;
    }

    public void loadBannerAds() {
        if (mAdUpdateListener != null) {
            if (isBannerAdDisabled())
                mAdUpdateListener.hideIMBannerAd();
            else
                mAdUpdateListener.loadBannerAd(mNavigationPos, pos, null, false, -1, false, false);
        }
    }

    /**
     * Returns true if Native Ad is Enabled in the configuration file,
     * otherwise false.
     */
    public boolean isNativeAdEnabled() {
        String status = ConfigManager.getInstance().getCustomApiUrl(AdConstants.ADS_INMOBI_NATIVE_AD_STATUS);
        if (AdConstants.ENABLED.equalsIgnoreCase(status))
            return true;
        return false;
    }

    private void setAdRecurringPosition() {
        if (isNativeAdEnabled()) {
            mPreviousAdPoSition = Integer.valueOf(ConfigManager.getInstance().getCustomApiUrl(AdConstants.NATIVE_ADS_POSITION)) - 1;
        }
    }

    public static interface NativeConsumptionListener {
        public void onNativeResponseConsumed(NativeContentAd nativeResponse);
    }

    public final class AdNativeConsumptionListener implements NativeConsumptionListener {
        @Override
        public void onNativeResponseConsumed(NativeContentAd nativeResponse) {
            // Native ad was displayed in the app, request a new one to always
            // have ads ready to display
            requestNativeAd();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
            mSectionNewsClickListener = (SectionNewsClickListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
