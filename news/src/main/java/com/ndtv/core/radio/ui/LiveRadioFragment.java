package com.ndtv.core.radio.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.XMLRequest;
import com.ndtv.core.common.util.views.RobotoMediumTextView;
import com.ndtv.core.common.util.views.RobotoRegularTextView;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.radio.LiveRadioManager;
import com.ndtv.core.radio.dto.LiveRadioSchedules;
import com.ndtv.core.radio.dto.ProgramItem;
import com.ndtv.core.radio.services.LiveRadioService;
import com.ndtv.core.radio.services.LiveRadioServiceNewApi;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.util.ApplicationUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sangeetha on 29/1/15.
 */
public class LiveRadioFragment extends BaseFragment implements Response.Listener<LiveRadioSchedules>, Response.ErrorListener {

    private static final String LOG_TAG = "RadioFragment ";

    private String mRadioUrl;
    private String mSectionTitle;
    private String mSchedule; //schedule detail of the Radio
    private int mSectionPos = 0;
    private int mNavPos;
    private NetworkImageView mRadioIV;
    private TextView mShowTitleTV;
    private TextView mComingUpNextTV;
    private ImageView mPlayPauseIV;
    private SeekBar mVolumeSeekBar;
    private int mIncrementor = 0;
    public static final String DATE_FORMAT = "HH:mm";
    private ProgramItem mNextProgram; //to store next Program item
    private AudioManager mAudioManager = null;
    private View.OnClickListener mOnPlayClkdListnr;
    private boolean mIsDataAvailable = false;
    private RadioReciever mRadioReciever;
    private ProgressBar mProgressBar;
    private boolean mIsVisible = false;

    private LiveRadioSchedules mRadioSchedules;
    private boolean bFragmentAttached = false;
    private static String TAG = "radio";
    private XMLRequest<LiveRadioSchedules> request;
    private BannerAdFragment.AdListener mAdUpdateListener;
    private String navigation;

    public static interface LiveRadioConstants {
        String RADIO_URL = "radio_url";
        String RADIO_TITLE = "radio_title";
        String LIVE_RADIO_SECTION_POSITION = "live_radio_section_position";
        String LIVE_RADIO_SCHEDULE = "live_radio_schedule";
        String LIVE_RADIO_PUSH = "live_radio_push_notification";
        String LIVE_RADIO_PLAY = "live_radio_play";
        String LIVE_RADIO_ERROR = "live_radio_error";
        String ACTION_PAUSE_PLAY = "com.july.ndtv.action.PAUSE_PLAY";
        String LAUNCH_SERVICE = "com.july.ndtv.launch.SERVICE";
        String UPDATE_UI = "com.july.ndtv.update.UI";
        String ACTION_START = "com.july.ndtv.action.START";
        String ACTION_PAUSE = "com.july.ndtv.action.PAUSE";
        String ACTION_STOP = "com.july.ndtv.action.STOP";
        String ACTION_RESUME = "com.july.ndtv.action.RESUME";
        String HIDE_PROGRESS_BAR = "com.july.ndtv.action.HIDE";
        String ENABLE_PAUSE_PLAY = "com.july.ndtv.action.ENABLE_PAUSE_PLAY";
        String ACTION_STOP_SERVICE = "com.july.ndtv.action.STOP_SERVICE";
        String FROM_NOTIFICATION = "from_radio_notificatio";
        String IS_VISIBLE = "is_visible_to_user";
        String LIVE_RADIO_STOPPED = "is_radio_stopped";
        String RADIO_BUFFERING_START = "com.july.ndtv.action.RADIO_BUFFERING_START";
        String RADIO_BUFFERING_END = "com.july.ndtv.action.RADIO_BUFFERING_END";
    }

    public static Fragment newInstance(String url, int position, String title, String schedule, int navPos) {
        LiveRadioFragment newsListingFragment = new LiveRadioFragment();
        Bundle bundle = new Bundle();
        bundle.putString(LiveRadioConstants.RADIO_URL, url);
        bundle.putString(LiveRadioConstants.RADIO_TITLE, title);
        bundle.putString(LiveRadioConstants.LIVE_RADIO_SCHEDULE, schedule);
        bundle.putInt(LiveRadioConstants.LIVE_RADIO_SECTION_POSITION, position);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, navPos);
        newsListingFragment.setArguments(bundle);
        return newsListingFragment;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bFragmentAttached = true;
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRadioReciever = new RadioReciever();
        extractArguments();
        navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavPos);
        //Hide the action bar icons.
        getActivity().invalidateOptionsMenu();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.live_radio, container, false);
        initViews(view);
        initListeners();
        return view;
    }

    @Override
    public void setScreenName() {
        setScreenName(navigation + " - " + mSectionTitle);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (request != null)
            request.cancel();
        mIsVisible = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        cancelTask();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        bFragmentAttached = false;
    }


    private void extractArguments() {
        if (null != getArguments()) {
            mRadioUrl = getArguments().getString(LiveRadioConstants.RADIO_URL);
//mRadioUrl="http://202.77.113.10:8000/b1bahasa";
            mSectionTitle = getArguments().getString(LiveRadioConstants.RADIO_TITLE);
            mSchedule = getArguments().getString(LiveRadioConstants.LIVE_RADIO_SCHEDULE);
            mSectionPos = getArguments().getInt(LiveRadioConstants.LIVE_RADIO_SECTION_POSITION);
            mNavPos = getArguments().getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS);
        }
    }

    private void initViews(View view) {
        mRadioIV = (NetworkImageView) view.findViewById(R.id.show_image);
        mShowTitleTV = (RobotoMediumTextView) view.findViewById(R.id.now_playing_text);
        mComingUpNextTV = (RobotoRegularTextView) view.findViewById(R.id.coming_up_next);
        mPlayPauseIV = (ImageView) view.findViewById(R.id.play_pause_iv);
        mVolumeSeekBar = (SeekBar) view.findViewById(R.id.volume_seek_bar);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        updateViewHeightToAspectRatio(mRadioIV);
        updateViewMargin(mPlayPauseIV);
    }

    private void initListeners() {
        setSeekBarListener();
        mOnPlayClkdListnr = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.play_pause_iv)
                    handlePlayButtonClick();
            }
        };
    }


    private void updateViewHeightToAspectRatio(final View view) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                //set the view height based on aspect ratio
                Log.d("VIEWHEIGHT", "onPreDraw:" + view);
                params.height = (int) (view.getWidth() * 0.62);
                Log.d("VIEWHEIGHT", "onPreDraw:" + params.height);
                view.setLayoutParams(params);
                return true;
            }
        });
    }


    private void updateViewMargin(final View view) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                params.bottomMargin = -(view.getHeight() / 2);
                view.setLayoutParams(params);
                return true;
            }
        });
    }

    private void handlePlayButtonClick() {

        if (Utility.isInternetOn(getActivity())) {


            if (mRadioUrl != null) {

                if (PreferencesManager.getInstance(getActivity()).isLiveRadioPlaying()) {
                    pauseRadio();
                    mPlayPauseIV.setSelected(false);
//                    mPlayPauseIV.setImageResource(R.drawable.icn_play_radio);
                } else {
                    resumeRadio();
                    mPlayPauseIV.setSelected(true);
//                    mPlayPauseIV.setImageResource(R.drawable.icn_pause_radio);

                }
            }

        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hidePausePlaybtn();
        downloadLiveRadioData();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            if (LiveRadioManager.getLiveRadioManagerInstance() != null && mAudioManager != null && mVolumeSeekBar != null) {
                int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (LiveRadioManager.getLiveRadioManagerInstance().getVolumeProgress() != currentVolume) {
                    mVolumeSeekBar.setProgress(currentVolume);
                    LiveRadioManager.getLiveRadioManagerInstance().setVolumeProgress(currentVolume);
                } else
                    mVolumeSeekBar.setProgress(LiveRadioManager.getLiveRadioManagerInstance().getVolumeProgress());
            }
        }
        if (getUserVisibleHint() && mIsDataAvailable) {

            if (PreferencesManager.getInstance(getActivity()) != null &&
                    PreferencesManager.getInstance(getActivity()).isLiveRadioStopped() && mRadioUrl != null) {
                playRadio();
                hidePausePlaybtn();
                showProgressBar();
            }
//            else if(PreferencesManager.getInstance(getActivity()) != null && PreferencesManager.getInstance(getActivity()).isLiveRadioPlaying() && mRadioUrl != null){
//                playRadio();
//                hidePausePlaybtn();
//                showProgressBar();
//            }

        }
        mIsVisible = true;


    }


    private void registerReciever(boolean visible) {
        if (visible) {

//            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRadioReciver, new IntentFilter(LiveRadioConstants.UPDATE_UI));
            IntentFilter intent = new IntentFilter();
            intent.addAction(LiveRadioConstants.HIDE_PROGRESS_BAR);
            intent.addAction(LiveRadioConstants.ENABLE_PAUSE_PLAY);
            intent.addAction(LiveRadioConstants.UPDATE_UI);
            intent.addAction(LiveRadioConstants.IS_VISIBLE);
            intent.addAction(LiveRadioConstants.RADIO_BUFFERING_START);
            intent.addAction(LiveRadioConstants.RADIO_BUFFERING_END);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRadioReciever, intent);

            Log.d("RADIO", "Registered:" + this);

        } else {

            if (mRadioReciever != null) {
                Log.d("RADIO", "Un Registered:" + this);

//                LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRadioReciver);
                LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRadioReciever);
            }
        }


    }


    private void setSeekBarListener() {

        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int volume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2; // max stream volume
        mVolumeSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));


        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                Log.d("radio", "AudioMngr" + mAudioManager);
                mVolumeSeekBar.setIndeterminate(false);
                mVolumeSeekBar.setProgress(progress);
                LiveRadioManager.getLiveRadioManagerInstance().setVolumeProgress(progress);
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (getUserVisibleHint()) {
            if (!LiveRadioManager.getLiveRadioManagerInstance().isIsFromNotifictn()) {
                if (LiveRadioManager.getLiveRadioManagerInstance().getVolumeProgress() < 0) {
                    mVolumeSeekBar.setProgress(volume);
                    LiveRadioManager.getLiveRadioManagerInstance().setVolumeProgress(volume);
                } else {
                    mVolumeSeekBar.setProgress(LiveRadioManager.getLiveRadioManagerInstance().getVolumeProgress());
                    LiveRadioManager.getLiveRadioManagerInstance().setVolumeProgress(LiveRadioManager.getLiveRadioManagerInstance().getVolumeProgress());
                }
            }

        }
    }

    private void downloadLiveRadioData() {
        if (null != getActivity()) {
            if (Utility.isInternetOn(getActivity())) {
                getLiveRadioData();


            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        registerReciever(isVisibleToUser);
        if (isVisibleToUser) {
            //If add is visible
            hideBannerAd();
            //send a request to laod new ad
            loadBannerAd();
        }
        if (isVisibleToUser && mIsDataAvailable) {
            // super.setUserVisibleHint(isVisibleToUser);
            showProgressBar();
            hidePausePlaybtn();
            //To retain the volume the user has set previously
            if (LiveRadioManager.getLiveRadioManagerInstance() != null)
                if (mVolumeSeekBar != null)
                    mVolumeSeekBar.setProgress(LiveRadioManager.getLiveRadioManagerInstance().getVolumeProgress());

            playRadio();
            Log.d("Url:", mRadioUrl);
            Log.d("Section title:", mSectionTitle);
            Log.d("Visible", "");
        } else
            // super.setUserVisibleHint(isVisibleToUser);

            // registerReciever(isVisibleToUser);

            Log.d(TAG, "Invisible");


    }

    public void handleUI() {
        PreferencesManager prefMngr = PreferencesManager.getInstance(getActivity());

        if (prefMngr.getSectionPositionLiveRadio() == mSectionPos) {

//            mPlayPauseIV.setImageResource(R.drawable.icn_pause_radio);
            mPlayPauseIV.setSelected(true);


        } else {
//            mPlayPauseIV.setImageResource(R.drawable.icn_play_radio);
            mPlayPauseIV.setSelected(false);
        }
        mVolumeSeekBar.setProgress(LiveRadioManager.getLiveRadioManagerInstance().getVolumeProgress());
    }

    private void getLiveRadioData() {
        // TODO trackLoadTime
        getLiveRadioSchedules(mSchedule);

    }

    private void getLiveRadioSchedules(String scheduleURL) {
        if (!TextUtils.isEmpty(scheduleURL)) {
            request = new XMLRequest<LiveRadioSchedules>(Request.Method.GET, scheduleURL, LiveRadioSchedules.class, this, this);
            request.setTag(scheduleURL);
            VolleyRequestQueue.getInstance(getActivity().getApplicationContext()).addToRequestQueue(request);
        }

    }


    @Override
    public void onErrorResponse(VolleyError volleyError) {
        mIsDataAvailable = false;
        if (bFragmentAttached)
            Toast.makeText(getActivity(), "Download Failed,Check your network Connection", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResponse(LiveRadioSchedules liveRadioSchedules) {
        if (null != liveRadioSchedules) {
            mIsDataAvailable = true;
            mRadioSchedules = liveRadioSchedules;
            ProgramItem currentProgram = calculateCurrentProgram(liveRadioSchedules);
            if (null != currentProgram && bFragmentAttached) {
                handleUI();
                handleLiveRadioLaunch();
                updateViews(currentProgram);
            }


        }


    }

    private ProgramItem calculateCurrentProgram(final LiveRadioSchedules liveRadioSchedules) {

        mIncrementor = 0;
        final Calendar currentCalender = Calendar.getInstance(Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        ProgramItem currentProgramItem = null;

        for (ProgramItem programItem : liveRadioSchedules.schedule.programList) {
            mIncrementor++;
            try {
                String timestamp = programItem.timestamp;
                Date radioDate = dateFormat.parse(timestamp);
                Calendar radioCalender = Calendar.getInstance();
                radioCalender.setTime(radioDate);

                int radioHour = radioCalender.get(Calendar.HOUR_OF_DAY);
                int radioMin = radioCalender.get(Calendar.MINUTE);
                int currentHour = currentCalender.get(Calendar.HOUR_OF_DAY);
                int currentMin = currentCalender.get(Calendar.MINUTE);

                if (radioHour == currentHour) {

                    if (radioMin == currentMin) {

                        currentProgramItem = programItem;
                        break;

                    } else if (radioMin < 30 && (currentMin - radioMin) < 30) {

                        currentProgramItem = programItem;
                        break;

                    } else if (radioMin >= 30) {

                        currentProgramItem = programItem;
                        break;

                    } else {

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return currentProgramItem;

    }


    private void updateViews(ProgramItem programItem) {
        Log.d(TAG, "Upadting the views");
//        mPlayPauseIV.setVisibility(View.VISIBLE);
//        if (PreferencesManager.getInstance(getActivity()).isLiveRadioPlaying() && getUserVisibleHint())
//            showPausePlayBtn();
//        else
        hidePausePlaybtn();
        ImageLoader imageLoader = VolleyRequestQueue.getInstance(getActivity()).getImageLoader();
        NetworkImageView.class.cast(mRadioIV).setImageUrl(programItem.image, imageLoader);

        mShowTitleTV.setText(Utility.decodeString(Html.fromHtml(programItem.name).toString()));
        if (mIncrementor < mRadioSchedules.schedule.programList.size())
            mNextProgram = mRadioSchedules.schedule.programList.get(mIncrementor);
        else
            mNextProgram = mRadioSchedules.schedule.programList.get(0);
        mComingUpNextTV.setText(getActivity().getResources().getString(R.string.live_radio_coming_up_next_text) + " "
                + Utility.decodeString(Html.fromHtml(mNextProgram.name).toString()));

        mPlayPauseIV.setTag(mIncrementor);
        mPlayPauseIV.setOnClickListener(mOnPlayClkdListnr);
        Log.d(TAG, "Views updated");

    }

    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    private void handleLiveRadioLaunch() {

        Log.d(TAG, "Launch radio");
        final PreferencesManager prefMngr = PreferencesManager.getInstance(getActivity());

        String url = prefMngr.getLiveRadioUrl();


        if (!url.equals(mRadioUrl) && !mRadioUrl.equalsIgnoreCase("")) {


            showProgressBar();
            playRadio();


        } else if (url.equals(mRadioUrl)) {
            showProgressBar();
            playRadio();
        } else {
            showProgressBar();
            playRadio();
//            mPlayPauseIV.setImageResource(R.drawable.icn_pause_radio);
            mPlayPauseIV.setSelected(true);
//            startTimerTask();
            Log.d(TAG, "Radio Launched");
        }

    }

    private void playRadio() {
        if (getUserVisibleHint()) {

//            startTimerTask();
            Intent intent;
            if (ApplicationUtils.isDeviceFitForExoplayer()) {
                intent = new Intent(getActivity(), LiveRadioServiceNewApi.class);
            } else {
                intent = new Intent(getActivity(), LiveRadioService.class);
            }
            intent.setAction(LiveRadioConstants.ACTION_START);
            intent.putExtra(LiveRadioConstants.RADIO_URL, mRadioUrl);
            intent.putExtra(LiveRadioConstants.RADIO_TITLE, mSectionTitle);
            intent.putExtra(LiveRadioConstants.LIVE_RADIO_SECTION_POSITION, mSectionPos);
            getActivity().startService(intent);

        }
//        cancelTask();
    }


    private void resumeRadio() {
        Intent intent;
        if (ApplicationUtils.isDeviceFitForExoplayer()) {
            intent = new Intent(getActivity(), LiveRadioServiceNewApi.class);
        } else {
            intent = new Intent(getActivity(), LiveRadioService.class);
        }
        intent.setAction(LiveRadioConstants.ACTION_RESUME);
        getActivity().startService(intent);
    }

    private void pauseRadio() {
        Intent intent;
        if (ApplicationUtils.isDeviceFitForExoplayer()) {
            intent = new Intent(getActivity(), LiveRadioServiceNewApi.class);
        } else {
            intent = new Intent(getActivity(), LiveRadioService.class);
        }
        intent.setAction(LiveRadioConstants.ACTION_PAUSE);
        getActivity().startService(intent);

    }

    private void stopRadio() {
//        cancelTask();
        Log.d(TAG, "Timer cancelled");
        PreferencesManager prefMngr = PreferencesManager.getInstance(getActivity());
        prefMngr.setLiveRadioUrl("");
        Intent intent;
        if (ApplicationUtils.isDeviceFitForExoplayer()) {
            intent = new Intent(getActivity(), LiveRadioServiceNewApi.class);
        } else {
            intent = new Intent(getActivity(), LiveRadioService.class);
        }
        intent.setAction(LiveRadioConstants.ACTION_STOP);
        getActivity().stopService(intent);

    }

    private void updatePausePlayBtn() {
        if (PreferencesManager.getInstance(getActivity()) != null) {
            if (PreferencesManager.getInstance(getActivity()).isLiveRadioPlaying()) {
                mPlayPauseIV.setSelected(true);
            }
//                mPlayPauseIV.setImageResource(R.drawable.icn_pause_radio);

            else {
                mPlayPauseIV.setSelected(false);
            }
//                mPlayPauseIV.setImageResource(R.drawable.icn_play_radio);

        }
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);

    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }


    public void onKeyUp() {
        int progress = LiveRadioManager.getLiveRadioManagerInstance().getVolumeProgress();
        progress = progress + 1;

        if (progress <= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            mVolumeSeekBar.setProgress(progress);
            LiveRadioManager.getLiveRadioManagerInstance().setVolumeProgress(progress);
        }
    }

    public void onKeyDown() {
        int progress = LiveRadioManager.getLiveRadioManagerInstance().getVolumeProgress();
        progress = progress - 1;

        if (progress >= 0) {
            mVolumeSeekBar.setProgress(progress);
            LiveRadioManager.getLiveRadioManagerInstance().setVolumeProgress(progress);
        }
    }

    private void hidePausePlaybtn() {
        mPlayPauseIV.setVisibility(View.GONE);
    }

    private void showPausePlayBtn() {
        mPlayPauseIV.setVisibility(View.VISIBLE);
    }

    private void loadBannerAd() {
        if (mAdUpdateListener != null)
            mAdUpdateListener.loadBannerAd(mNavPos, mSectionPos, null, false, -1, false, false);
    }

    private void hideBannerAd() {
        if (mAdUpdateListener != null)
            mAdUpdateListener.hideIMBannerAd();
    }

    class RadioReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("RADIO", "onReceive ProgressBarReciever:");

            if (intent != null) {
                if (intent.getAction() == LiveRadioConstants.HIDE_PROGRESS_BAR) {
                    hideProgressBar();
                    Log.d("Play", PreferencesManager.getInstance(getActivity()).getLiveRadioUrl());

                } else if (intent.getAction() == LiveRadioConstants.ENABLE_PAUSE_PLAY) {
                    mPlayPauseIV.setSelected(true);
//                        mPlayPauseIV.setImageResource(R.drawable.icn_pause_radio);

                    showPausePlayBtn();

                } else if (intent.getAction() == LiveRadioConstants.UPDATE_UI)
                    updatePausePlayBtn();
                else if (intent.getAction() == LiveRadioConstants.IS_VISIBLE) {
                    if (mIsVisible && mIsDataAvailable) {
                        playRadio();
                        hidePausePlaybtn();
                        showProgressBar();
                    }

                } else if (intent.getAction() == LiveRadioConstants.RADIO_BUFFERING_START) {
                    hidePausePlaybtn();
                    showProgressBar();
                    Log.d(TAG, "Radio Buffering started");
                } else if (intent.getAction() == LiveRadioConstants.RADIO_BUFFERING_END) {
                    showPausePlayBtn();
                    hideProgressBar();
                    Log.d(TAG, "Radio Buffering Ended");
                }
            }

        }
    }

}
