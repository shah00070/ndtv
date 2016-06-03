package com.ndtv.core.deeplinking.io;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.Response;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.common.util.GsonObjectRequest;
import com.ndtv.core.config.model.Albums;
import com.ndtv.core.config.model.Configuration;

/**
 * Created by sangeetha on 2/3/15.
 */
public class DeeplinkingConnectnMngr {
    private static DeeplinkingConnectnMngr sDeepLinkConnctnMngr;

    public static DeeplinkingConnectnMngr getInstance() {
        if (sDeepLinkConnctnMngr == null) {
            sDeepLinkConnctnMngr = new DeeplinkingConnectnMngr();
        }
        return sDeepLinkConnctnMngr;
    }

    private DeeplinkingConnectnMngr() {
    }


    public void downloadConfig(Context context, String url, Response.Listener listener, Response.ErrorListener errorListener) {
        if (!TextUtils.isEmpty(url)) {
            NdtvApplication ndtvApplication = NdtvApplication.getApplication(context);
            GsonObjectRequest<Configuration> configGsonObjectRequest = new GsonObjectRequest<Configuration>(Request.Method.GET, url, Configuration.class,
                    null, listener, errorListener, ndtvApplication);
            ndtvApplication.mRequestQueue.add(configGsonObjectRequest);
        }
    }
}
