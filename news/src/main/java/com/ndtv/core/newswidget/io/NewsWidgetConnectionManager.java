package com.ndtv.core.newswidget.io;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.common.util.GsonObjectRequest;
import com.ndtv.core.config.model.Albums;
import com.ndtv.core.newswidget.dto.NewsWidget;
import com.ndtv.core.video.dto.Videos;

/**
 * Created by Harisha B on 18/2/15.
 */
public class NewsWidgetConnectionManager {
    public static final int DEFAULT_SEARCH_TIMEOUT = 60000;

    private static NewsWidgetConnectionManager sNewsWidgetConnectionManager;

    public static NewsWidgetConnectionManager getInstance() {
        if (sNewsWidgetConnectionManager == null) {
            sNewsWidgetConnectionManager = new NewsWidgetConnectionManager();
        }
        return sNewsWidgetConnectionManager;
    }

    private NewsWidgetConnectionManager() {
    }

    public void downloadNewsWidget(Context context, String url, Response.Listener successListener, Response.ErrorListener errorListener) {

        if (!TextUtils.isEmpty(url)) {

            final NdtvApplication application = NdtvApplication.getApplication(context.getApplicationContext());

            final GsonObjectRequest<NewsWidget> jsObjRequest = new GsonObjectRequest<NewsWidget>(Request.Method.GET, url,
                    NewsWidget.class, null, successListener, errorListener, application);
            jsObjRequest.setShouldCache(false);
            DefaultRetryPolicy policy = new DefaultRetryPolicy(DEFAULT_SEARCH_TIMEOUT,
                    1, 1f);
            jsObjRequest.setRetryPolicy(policy);
            application.mRequestQueue.add(jsObjRequest);
        }
    }

    public void downloadVideoItem(Context context, String url, Response.Listener listener, Response.ErrorListener errorListener) {

        if (!TextUtils.isEmpty(url)) {
            NdtvApplication ndtvApplication = NdtvApplication.getApplication(context);
            GsonObjectRequest<Videos> videosGsonObjectRequest = new GsonObjectRequest<Videos>(Request.Method.GET, url, Videos.class,
                    null, listener, errorListener, ndtvApplication);
            ndtvApplication.mRequestQueue.add(videosGsonObjectRequest);
        }
    }

    public void downloadAlbum(Context context, String url, Response.Listener listener, Response.ErrorListener errorListener) {
        if (!TextUtils.isEmpty(url)) {
            NdtvApplication ndtvApplication = NdtvApplication.getApplication(context);
            GsonObjectRequest<Albums> albumsGsonObjectRequest = new GsonObjectRequest<Albums>(Request.Method.GET, url, Albums.class,
                    null, listener, errorListener, ndtvApplication);
            ndtvApplication.mRequestQueue.add(albumsGsonObjectRequest);
        }
    }
}
