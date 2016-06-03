package com.ndtv.core.deeplinking.io;

import android.content.Context;

import com.android.volley.Response;

/**
 * Created by sangeetha on 26/2/15.
 */
public class DeepLinkingManager {
    private static DeepLinkingManager sDeeplinkManager;

    private boolean mIsFromDL = false;
    private int mPos=0;


    private DeepLinkingManager() {
    }

    public synchronized static DeepLinkingManager getInstance() {
        if (null == sDeeplinkManager) {
            sDeeplinkManager = new DeepLinkingManager();
        }
        return sDeeplinkManager;
    }


    public void setFromDeepLink(boolean value) {
        mIsFromDL = value;
    }

    public Boolean isFromDeepLink() {
        return mIsFromDL;
    }

    public void saveItemPos(int pos) {
        mPos = pos;
    }

    public int geItemPos() {
        return mPos;
    }

    //If cache is cleared,download config
    public void downloadConfig(Context context, String url, Response.Listener listener, Response.ErrorListener errorListener) {
        DeeplinkingConnectnMngr deeplinkingConnectnMngr = DeeplinkingConnectnMngr.getInstance();
        deeplinkingConnectnMngr.downloadConfig(context, url, listener, errorListener);
    }


    }

