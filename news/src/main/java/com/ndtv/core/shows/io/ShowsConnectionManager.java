/**
 Project      : Awaaz
 Filename     : ShowsConnectionManager.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.shows.io;

import android.content.Context;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.common.util.GsonObjectRequest;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.shows.dto.PrimeShows;
import com.ndtv.core.shows.dto.PrimeVideos;

/**
 * @author Chandan kumar
 */
public class ShowsConnectionManager {

    public void downloadShows(String url, final Context ctx, Response.Listener<PrimeShows> listener,
                              ErrorListener errorListener, Context context) {
        url = Utility.getFinalUrl(url, context);
        final NdtvApplication application = NdtvApplication.getApplication(ctx);
        final GsonObjectRequest<PrimeShows> jsObjRequest = new GsonObjectRequest<PrimeShows>(Method.GET, url,
                PrimeShows.class, null, listener, errorListener, ctx.getApplicationContext());
        application.mRequestQueue.add(jsObjRequest);
    }

    public void downloadPrimeVideos(String url, final Context ctx, Response.Listener<PrimeVideos> listener,
                                    ErrorListener errorListener) {
        url = Utility.getFinalUrl(url, ctx, "1");
        final NdtvApplication application = NdtvApplication.getApplication(ctx);
        final GsonObjectRequest<PrimeVideos> jsObjRequest = new GsonObjectRequest<PrimeVideos>(Method.GET, url,
                PrimeVideos.class, null, listener, errorListener, ctx.getApplicationContext());
        application.mRequestQueue.add(jsObjRequest);
    }
}
