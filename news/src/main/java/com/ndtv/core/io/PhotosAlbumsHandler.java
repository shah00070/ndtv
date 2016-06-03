package com.ndtv.core.io;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.config.model.Albums;
import com.ndtv.core.config.model.PhotoResults;
import com.ndtv.core.util.GsonRequest;


public class PhotosAlbumsHandler {

    String url;
    int pageNumber;
    boolean isCacheRequst;
    Context mContext;
    ResultReceiver mResultreceiver;
    //private PhotoFeedItem photoFeedItem;
    public PhotoResults photoResultsData = new PhotoResults();

    public PhotosAlbumsHandler(ResultReceiver resultreceiver, Context ctx, String rssUrl, boolean flag) {
        this.mContext = ctx;
        this.isCacheRequst = flag;
        this.mResultreceiver = resultreceiver;
        this.url = rssUrl;
    }

    public void downloadFeed() {

        GsonRequest<Albums> gsonRequest = new GsonRequest<Albums>(url, Albums.class, null, new Response.Listener<Albums>() {
            @Override
            public void onResponse(Albums albums) {

                if (albums.results != null) {
                    photoResultsData.setCount(albums.results.getCount());
                    photoResultsData.setDescription(albums.results.getDescription());
                    photoResultsData.setId(albums.results.getId());
                    photoResultsData.setLink(albums.results.getLink());
                    photoResultsData.setPhotos(albums.results.getPhotos());
                    photoResultsData.setPubDate(albums.results.getPubDate());
                    photoResultsData.setTitle(albums.results.getTitle());
                    photoResultsData.setThumbimage(albums.results.getThumbimage());

                }
                mResultreceiver.send(2, Bundle.EMPTY);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {


            }
        });

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(gsonRequest);
    }

}
