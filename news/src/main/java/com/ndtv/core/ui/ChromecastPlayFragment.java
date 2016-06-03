package com.ndtv.core.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;

/**
 * Created by laveen on 27/2/15.
 */
public abstract class ChromecastPlayFragment extends ChromeCastFragment {
    protected VideoCastConsumerImpl mCastConsumer;

    protected MediaInfo mSelectedMedia;

    protected abstract MediaInfo getMediaInfo();

    protected int getSeekPosition() {
        return 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastConsumer = getCastConsumer();
    }

    protected void playVideoRemote() {
        try {
            mSelectedMedia = getMediaInfo();
            if (mSelectedMedia != null)
                mCastManager.startCastControllerActivity(getActivity(), mSelectedMedia, getSeekPosition(), true);
            if (getActivity() != null)
                getActivity().finish();
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "Exception", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCastManager.addVideoCastConsumer(mCastConsumer);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCastConsumer = null;
    }

    //override this in derived class to make changes when the device is connected to cast device
    protected VideoCastConsumerImpl getCastConsumer() {
        return new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
                playVideoRemote();
            }

            @Override
            public void onFailed(int resourceId, int statusCode) {
                Toast.makeText(getActivity(), "FAILED", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
