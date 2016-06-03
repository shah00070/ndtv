package com.ndtv.core.search.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.NewsManager;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.util.URLUtility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.NewsFeed;
import com.ndtv.core.config.model.NewsItems;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.newswidget.ui.custom.CustomListView;
import com.ndtv.core.search.adapter.NewsListingSearchAdapter;
import com.ndtv.core.ui.listener.EndlessScrollListener;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.util.GsonRequest;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ram Prakash on 8/4/15.
 */
public class NewsListingSearchFragment extends BaseFragment implements Response.Listener<NewsFeed>, Response.ErrorListener {    //, View.OnClickListener

    private static final String LOG_TAG = "Search Text";
    private TextView mNewsItemSearchCount;
    private View rootView;
    //private View mFooterView;
    protected String feedUrl;
    protected CustomListView mListView;
    private ProgressBar mProgressBar;
    private List<NewsItems> mNewsList;
    private SwipeRefreshLayout mSwipeView;
    protected int mPageNum;
    private boolean mDownloading;
    private boolean mDownloadData = true;
    private boolean mCandownloadFurther = true;
    private NewsListingSearchAdapter mAdapter;
    private int mTotalSearchCount = 0;
    protected String mSearchText;
    private TextView mEmptyTextView;
    private BannerAdFragment.AdListener mAdUpdateListener;

    public static Fragment newInstance(String searchText) {
        Fragment searchFragment = new NewsListingSearchFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("Search_Text", searchText);
        searchFragment.setArguments(bundle);
        return searchFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (null != bundle) {
            mSearchText = bundle.getString("Search_Text");
            feedUrl = ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CustomApiType.STORY_SEARCH_API);
        }
        mNewsList = new ArrayList<>();
        mPageNum = 1;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser)
            loadBannerAds();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_news_listing, container, false);
        // mFooterView = inflater.inflate(R.layout.list_footer, null, false);
        initViews();
        return rootView;
    }

    private void initViews() {
        // mSyncHelper = SyncHelper.getSyncHelper(getActivity());
        // mReceiver = new DetachableResultReceiver(new Handler());
        // mReceiver.setReceiver(this);
        mListView = (CustomListView) rootView.findViewById(R.id.news_list);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        mSwipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeView.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        mNewsItemSearchCount = (TextView) rootView.findViewById(R.id.search_news_count);
        mEmptyTextView = (TextView) rootView.findViewById(R.id.empty_view);
        mSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeView.setRefreshing(true);
                onRefreshData();
            }
        });
        /*mListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                downloadNextPage();
            }
        });*/
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDownloadData = false;
                NewsItems item = mNewsList.get(position);
                if (mDetailFragmentListener != null) {

                    PreferencesManager.getInstance(getActivity()).setIsBackFromCommentList(ApplicationConstants.PreferenceKeys.IS_BACK_FROM_COMMENT, false);
                    mDetailFragmentListener.onAddDetailFragment(NewsDetailSearchFragment.newInstance(position), NewsListingSearchFragment.this.toString());
                    //item.id, item.getTitle(), item.story_image, item.device,mNewsList.size()
                }

            }
        });
    }


    private void downloadNextPage() {
        if (mCandownloadFurther && !mDownloading) {
            mPageNum++;
            showLoadingBar();
            downloadFeed(mPageNum);
        }
    }

    private void onRefreshData() {
        setEndlessScrollListener();
        mCandownloadFurther = true;
        mPageNum = 1;
        mNewsList.clear();
        NewsManager.getNewsInstance().mNewsSearchItems.clear();
        downloadFeed(mPageNum);
    }

    private void setEndlessScrollListener() {
        mListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                mDownloading = false;
                downloadNextPage();
            }
        });
    }


    private void hideLoadingBar() {
        mProgressBar.setVisibility(View.GONE);
        //mSwipeView.setRefreshing(false);
    }

    private void showLoadingBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        //  mSwipeView.setRefreshing(true);
    }

    public void downloadFeed(int pageNum) {
        if (!TextUtils.isEmpty(feedUrl)) {
            mDownloading = true;
            String strToReplace[] = new String[]{"@search"};
            String replacement[] = new String[]{mSearchText};
            String url = URLUtility.getFinalUrl(strToReplace, replacement, feedUrl, getActivity(), pageNum);

            GsonRequest<NewsFeed> gsonRequest = new GsonRequest<>(url, NewsFeed.class, null, this, this);
            VolleyRequestQueue.getInstance(getActivity()).addToRequestQueue(gsonRequest);

            //VideoManager.getInstance().downloadData(context,url,this,this);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       /* if (mNewsItemSearchCount != null)
            mNewsItemSearchCount.setText(MessageFormat.format(getActivity().getString(R.string.search_header_text)),mSearchText));*/
        mListView.setAdapter(mAdapter = new NewsListingSearchAdapter(mNewsList, getActivity())); //, this
        //TODO load Banner Add here
    }

    @Override
    public void onDestroy() {
        if (NewsManager.getNewsInstance().mNewsSearchItems != null) {
            NewsManager.getNewsInstance().mNewsSearchItems.clear();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDownloadData) {
            showLoadingBar();
            onRefreshData();
        }
        setEndlessScrollListener();
        if (mProgressBar.isShown() && !mDownloading) {
            //String article;
            hideLoadingBar();
            /*if (getActivity() != null) {
                if (mTotalSearchCount == 1) {
                    article = getActivity().getString(R.string.search_article);
                } else {
                    article = getActivity().getString(R.string.search_articles);
                }
                mNewsItemSearchCount.setText((MessageFormat.format(getActivity().getString(R.string.search_header_text), mSearchText, mTotalSearchCount + " " + article)));
            }*/
            setSearchResultTextView();
        }
        mDownloadData = true;
    }
    /*@Override
    protected void requestFeed(DetachableResultReceiver receiver, String url, int i) {
        mIsFetching = true;
       order=0;
        if (i == 1 && order == 1)
            clearData();
        String strToReplace[] = new String[] { "@search" };
        String replacement[] = new String[] { "Dhoni" };
        url = URLUtility.getFinalUrl(strToReplace, replacement, url, getActivity(), i);

        mSyncHelper.requestFeed(receiver, url, i, isCacheRequest, SEARCH_ITEM);
    }*/

    @Override
    public void setScreenName() {
        setScreenName(LOG_TAG + " - " + mSearchText);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        mDownloading = false;
        hideLoadingBar();
        if (mNewsList == null || mNewsList.isEmpty()) {
            mEmptyTextView.setVisibility(View.VISIBLE);
            mListView.setEmptyView(mEmptyTextView);
            mCandownloadFurther = false;
        }
    }

    @Override
    public void onResponse(NewsFeed newsFeed) {
        mDownloading = false;
        if (mSwipeView.isRefreshing()) {
            mSwipeView.setRefreshing(false);
        }
        hideLoadingBar();
        if (newsFeed != null && newsFeed.results != null) {
            mTotalSearchCount = newsFeed.total;
            NewsManager.getNewsInstance().mNewsSearchItems.addAll(newsFeed.results);
            mNewsList.addAll(newsFeed.results);
            mAdapter.notifyDataSetChanged();
            if (getActivity() != null) {
                //mNewsItemSearchCount.setText((MessageFormat.format(getActivity().getString(R.string.search_header_text), mSearchText, newsFeed.total + " " + getActivity().getString(R.string.search_articles))));
                setSearchResultTextView();
                Fragment currrentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.container);
                if (null != currrentFragment && currrentFragment instanceof NewsDetailSearchFragment) {
                    ((NewsDetailSearchFragment) currrentFragment).notifyAdapter();
                }
            }
        } else {
            mEmptyTextView.setVisibility(View.VISIBLE);
            mListView.setEmptyView(mEmptyTextView);
            mCandownloadFurther = false;
        }

    }

    private void setSearchResultTextView() {
        if (getActivity() != null) {
            String article;
            if (mTotalSearchCount == 1) {
                article = getActivity().getString(R.string.search_article);
            } else {
                article = getActivity().getString(R.string.search_articles);
            }
            mNewsItemSearchCount.setText((MessageFormat.format(getActivity().getString(R.string.search_header_text), mSearchText, mTotalSearchCount + " " + article)));
        }
    }

    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }

    public void loadBannerAds() {
        if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(-1, -1, null, false, -1, false, false);
        }
    }

    /*@Override
    public void onClick(View v) {

        int index = (int) v.getTag();

        mDownloadData = false;

        NewsItems item = mNewsList.get(index);
        if (mDetailFragmentListener != null) {
            mDetailFragmentListener.onAddDetailFragment(NewsDetailSearchFragment.newInstance(item.id, item.getTitle(), item.story_image, item.link), NewsListingSearchFragment.this.toString());
        }


    }*/
}
