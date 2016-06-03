package com.ndtv.core.radio;

/**
 * Created by sangeetha on 30/1/15.
 */
public class LiveRadioManager {
    private static LiveRadioManager sLiveRadioManager;
    private int mVolumeProgress = -1; //By default volume progress
    private boolean mIsFromNotifictn = false;

    public static synchronized LiveRadioManager getLiveRadioManagerInstance() {
        if (null == sLiveRadioManager) {
            sLiveRadioManager = new LiveRadioManager();
        }
        return sLiveRadioManager;
    }


    public int getVolumeProgress() {
        return mVolumeProgress;
    }

    public void setVolumeProgress(int volumeProgress) {
        mVolumeProgress = volumeProgress;
    }

    public void setFromNotification(boolean fromNotification) {
        mIsFromNotifictn = fromNotification;
    }

    public boolean isIsFromNotifictn() {
        return mIsFromNotifictn;
    }

}
