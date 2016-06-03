package com.ndtv.core.sync;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.ndtv.core.io.NewsFeedHandler;

import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Srihari S Reddy on 13/01/15.
 */
public class SyncService extends IntentService {

    private static final String TAG = makeLogTag(SyncService.class);

    public SyncService() {
        super("NDTV-News-SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getExtras() != null)
            Log.d("DOWNLOAD", "URL:" + intent.getExtras().getString("com.july.ndtv.EXTRA_FEED_SLUG"));
        LOGD(TAG, new StringBuilder().append("SyncService: onHandleIntent(intent=").append(intent.toString()).append(")").toString());
        ResultReceiver resultreceiver;
        resultreceiver = (ResultReceiver) intent.getParcelableExtra("com.july.ndtv.STATUS_RECEIVER");

        // resultreceiver.send(1, Bundle.EMPTY);

        executeMethod(intent, resultreceiver);

        LOGD(TAG, "SyncService: sync finished successfully");

    }

    private Bundle executeMethod(Intent intent, ResultReceiver resultreceiver) {

        Bundle bundle = intent.getExtras();
        switch (bundle.getString("com.july.ndtv.EXTRA_FEED_TYPE")) {

            case "newsList":
                new NewsFeedHandler(resultreceiver, this, bundle.getString("com.july.ndtv.EXTRA_FEED_SLUG"), bundle.getInt("com.july.ndtv.EXTRA_FEED_PAGE_NUMBER"), bundle.getBoolean("com.july.ndtv.EXTRA_OFFLINE_ENABLED"), bundle.getString("com.july.ndtv.EXTRA_SECTION")).downloadFeed();

            default:

        }
        return null;
    }


}
