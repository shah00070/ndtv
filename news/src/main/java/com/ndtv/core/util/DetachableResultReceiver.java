package com.ndtv.core.util;

/**
 * Created by Srihari S Reddy on 15/01/15.
 */


import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.makeLogTag;

public class DetachableResultReceiver extends ResultReceiver {
    private static final String TAG = makeLogTag(DetachableResultReceiver.class);

    public static interface Receiver {

        public abstract void onReceiveResult(int i, Bundle bundle);
    }


    private Receiver mReceiver;

    public DetachableResultReceiver(Handler handler) {
        super(handler);
    }

    public void clearReceiver() {
        mReceiver = null;
    }

    protected void onReceiveResult(int i, Bundle bundle) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(i, bundle);
            return;
        } else {
            LOGD(TAG, (new StringBuilder()).append("Dropping result on floor for code ").append(i).append(": ").append(bundle.toString()).toString());
            return;
        }
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }
}
