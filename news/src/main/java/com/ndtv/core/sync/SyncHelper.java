package com.ndtv.core.sync;

import android.content.Context;
import android.content.Intent;

import com.ndtv.core.util.DetachableResultReceiver;

/**
 * Created by Srihari S Reddy on 13/01/15.
 */
public class SyncHelper {

    private static SyncHelper sSyncHelper = null;
    private Context mContext;

    private SyncHelper(Context context) {
        mContext = context.getApplicationContext();
    }

    public static SyncHelper getSyncHelper(Context context) {
        if (sSyncHelper == null) {
            sSyncHelper = new SyncHelper(context);
        }
        return sSyncHelper;
    }


    public void requestFeed(DetachableResultReceiver detachableresultreceiver, String s, int i, boolean flag, String section) {
        Intent intent = new Intent("android.intent.action.SYNC", null, mContext, SyncService.class);
        intent.putExtra("com.july.ndtv.STATUS_RECEIVER", detachableresultreceiver);
        intent.putExtra("com.july.ndtv.EXTRA_FEED_TYPE", "newsList");
        intent.putExtra("com.july.ndtv.EXTRA_FEED_SLUG", s);
        intent.putExtra("com.july.ndtv.EXTRA_FEED_PAGE_NUMBER", i);
        intent.putExtra("com.july.ndtv.EXTRA_OFFLINE_ENABLED", flag);
        intent.putExtra("com.july.ndtv.EXTRA_SECTION", section);
        mContext.startService(intent);
    }
}
