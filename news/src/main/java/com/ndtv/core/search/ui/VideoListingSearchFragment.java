package com.ndtv.core.search.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.ndtv.core.R;
import com.ndtv.core.common.util.util.URLUtility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.util.ApplicationUtils;
import com.ndtv.core.util.GsonRequest;
import com.ndtv.core.video.dto.Videos;
import com.ndtv.core.video.ui.VideosListingFragment;

import java.text.MessageFormat;

/**
 * Created by Ram Prakash on 8/4/15.
 */
public class VideoListingSearchFragment extends VideosListingFragment {

    private TextView mVideoSearchCount;
    private String mSearchText;
    private TextView mEmptyTextView;
    private int mVideocount;

    public static Fragment newInstance(String searchText) {
        Fragment searchFragment = new VideoListingSearchFragment();
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
        mVideoListUrl = ApplicationUtils.setVideoFormat(ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CustomApiType.VIDEO_SEARCH_API));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_video_lisitng, container, false);
        initViews(view);
        return view;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mVideocount = 0;
        mVideoSearchCount = (TextView) view.findViewById(R.id.search_video_count);
        mEmptyTextView = (TextView) view.findViewById(R.id.empty_view);
    }

    @Override
    protected void onRefreshData() {
        super.onRefreshData();
        mVideocount = 0;
    }

    @Override
    public void downloadFeed(Context context, int pageNum) {
        if (!TextUtils.isEmpty(mVideoListUrl)) {
            mDownloading = true;
            String strToReplace[] = new String[]{"@search"};
            String replacement[] = new String[]{mSearchText};
            String url = URLUtility.getFinalUrl(strToReplace, replacement, mVideoListUrl, getActivity(), pageNum);
            // String finalUrl = ApplicationUtils.buildUrl(url, pageNum, ConfigManager.getInstance().getPageSize() + "");
            GsonRequest<Videos> gsonRequest = new GsonRequest<>(url, Videos.class, null, this, this);
            VolleyRequestQueue.getInstance(context).addToRequestQueue(gsonRequest);
            //VideoManager.getInstance().downloadData(context,url,this,this);
        }
    }

    @Override
    public void onResponse(Videos videos) {
        super.onResponse(videos);
        if (videos.total == 0) {
            mScheduleList.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.VISIBLE);
            return;
        }
        int count = videos.total;
        if (count == 0) {
            mVideocount += videos.total;
        } else {
            mVideocount = videos.total;
        }
        if (mVideosList != null && !mVideosList.isEmpty() && getActivity() != null) {
            setSearchResultTextView();
            // mVideoSearchCount.setText((MessageFormat.format(getActivity().getString(R.string.search_header_text), mSearchText, mVideocount + " " + getActivity().getString(R.string.search_videos))));
        } else {
            mScheduleList.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private void setSearchResultTextView() {
        if (getActivity() != null) {
            String video;
            if (mVideocount == 1) {
                video = getActivity().getString(R.string.search_video);
            } else {
                video = getActivity().getString(R.string.search_videos);
            }
            mVideoSearchCount.setText((MessageFormat.format(getActivity().getString(R.string.search_header_text), mSearchText, mVideocount + " " + video)));
        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        super.onErrorResponse(volleyError);
        if (mVideosList == null || mVideosList.isEmpty()) {
            mScheduleList.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.VISIBLE);
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

    public void loadBannerAd(){
        if(mAdUpdateListener != null)
            mAdUpdateListener.loadBannerAd(-1, -1, null, false, -1, false, true);
    }
}
