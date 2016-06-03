package com.ndtv.core.io;

import com.android.volley.Response;
import com.ndtv.core.video.dto.VideoItem;
import com.ndtv.core.video.dto.Videos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laveen on 16/2/15.
 */
public class VideosFeedHandler {

    public String mUrl;

    private VideosFeedHandler() {
        mVideoItems = new ArrayList<>();
    }

    private static VideosFeedHandler sInstace;
    private Response.Listener<Videos> mListner;
    private Response.ErrorListener mErrorListner;
    private List<VideoItem> mVideoItems;

    public static VideosFeedHandler getInstance() {
        if (sInstace == null)
            sInstace = new VideosFeedHandler();
        return sInstace;
    }

    public void setVideoListData(List<VideoItem> items) {
        mVideoItems = items;
    }

    public VideoItem getVideoItem(int index) {
        if (index < mVideoItems.size()) {
            return mVideoItems.get(index);
        }
        return null;
    }

}
