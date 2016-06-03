package com.ndtv.core.newswidget.ui.custom;

import android.os.Bundle;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.common.util.util.URLUtility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.video.dto.VideoItem;
import com.ndtv.core.video.dto.Videos;
import com.ndtv.core.video.ui.VideoDetailFragment;

/**
 * Created by Harisha B on 23/2/15.
 */
public class DeeplinkingVideoDetailFragment extends VideoDetailFragment implements WidgetInterfaces.OnVideoItemAvailbleListener {


    private String mVideoId;
    private VideoItem mVideoItem;
    private WidgetInterfaces.OnVideoItemAvailbleListener mOnVideoItemAvailbleListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // extractBundleData();
        extractVideoUrl();
//        downloadVideoItemData();
    }

    private void extractVideoUrl() {
        if (getArguments() != null) {
            String videoUrl = null;
            if (getArguments().getBoolean(ApplicationConstants.BundleKeys.IS_DEEPLINK_URL))
                videoUrl = getArguments().getString(ApplicationConstants.BundleKeys.DEEP_LINK_URL);
            else
                videoUrl = getNewsWidgetVideoUrl();
            downloadVideoItemData(videoUrl);
        }

    }

    private String getNewsWidgetVideoUrl() {
        String videoDetailAPI = ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CustomApiType.VIDEO_DETAIL_API);
        String strToReplaceEpisode[] = new String[]{ApplicationConstants.UrlKeys.URL_VIDEO_ID};
        String replacementEpisode[] = new String[]{mVideoId};

        videoDetailAPI = URLUtility.getFinalUrl(strToReplaceEpisode, replacementEpisode, videoDetailAPI, getActivity());
        videoDetailAPI = URLUtility.getVideoEncodedUrl(videoDetailAPI);
        return videoDetailAPI;
    }

    private void downloadVideoItemData(String videoDetailAPI) {
        NewsWidgetManager.getInstance().downloadVideoItem(getActivity().getBaseContext(), videoDetailAPI, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
    }

    @Override
    public void extractBundleData() {
        if (getArguments() != null) {
            if (!getArguments().getBoolean(ApplicationConstants.BundleKeys.IS_DEEPLINK_URL)){
                String videoWidgetApplink = getArguments().getString(Constants.DEEP_LINK_URL);
                mVideoId = NewsWidgetManager.getDeeplinkingId(videoWidgetApplink);
            }

        } else {
            return;
        }
    }

    @Override
    public void onVideoAvailable(Videos response) {
        if (response != null && null != response.videoList) {
            mVideoItem = response.videoList.get(0);
            mVideoPlayUrl = mVideoItem.url;
            mVideoTitle = mVideoItem.getVideoTitle();
            mVideoDescription = mVideoItem.getVideoDescription();
            setTitleAndDescription();
            playVideo();
        }
    }
}

