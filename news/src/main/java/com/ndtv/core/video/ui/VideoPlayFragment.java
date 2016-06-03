package com.ndtv.core.video.ui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.ndtv.core.R;
import com.ndtv.core.util.LogUtils;

/**
 * Created by laveen on 10/3/15.
 */
public class VideoPlayFragment extends Fragment implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,VideoControllerView.MediaPlayerControl, View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener, AudioManager.OnAudioFocusChangeListener {

    public static final String VIDEO_PLAY_URL = "play_url";
    public static final String VIDEO_PLAY_CURRENT_POSITION = "play_position";
    public static final String IS_LIVE_TV = "is_live_tv";
    private static final String TAG = "VideoPlayFragment";
    private ProgressBar mLoadingBar;

    private String mVideoUrl;

    private int mVideoPosition;

    private AudioManager mAudioManager;

    protected VideoView mVideoView;

    protected MediaPlayer mMediaPlayer;

    protected VideoControllerView mController;

    protected FrameLayout mMediaContainer;

    protected boolean mIsFullScreen;
    protected boolean mIsLiveTv;



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractBundleData();

        if (savedInstanceState != null)
            mVideoPosition = savedInstanceState.getInt("seek_position", 0);
    }

    public void extractBundleData() {
        Bundle bundle = getArguments();
        mVideoUrl = bundle.getString(VIDEO_PLAY_URL);
        mVideoPosition = bundle.getInt(VIDEO_PLAY_CURRENT_POSITION, 0);
        mIsLiveTv=bundle.getBoolean(IS_LIVE_TV,false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_play, container, false);
        initViews(view);
        return view;
    }

    protected void initViews(View view) {
        mVideoView = (VideoView) view.findViewById(R.id.video_view);
        mMediaContainer = (FrameLayout) view.findViewById(R.id.media_container);
        mLoadingBar = (ProgressBar) view.findViewById(R.id.loading_indicator);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mVideoView.setOnTouchListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        showLoadingBar();
        Log.d("VDIEOPLAY", "POSITION RESUME:" + mVideoPosition);

        if (!TextUtils.isEmpty(mVideoUrl)) {
            if(getActivity()!=null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoView.setVideoURI(Uri.parse(mVideoUrl));
                    }
                });
            }
            else
            {
                mVideoView.setVideoURI(Uri.parse(mVideoUrl));

            }
            LogUtils.LOGD(TAG, "duration" + mVideoView.getDuration());
            if(!mIsLiveTv)
            mVideoView.seekTo(mVideoPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("seek_position", mVideoPosition);
    }

    @Override
    public void onStop() {
        super.onStop();
        //mVideoPosition = getCurrentPosition();
        Log.d("VDIEOPLAY", "POSITION STOP:" + mVideoPosition);
        reset();
    }

    private void reset() {
        //  mVideoPosition = getCurrentPosition();
        mAudioManager.abandonAudioFocus(this);
        // mVideoView.stopPlayback();
        mMediaPlayer = null;

        if (mController != null) {
            mController.hide();
            mController.setMediaPlayer(null);
            mController = null;
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        int status = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mp.start();

            mMediaPlayer = mp;

            mController = new VideoControllerView(getActivity());
            mController.setMediaPlayer(this);
            mController.setAnchorView(mMediaContainer);
            mController.show();

            if (getActivity() != null)
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        }

        hideLoadingBar();
    }

    @Override
    public void start() {
        try {
            mMediaPlayer.start();
        } catch (IllegalStateException ex) {

        }
    }

    @Override
    public void pause() {
        try {
            mMediaPlayer.pause();
        } catch (IllegalStateException ex) {

        }
    }

    @Override
    public int getDuration() {
        try {
            return mMediaPlayer.getDuration();
        } catch (IllegalStateException ex) {

        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        try {
            if (mMediaPlayer != null)
                return mVideoPosition = mMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException ex) {

        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        try {
            if(!mIsLiveTv)
                mMediaPlayer.seekTo(pos);
        } catch (IllegalStateException ex) {
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            return mMediaPlayer.isPlaying();
        } catch (IllegalStateException ex) {

        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public boolean isFullScreen() {
        return mIsFullScreen;
    }

    @Override
    public void toggleFullScreen() {
        mIsFullScreen = !mIsFullScreen;
        if (mIsFullScreen) {
            //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            if (mController != null) {
                if (mController.isShowing())
                    mController.hide();
                else
                    mController.show();
            }
        } catch (IllegalStateException ex) {
        }
        return false;
    }

    @Override
    public void onGlobalLayout() {
        ViewGroup.LayoutParams params = mVideoView.getLayoutParams();
        params.height = (int) (mVideoView.getWidth() * .75);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mVideoView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else {
            mVideoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            Log.d("FOCUS", "AUDIOFOCUS_LOSS_TRANSIENT");
            mVideoView.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            Log.d("FOCUS", "AUDIOFOCUS_GAIN");
            // mVideoView.resume();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            Log.d("FOCUS", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            //mVideoView.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            Log.d("FOCUS", "AUDIOFOCUS_LOSS");
            //  mAudioManager.abandonAudioFocus(this);
            // mVideoView.stopPlayback();
        }
    }

    protected void showLoadingBar() {
        mLoadingBar.setVisibility(View.VISIBLE);
    }

    protected void hideLoadingBar() {
        mLoadingBar.setVisibility(View.GONE);
    }


    private void makeVideoFullScreen() {
        ViewGroup.LayoutParams params = mVideoView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mController != null)
            mController.show();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mIsFullScreen = true;
            makeVideoFullScreen();
        } else {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getActivity().getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mIsFullScreen = false;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogUtils.LOGE(TAG, "error encountered " + what+"some more detail "+extra);
        return true;
    }
}
