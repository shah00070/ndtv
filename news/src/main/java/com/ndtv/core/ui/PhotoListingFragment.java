package com.ndtv.core.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.PhotoFeedItem;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.io.PhotosAlbumsHandler;
import com.ndtv.core.io.PhotosFeedHandler;
import com.ndtv.core.search.ui.PhotoListingSearchFragment;
import com.ndtv.core.ui.adapters.PhotoListAdapter;
import com.ndtv.core.ui.listener.EndlessScrollListener;
import com.ndtv.core.ui.widgets.AlbumDetailFragment;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.util.DetachableResultReceiver;

import java.util.ArrayList;

import static com.ndtv.core.util.LogUtils.LOGV;

public class PhotoListingFragment extends BaseFragment implements com.ndtv.core.util.DetachableResultReceiver.Receiver, ApplicationConstants.BundleKeys {

    private static final String TAG = "PhotoListing ";
    protected boolean mIsFetching = false;
    private boolean isPhotoAlbumFetching = false;
    private int mPage = 1;
    protected String feedUrl;
    protected DetachableResultReceiver mReceiver, mReceiverForAlbum;
    protected View rootView;

    private PhotoListAdapter mAdapter;
    protected ListView mListView;
    private SwipeRefreshLayout mSwipeView;
    private int pos;
    protected String section;
    private PhotoListingFragment mPhotoListingFragment;
    private ArrayList<PhotoFeedItem> allPhotoItemList;
    protected PhotosFeedHandler mphotosFeedHandler;
    protected PhotosAlbumsHandler mAlbumHandler;
    private int mNavigationPosition;
    protected ProgressBar progressBar;
    //For Deeplinking
    private int mListItemPos;
    private ArrayList<String> albumIdsList;
    protected BannerAdFragment.AdListener mAdUpdateListener;
    private String navigation;
    private boolean bIsFromSearch = false;

    public static Fragment newInstance(String url, int position, String title, int navigationPos) {
        PhotoListingFragment photoListingFragment = new PhotoListingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("com.july.ndtv.EXTRA_FEED_SLUG", url);
        bundle.putInt("position", position);
        bundle.putString("section", title);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, navigationPos);
        photoListingFragment.setArguments(bundle);
        return photoListingFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.feedUrl = bundle.getString("com.july.ndtv.EXTRA_FEED_SLUG");
            this.pos = bundle.getInt("position");
            this.section = bundle.getString("section");
            this.mNavigationPosition = bundle.getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS);
        }
        navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPosition);
        this.mPhotoListingFragment = this;
        this.allPhotoItemList = new ArrayList<>();
        this.albumIdsList = new ArrayList<>();
        PreferencesManager.getInstance(getActivity()).setCurrentAlbumPosition(PreferencesManager.CURRENT_ALBUM_POSITION, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        requestFeed(mReceiver, feedUrl, mPage);
//      loadBannerAds();

        boolean isFromNavDrawer = PreferencesManager.getInstance(getActivity()).isFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER);
        if (isFromNavDrawer) {
            PreferencesManager.getInstance(getActivity()).setCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN, 1);
            PreferencesManager.getInstance(getActivity()).setIsFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER, false);
        }
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
    public void setScreenName() {
        setScreenName(navigation + " - " + section);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_photo_listing, container, false);

        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        mReceiverForAlbum = new DetachableResultReceiver(new Handler());
        mReceiverForAlbum.setReceiver(this);
        mListView = (ListView) rootView.findViewById(R.id.photo_list);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //  requestFeed(mReceiver, feedUrl, mPage);

        swipeToRefresh();
        infiniteScroll();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListItemPos = position;

                PreferencesManager.getInstance(getActivity()).setCurrentAlbumPosition(PreferencesManager.CURRENT_ALBUM_POSITION, position);
                PreferencesManager.getInstance(getActivity()).setCurrentSectionName(PreferencesManager.CURRENT_SECTION_NAME, section);

                if (!isPhotoAlbumFetching) {
                    progressBar.setVisibility(View.VISIBLE);
                    isPhotoAlbumFetching = true;
                    mAlbumHandler = new PhotosAlbumsHandler(mReceiverForAlbum, getActivity(), allPhotoItemList.get(position).getRss(), false);
                    mAlbumHandler.downloadFeed();
                } else {
                    // Toast.makeText(getActivity(), "Please wait....", Toast.LENGTH_LONG).show();
                }
            }
        });

//        loadBannerAds();
    }

    private boolean isPhotoSearchFragment(){
        if(this instanceof PhotoListingSearchFragment)
            return true;
        return false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (null != getActivity() && isVisibleToUser) {
            if (allPhotoItemList != null)
                loadBannerAds();
        }
    }

    public void openPhotoAlbum() {
        PreferencesManager.getInstance(getActivity()).setCurrentPhotoIndex(ApplicationConstants.PreferenceKeys.CURRENT_IMAGE_INDEX, 0);
        if (mDetailFragmentListener != null) {
            if(isPhotoSearchFragment()){
                bIsFromSearch = true;
            }
            AlbumDetailFragment albumDetailFragment = AlbumDetailFragment.newInstance(mAlbumHandler.photoResultsData, section, mNavigationPosition, pos, bIsFromSearch);
            mDetailFragmentListener.onAddDetailFragment(albumDetailFragment, mPhotoListingFragment.getClass().getName());
            //For deeplinking
            albumDetailFragment.setAlbumLinkUrl(allPhotoItemList.get(mListItemPos).link);
        }
        isPhotoAlbumFetching = false;
        progressBar.setVisibility(View.GONE);
    }

    public void requestFeed(DetachableResultReceiver receiver, String url, int i) {
        if (!mIsFetching) {
            progressBar.setVisibility(View.VISIBLE);
            mIsFetching = true;
            mphotosFeedHandler = new PhotosFeedHandler(receiver, getActivity(), url, i, false);
            mphotosFeedHandler.downloadFeed();
        }
    }

    @Override
    public void onReceiveResult(int i, Bundle bundle) {
        if (i == 1) {
            LOGV(TAG, "onReceiveResult for page " + mPage + " of " + feedUrl);
            if (mSwipeView.isRefreshing())
                mSwipeView.setRefreshing(false);

            for (int j = 0; j < mphotosFeedHandler.photoItemList.size(); j++) {
                if (!albumIdsList.contains(mphotosFeedHandler.photoItemList.get(j).getId())) {
                    albumIdsList.add(mphotosFeedHandler.photoItemList.get(j).getId());
                    allPhotoItemList.add(mphotosFeedHandler.photoItemList.get(j));
                }
            }

            mAdapter = new PhotoListAdapter(getActivity(), allPhotoItemList);
            int visiblePosition = mListView.getFirstVisiblePosition();
            if (mListView != null) {
                mListView.setAdapter(mAdapter);
                boolean isBackFromAlbum = PreferencesManager.getInstance(getActivity()).getIsBackFromAlbum(PreferencesManager.IS_BACK_FROM_ALBUM);
                String backSectionName = PreferencesManager.getInstance(getActivity()).getCurrentSectionName(PreferencesManager.CURRENT_SECTION_NAME);
                if (isBackFromAlbum && backSectionName != null && section.equalsIgnoreCase(backSectionName)) {
                    int albumPos = PreferencesManager.getInstance(getActivity()).getCurrentAlbumPosition(PreferencesManager.CURRENT_ALBUM_POSITION);
                    mListView.setSelection(albumPos);
                    PreferencesManager.getInstance(getActivity()).setIsBackFromAlbum(PreferencesManager.IS_BACK_FROM_ALBUM, false);
                    PreferencesManager.getInstance(getActivity()).setCurrentSectionName(PreferencesManager.CURRENT_SECTION_NAME, null);
                } else {
                    mListView.setSelection(visiblePosition);
                }
            }
            mAdapter.notifyDataSetChanged();
            mIsFetching = false;
            progressBar.setVisibility(View.GONE);
        } else if (i == 2) {
            openPhotoAlbum();
        }
    }


    private void swipeToRefresh() {
        if (mAdUpdateListener != null)
            mAdUpdateListener.hideIMBannerAd();
        mSwipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeView.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        mSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LOGV(TAG, "on Swipe Refresh");
                mSwipeView.setRefreshing(true);
                mPage += 1;
                requestFeed(mReceiver, feedUrl, mPage);
                progressBar.setVisibility(View.GONE);
                //Load the Banner Ad, only if fragment is visible
                if (getUserVisibleHint())
                    loadBannerAds();
            }
        });
    }


    private void infiniteScroll() {
        mListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                mPage++;
                requestFeed(mReceiver, feedUrl, mPage);
            }
        });
    }


    public void loadBannerAds() {
        if (mAdUpdateListener != null) {
            if(!(this instanceof PhotoListingSearchFragment))
                mAdUpdateListener.loadBannerAd(mNavigationPosition, pos, null, true, -1, false, false);
        }
    }
}
