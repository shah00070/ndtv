package com.ndtv.core.livetv.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.cast.MediaInfo;
import com.ndtv.core.R;
import com.ndtv.core.ui.ChromecastPlayFragment;
import com.ndtv.core.util.ChromecastUtil;
import com.ndtv.core.video.ui.VideoPlayFragment;

/**
 * Created by laveen on 09-02-2015.
 */
public class LiveTvPlayFragment extends ChromecastPlayFragment {
    private static final String TAG = "Live Tv Play";

    public static String LIVETV_URL = "live_tv_url";
    public static String LIVETV_SHOW_NAME = "live_tv_show";
    public static String LIVETV_SHOW_IMAGE_URL = "live_show_image";
    public static String LIVETV_NAME = "live_tv_name";

    protected String mLiveTvUrl;
    protected String mLiveTvName;
    protected String mLiveTvShowName;
    protected String mLiveTvShowImageUrl;

    public static Fragment getInstance(String liveTvName, String liveTvurl, String showName, String showUrl) {
        Fragment fragment = new LiveTvPlayFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(LIVETV_NAME, liveTvName);
        bundle.putString(LIVETV_URL, liveTvurl);
        bundle.putString(LIVETV_SHOW_NAME, showName);
        bundle.putString(LIVETV_SHOW_IMAGE_URL, showUrl);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected MediaInfo getMediaInfo() {
        return ChromecastUtil.createMediaInfo(mLiveTvUrl, mLiveTvName, mLiveTvShowName, mLiveTvShowImageUrl, null);
    }

    @Override
    protected int getSeekPosition() {
        return 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLiveTvUrl = bundle.getString(LIVETV_URL);
            mLiveTvName = bundle.getString(LIVETV_NAME);
            mLiveTvShowName = bundle.getString(LIVETV_SHOW_NAME);
            mLiveTvShowImageUrl = bundle.getString(LIVETV_SHOW_IMAGE_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_livetv_play, container, false);
        return view;
    }

    private void playVideo() {
        if (!TextUtils.isEmpty(mLiveTvUrl)) {
            if (isAppConnectedToCastDevice()) {
                playVideoRemote();
            } else {
                addVideoPlayFragment();
            }
        }
    }

    protected void addVideoPlayFragment() {
        FragmentManager manager = getChildFragmentManager();
        if (manager.findFragmentById(R.id.media_container) == null) {

            FragmentTransaction transaction = manager.beginTransaction();

            Fragment fragment = new VideoPlayFragment();
            Bundle bundle = new Bundle(1);
            bundle.putString(VideoPlayFragment.VIDEO_PLAY_URL, mLiveTvUrl);
            bundle.putBoolean(VideoPlayFragment.IS_LIVE_TV, true);
            fragment.setArguments(bundle);

            transaction.replace(R.id.media_container, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        playVideo();
    }

    @Override
    public void setScreenName() {
        setScreenName(TAG + " - " + mLiveTvName);
    }

   /* @Override
    protected VideoCastConsumerImpl getCastConsumer() {
        return super.getCastConsumer();
    }*/
}
