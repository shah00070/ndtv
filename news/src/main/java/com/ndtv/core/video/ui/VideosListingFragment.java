package com.ndtv.core.video.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.search.ui.VideoListingSearchFragment;
import com.ndtv.core.ui.ChromeCastFragment;
import com.ndtv.core.ui.listener.RecyclerViewVeriticalEndlessScrollListner;
import com.ndtv.core.util.ApplicationUtils;
import com.ndtv.core.util.GsonRequest;
import com.ndtv.core.video.adapter.VideoListAdapter;
import com.ndtv.core.video.dto.VideoItem;
import com.ndtv.core.video.dto.Videos;
import com.ndtv.core.video.manager.VideoManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laveen on 16/2/15.
 */
public class VideosListingFragment extends ChromeCastFragment implements Response.Listener<Videos>, Response.ErrorListener, View.OnClickListener {

    protected static final String TAG = "VideosListing ";

    public static final String VIDEOS_LISTING_URL = "video_list_url";
    public static final String VIDEOS_LISTING_SECTION = "video_list_section";

    protected String mVideoListUrl;
    protected String mVideoListSection;

    protected VideoListAdapter mAdapter;

    protected List<VideoItem> mVideosList;

    protected int mPageNum;

    protected boolean mDownloading;

    protected boolean mDownloadData = true;

    protected RecyclerView mScheduleList;

    protected ProgressBar mLoadingIndicator;

    protected LinearLayoutManager mLayoutManager;

    protected SwipeRefreshLayout mSwipeView;

    private int mNavigationPosition;

    private int mSectionPosition;

    public BannerAdFragment.AdListener mAdUpdateListener;
    private String navigation;
    private ArrayList<String> videoIdsList;

    protected boolean mCandownloadFuther = true;

    int total = 0;

    public static Fragment getInstance(String url, String section, int navigationPosition, int sectionPosition) {
        Fragment fragment = new VideosListingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(VIDEOS_LISTING_URL, ApplicationUtils.setVideoFormat(url));
        bundle.putString(VIDEOS_LISTING_SECTION, section);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, navigationPosition);
        bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POS, sectionPosition);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mVideoListUrl = bundle.getString(VIDEOS_LISTING_URL);
            mVideoListSection = bundle.getString(VIDEOS_LISTING_SECTION);
            mNavigationPosition = bundle.getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS);
            mSectionPosition = bundle.getInt(ApplicationConstants.BundleKeys.SECTION_POS);
        }
        navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPosition);
        mVideosList = new ArrayList<>();
        mPageNum = 1;

        VideoManager.getInstance().addData(mVideoListSection, mVideosList);
        videoIdsList = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos_list, container, false);
        initViews(view);
        return view;
    }

    protected void initViews(View view) {
        mScheduleList = (RecyclerView) view.findViewById(R.id.recycler_view);
        mLoadingIndicator = (ProgressBar) view.findViewById(R.id.loading_indicators);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mScheduleList.setLayoutManager(mLayoutManager);
        mScheduleList.setHasFixedSize(true);
        mSwipeView = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeView.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        mSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeView.setRefreshing(true);
                onRefreshData();
                ;

            }
        });

        mScheduleList.setOnScrollListener(new RecyclerViewVeriticalEndlessScrollListner() {
            @Override
            public void loadNextPage() {
                if (total > mAdapter.getItemCount())
                    downloadNextPage();
            }
        });
    }

    private void downloadNextPage() {
        if (mCandownloadFuther && !mDownloading) {
            mPageNum++;
            showLoadingBar();
            downloadFeed(getActivity(), mPageNum);
        }
    }

    protected void onRefreshData() {
        mCandownloadFuther = true;
        mPageNum = 1;
        //  mVideosList.clear();
        downloadFeed(getActivity(), mPageNum);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDownloadData) {
            showLoadingBar();
            onRefreshData();
        }
        mDownloadData = true;
        boolean isFromNavDrawer = PreferencesManager.getInstance(getActivity()).isFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER);
        if (isFromNavDrawer) {
            PreferencesManager.getInstance(getActivity()).setCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN, 2);
            PreferencesManager.getInstance(getActivity()).setIsFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER, false);
        }
    }

    @Override
    public void setScreenName() {
        setScreenName(navigation + " - " + mVideoListSection);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mScheduleList.setAdapter(mAdapter = new VideoListAdapter(mVideosList, this));
        loadBannerAds();
    }

    public void downloadFeed(Context context, int pageNum) {
        if (!TextUtils.isEmpty(mVideoListUrl)) {
            mDownloading = true;
            String url = ApplicationUtils.buildUrl(mVideoListUrl, pageNum, ConfigManager.getInstance().getPageSize() + "");

            GsonRequest<Videos> gsonRequest = new GsonRequest<>(url, Videos.class, null, this, this);
            VolleyRequestQueue.getInstance(context).addToRequestQueue(gsonRequest);

            //VideoManager.getInstance().downloadData(context,url,this,this);
        }
    }


    @Override
    public void onResponse(Videos videos) {
        mDownloading = false;
        total = videos.total;
        if (mSwipeView.isRefreshing())
            mSwipeView.setRefreshing(false);

        hideLoadingBar();

//        if (videos != null && videos.videoList != null) {
//            mVideosList.addAll(videos.videoList);
//            mAdapter.notifyDataSetChanged();
//        } else {
//            mCandownloadFuther = false;
//        }

        if (videos != null && videos.videoList != null) {

            for (int j = 0; j < videos.videoList.size(); j++) {
                if (!videoIdsList.contains(videos.videoList.get(j).getId())) {
                    videoIdsList.add(videos.videoList.get(j).getId());
                    mVideosList.add(videos.videoList.get(j));
                }
            }
            mAdapter.notifyDataSetChanged();
        } else {
            mCandownloadFuther = false;
        }
    }

    private void hideLoadingBar() {
        mLoadingIndicator.setVisibility(View.GONE);
        //mSwipeView.setRefreshing(false);
    }

    private void showLoadingBar() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        //  mSwipeView.setRefreshing(true);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        mCandownloadFuther = false;
        mDownloading = false;
        hideLoadingBar();
    }

    @Override
    public void onClick(View v) {
        int index = (int) v.getTag();
        mDownloadData = false;
        VideoItem item = mVideosList.get(index);

        Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VideoDetailFragment.VIDEO_TITLE, item.getVideoTitle());
        bundle.putString(VideoDetailFragment.VIDEO_DESCRIPTION, item.getVideoDescription());
        bundle.putString(VideoDetailFragment.VIDEO_PLAY_URL, item.url);
        bundle.putString(VideoDetailFragment.VIDEO_IMAGE, item.fullImage);
        bundle.putString(VideoDetailFragment.VIDEO_LINK, item.link);
        bundle.putString(VideoDetailFragment.VIDEO_ID, item.id);
        if(isVideolistSearch())
            bundle.putBoolean(VideoDetailFragment.IS_FROM_SEARCH, true);
        else
            bundle.putBoolean(VideoDetailFragment.IS_FROM_SEARCH, false);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, mNavigationPosition);
        bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POS, mSectionPosition);
        bundle.putString(VIDEOS_LISTING_SECTION, mVideoListSection);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public boolean isVideolistSearch(){
        if(this instanceof VideoListingSearchFragment)
            return true;
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Log.d("VIDEOLIST", "onAttch:" + this);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
//            setAdBackground();
        } catch (Exception ex) {

        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("VIDEOLIST", "setUserVisibleHint:" + isVisibleToUser + " " + this);
        /*if (isVisibleToUser)
            setAdBackground();
        else
            resetAdBackground();*/
    }

    private void setAdBackground() {
        if (getActivity() != null && getActivity().findViewById(R.id.adContainer) != null) {
            getActivity().findViewById(R.id.adContainer).setBackgroundColor(getResources().getColor(R.color.video_list_background));
        }
    }

    private void resetAdBackground() {
        if (getActivity() != null && getActivity().findViewById(R.id.adContainer) != null) {
            getActivity().findViewById(R.id.adContainer).setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("VIDEOLIST", "onDetach:" + this);
//        resetAdBackground();
        //resetting ad background to white color since adview container is part of activity
       /* if (getActivity() != null && getActivity().findViewById(R.id.adContainer) != null) {
            getActivity().findViewById(R.id.adContainer).setBackgroundColor(getResources().getColor(R.color.white));
        }*/
    }

    public void loadBannerAds() {
        if (mAdUpdateListener != null) {
            if(!(this instanceof VideoListingSearchFragment))
                mAdUpdateListener.loadBannerAd(mNavigationPosition, mSectionPosition, null, false, -1, false, true);
        }
    }
}
