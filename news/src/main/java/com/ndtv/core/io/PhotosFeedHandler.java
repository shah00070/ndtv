package com.ndtv.core.io;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.common.util.util.URLUtility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.PhotoFeed;
import com.ndtv.core.config.model.PhotoFeedItem;
import com.ndtv.core.util.ApplicationUtils;
import com.ndtv.core.util.GsonRequest;

import java.util.ArrayList;


public class PhotosFeedHandler {

    String url;
    int pageNumber;
    boolean isCacheRequst;
    Context mContext;
    private ContentResolver mContentResolver;
    ResultReceiver mResultreceiver;
    public String totalPhotoCount;
    public ArrayList<PhotoFeedItem> photoItemList = new ArrayList<PhotoFeedItem>();

    public PhotosFeedHandler(ResultReceiver resultreceiver, Context ctx, String s, int i, boolean flag) {
        mContext = ctx;
        url = s;
        pageNumber = i;
        isCacheRequst = flag;
        mContentResolver = mContext.getContentResolver();
        url = ApplicationUtils.buildUrl(url, pageNumber, ConfigManager.getInstance().getProp("size", 20 + ""));
        mResultreceiver = resultreceiver;
    }

    /*
    * This is related Search Implementation (Construction of Search related URL)
    * */
    public PhotosFeedHandler(ResultReceiver resultreceiver, Context ctx, String feedUrl, int pageNumber, boolean flag, String searchText) {
        String strToReplace[] = new String[]{"@search"};
        String replacement[] = new String[]{searchText};
        url = URLUtility.getFinalUrl(strToReplace, replacement, feedUrl, ctx, pageNumber);
        mContext = ctx;
        isCacheRequst = flag;
        mContentResolver = mContext.getContentResolver();
        mResultreceiver = resultreceiver;
    }


    public void downloadFeed() {

        GsonRequest<PhotoFeed> gsonRequest = new GsonRequest<PhotoFeed>(url, PhotoFeed.class, null, new Response.Listener<PhotoFeed>() {
            @Override
            public void onResponse(PhotoFeed photoFeed) {

                if (photoFeed != null && photoFeed.results != null) {

                    for (PhotoFeedItem pi : photoFeed.results) {
                        photoItemList.add(pi);
                    }
                    totalPhotoCount = photoFeed.total;
                    mResultreceiver.send(1, Bundle.EMPTY);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                volleyError.printStackTrace();

            }
        });

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(gsonRequest);
    }

}
