package com.ndtv.core.video.manager;

import android.content.Context;

import com.android.volley.Response;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.util.GsonRequest;
import com.ndtv.core.video.dto.VideoItem;
import com.ndtv.core.video.dto.Videos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laveen on 11/3/15.
 */
public class VideoManager {

    private static VideoManager sManager;

    private Map<String, List<VideoItem>> mData;

    private VideoManager() {
        mData = new HashMap<>();
    }

    public static VideoManager getInstance() {
        if (sManager == null)
            sManager = new VideoManager();
        return sManager;
    }

    public void downloadData(Context context, String url, final Response.Listener<Videos> listener, final Response.ErrorListener errorListener) {
        GsonRequest<Videos> gsonRequest = new GsonRequest<>(url, Videos.class, null, listener, errorListener);
        VolleyRequestQueue.getInstance(context).addToRequestQueue(gsonRequest);
    }

    public void addData(String section, List<VideoItem> items) {
        mData.put(section, items);
    }

    public List<VideoItem> getData(String section) {
        return mData.get(section);
    }
}
