package com.ndtv.core.search.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.io.PhotosFeedHandler;
import com.ndtv.core.ui.PhotoListingFragment;
import com.ndtv.core.util.DetachableResultReceiver;

import java.text.MessageFormat;

/**
 * Created by Ram Prakash on 8/4/15.
 */
public class PhotoListingSearchFragment extends PhotoListingFragment {
    private TextView mPhotoSearchCount;
    private String mSearchText;
    private TextView mEmptyView;

    public static Fragment newInstance(String searchText) {
        Fragment searchFragment = new PhotoListingSearchFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("Search_Text", searchText);
        searchFragment.setArguments(bundle);
        return searchFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        mSearchText = bundle.getString("Search_Text");
        section = "Photos";
        feedUrl = ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CustomApiType.PHOTO_SEARCH_API);
//        loadBannerAds();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_photo_listing, container, false);
        initView();
        return rootView;

    }

    private void initView() {
        mPhotoSearchCount = (TextView) rootView.findViewById(R.id.search_photo_count);
        mListView = (ListView) rootView.findViewById(R.id.photo_list);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);
        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        mReceiverForAlbum = new DetachableResultReceiver(new Handler());
        mReceiverForAlbum.setReceiver(this);
    }

    @Override
    public void requestFeed(DetachableResultReceiver receiver, String url, int i) {
        if (!mIsFetching) {
            progressBar.setVisibility(View.VISIBLE);
            mIsFetching = true;
            mphotosFeedHandler = new PhotosFeedHandler(receiver, getActivity(), url, i, false, mSearchText);
            mphotosFeedHandler.downloadFeed();
        }
    }

    @Override
    public void onReceiveResult(int i, Bundle bundle) {
        super.onReceiveResult(i, bundle);
        if (mphotosFeedHandler.totalPhotoCount == null) {
            mEmptyView.setVisibility(View.VISIBLE);
            mListView.setEmptyView(mEmptyView);
            return;
        }
        if (Integer.parseInt(mphotosFeedHandler.totalPhotoCount.trim()) > 0) {
            if (mPhotoSearchCount != null && getActivity() != null) {
                // mPhotoSearchCount.setText((MessageFormat.format(getActivity().getString(R.string.search_header_text), mSearchText, mphotosFeedHandler.totalPhotoCount + " " + getActivity().getString(R.string.search_gellery))));
                setSearchResultTextView();
            }
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mListView.setEmptyView(mEmptyView);
        }
    }

    private void setSearchResultTextView() {
        if (getActivity() != null) {
            String gellery;
            if (Integer.parseInt(mphotosFeedHandler.totalPhotoCount.trim()) == 1) {
                gellery = getActivity().getString(R.string.search_gellery);
            } else {
                gellery = getActivity().getString(R.string.search_gelleries);
            }
            mPhotoSearchCount.setText((MessageFormat.format(getActivity().getString(R.string.search_header_text), mSearchText, mphotosFeedHandler.totalPhotoCount + " " + gellery)));
        }
    }


    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser)
            loadBannerAd();
    }

    public void loadBannerAd() {
        if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(-1, -1, null, true, -1, false, false);
        }
    }
}
