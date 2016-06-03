package com.ndtv.core.radio.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.ndtv.core.R;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.util.UiUtility;
import com.ndtv.core.common.util.views.AvatarDrawable;
import com.ndtv.core.radio.LiveRadioManager;
import com.ndtv.core.radio.ui.LiveRadioFragment;
import com.ndtv.core.ui.HomeActivity;
import com.ndtv.core.util.ApplicationUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sangeetha on 30/1/15.
 */
public class LiveRadioService extends Service implements LiveRadioFragment.LiveRadioConstants, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {
    private MediaPlayer mMediaPlayer;
    private AudioReciever mAudioReciever;
    private NotiicationReciever mNotificationReciever;
    private NotificationManager mNotificationMngr;
    private AudioManager mAudioManager;
    private final int RADIO_NOTIFICATION_ID = 1;
    private String mTitle; //section title
    private int mSectionPosition; //section position
    private String mUrl;
    private RemoteViews mNotificationView;
    private NotificationCompat.Builder mBuilder;
    private static String TAG = "service";
    private boolean mPrepared = true;
    //this is to avoid playing of radio, on gaining audio focus, if radio was paused from notification
    private boolean mIsPaused;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("RADIO", "onCreate");
        mNotificationMngr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioReciever = new AudioReciever();
        mNotificationReciever = new NotiicationReciever();
        registerReceiver(mAudioReciever, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PAUSE_PLAY);
        filter.addAction(ACTION_STOP);

        registerReceiver(mNotificationReciever, filter);
        createRadio();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("RADIO", "onStartCommand:" + intent.getStringExtra(RADIO_URL));

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_START:
                    startRadio(intent);
                    break;
                case ACTION_PAUSE:
//                    updateNotification();
                    pauseRadio();
                    cancelNotification();

                    break;
                case ACTION_RESUME:
//                    updateNotification();
                    resumeRadio();
                    showNotification();

                    break;
                case ACTION_STOP:
                    clear();
                    stopSelf();
                    break;


            }


        } else
            stopSelf();
        return START_NOT_STICKY;

    }

    private void createRadio() {
        registerCallListener();
        mAudioManager = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnInfoListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startRadio(Intent intent) {
        Log.d(TAG, "Live radio Started");

        mUrl = intent.getStringExtra(RADIO_URL);
        mTitle = intent.getStringExtra(RADIO_TITLE);
        mSectionPosition = intent.getIntExtra(LIVE_RADIO_SECTION_POSITION, 0);

        if (mUrl != null && mUrl.length() > 0 && PreferencesManager.getInstance(this) != null && !PreferencesManager.getInstance(this).getLiveRadioUrl().equalsIgnoreCase(mUrl)) {
            playRadio();
        } else if (mUrl.equalsIgnoreCase(PreferencesManager.getInstance(this).getLiveRadioUrl())) {
            //check if from notification
            if (LiveRadioManager.getLiveRadioManagerInstance() != null && LiveRadioManager.getLiveRadioManagerInstance().isIsFromNotifictn()) {
                //check whether its playing
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    //dont do anything,hide progress bar if visible.show pause icon.
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(HIDE_PROGRESS_BAR));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ENABLE_PAUSE_PLAY));

                } else {
                    //if not playing
                    //resume radio
                    updateNotification();
                    resumeRadio();
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(HIDE_PROGRESS_BAR));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ENABLE_PAUSE_PLAY));
                }
            } else {
                //If not From Notification
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying())
                    playRadio();
                else {
                    //If progress bar is visible.
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(HIDE_PROGRESS_BAR));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ENABLE_PAUSE_PLAY));

                }
            }
        }
    }

    private void playRadio() {

        try {
            cancelNotification();
            PreferencesManager.getInstance(this).setLiveRadioPlayStatus(false);
            PreferencesManager.getInstance(this).setSectionPositionLiveRadio(0);
//            PreferencesManager.getInstance(this).setLiveRadioUrl(mUrl);
            Log.d(TAG, "Thread Started");


            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            Log.d("RADIO", "playRadio START");

            releaseAudioFocus();
            if (mPrepared)
                resetMediaPlayer();
//            mMediaPlayer.reset();
//            mMediaPlayer.setDataSource(mUrl);
//            mMediaPlayer.prepareAsync();


        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.d("RADIO", "playRadio END");

    }

    private void resetMediaPlayer() {
        try {
            mPrepared = false;
            PreferencesManager.getInstance(this).setLiveRadioUrl(mUrl);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mUrl);
            mMediaPlayer.prepareAsync();
            Log.d("RADIO", "OnPrepare Called");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void clear() {
        if (PreferencesManager.getInstance(this) != null) {
            Log.d(TAG, "Live radio Stopped");
            PreferencesManager.getInstance(this).setLiveRadioUrl("");
            PreferencesManager.getInstance(this).setLiveRadioPlayStatus(false);
            PreferencesManager.getInstance(this).setSectionPositionLiveRadio(0);

        }

    }

    private void releaseMediaPlayer() {

        mNotificationMngr.cancel(RADIO_NOTIFICATION_ID);
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d("RADIO", "OnPrepared:");

        if (PreferencesManager.getInstance(this) != null) {
            Log.d(TAG, "media Player about to start");
            mPrepared = true;
            if (!PreferencesManager.getInstance(this).getLiveRadioUrl().equalsIgnoreCase(mUrl)) {
                resetMediaPlayer();
            } else {
                PreferencesManager.getInstance(this).setLiveRadioPlayStatus(true);
                PreferencesManager.getInstance(this).setSectionPositionLiveRadio(mSectionPosition);
                PreferencesManager.getInstance(this).setErrorRadio(false);


                /** Request audio focus for playback */
                int result = mAudioManager.requestAudioFocus(audioFocusChangeListener,

                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    // am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                    mediaPlayer.start();
                    Log.d("URL", mUrl);
                    Log.d("URLPlay", PreferencesManager.getInstance(this).getLiveRadioUrl());


                }
                if (PreferencesManager.getInstance(this) != null)
                    PreferencesManager.getInstance(this).setLiveRadioStopped(false);
                Intent intent = new Intent(HIDE_PROGRESS_BAR);

                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                Intent pausePlayIntent = new Intent(ENABLE_PAUSE_PLAY);

                LocalBroadcastManager.getInstance(this).sendBroadcast(pausePlayIntent);
                showNotification();

            }

        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        releaseAudioFocus();
        //This is to hide progress bar, if some error occured
        PreferencesManager.getInstance(this).setErrorRadio(true);
        Intent intent = new Intent(HIDE_PROGRESS_BAR);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        stopSelf();
        cancelNotification();
//        Toast.makeText(getApplicationContext(), R.string.no_network_msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
//        releaseAudioFocus();
//        PreferencesManager.getInstance(this).setErrorRadio(true);
//        stopSelf();
//        cancelNotification();
        //Added To prevent the issue caused in Saumsung devices
        if (mMediaPlayer != null)
            mMediaPlayer.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clear();

        unregisterCallListener();
        if (PreferencesManager.getInstance(this) != null)
            PreferencesManager.getInstance(this).setLiveRadioStopped(true);
        if (mAudioReciever != null) {
            //Adding try catch block if mAudioReceiver is not registered.
            try {
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }

                unregisterReceiver(mAudioReciever);
                unregisterReceiver(mNotificationReciever);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cancelNotification();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        clear();
        releaseMediaPlayer();
        cancelNotification();
//        super.onTaskRemoved(rootIntent);

    }

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                Log.d(TAG, "Transient loss called");
                if (mMediaPlayer != null)
                    handleBtnIntent();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
                Log.d(TAG, "Focus gain called");
                if (mIsPaused) {
                    if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                        updateNotification();
                        resumeRadio();
                        mIsPaused = false;
                    }
                }

            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                Log.d(TAG, "Focus loss called");
                stopSelf();
                releaseAudioFocus();

            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                Log.d(TAG, "Transient duck called");
                // Pause playback if user is in NewsDetailpage
                if (UiUtility.getsCurrentFragment() != null) {
                    if(UiUtility.getsCurrentFragment().toString().toLowerCase().contains("newsdetailfragment"))
                    {
                        stopSelf();
                        releaseAudioFocus();
                        Log.d(TAG, "Radio Stopped");
                        return;
                    }

                // Pause playback
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    updateNotification();
                    pauseRadio();
                    mIsPaused = true;
                    Log.d(TAG, "Notification Arrived");
                    Log.d(TAG, focusChange + "");
                }
            }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
                Log.d(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                Log.d(TAG, focusChange + "");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) {
                Log.d(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE");
                Log.d(TAG, focusChange + "");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
                Log.d(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                Log.d(TAG, focusChange + "");
            }
        }
    };

    private void cancelNotification() {
        if (mNotificationMngr != null)
            mNotificationMngr.cancel(RADIO_NOTIFICATION_ID);

    }

    private void showNotification() {

        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(LIVE_RADIO_PUSH, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setCategory(Notification.CATEGORY_SERVICE);
//        mBuilder.setContentTitle(mTitle);
//        mBuilder.setContentText("Live Radio");
        mBuilder.setSmallIcon(R.drawable.ic_ndtv_notfctn);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setOngoing(true);
        mBuilder.setAutoCancel(false);

        if (ApplicationUtils.isInverseSet(getApplicationContext())) {
            mNotificationView = new RemoteViews(getPackageName(), R.layout.notification_layout_inv);
        } else {
            mNotificationView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        }
        mNotificationView.setTextViewText(R.id.content_title, mTitle);
        mNotificationView.setTextViewText(R.id.content_text, "Live Radio");
        if (Utility.isLollypopAndAbove()) {
            Bitmap roundedIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher);
            mNotificationView.setImageViewBitmap(R.id.ndtv_icon, Utility.drawableToBitmap(new AvatarDrawable(roundedIcon)));
        } else {
            mNotificationView.setImageViewResource(R.id.ndtv_icon, R.drawable.ic_launcher);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
        String time = dateFormat.format(new Date(System.currentTimeMillis()));
        mNotificationView.setTextViewText(R.id.current_time, time);


        if (Utility.isLollypopAndAbove()) {
            if (ApplicationUtils.isInverseSet(getApplicationContext())) {
                mNotificationView.setImageViewResource(R.id.close_btn, R.drawable.notification_close_btn);
                mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.pause_radio_2);
            } else {
                mNotificationView.setImageViewResource(R.id.close_btn, R.drawable.notification_close_btn_v11);
                mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.pause_radio);
            }

        } else {
            if (ApplicationUtils.isInverseSet(getApplicationContext())) {
                mNotificationView.setImageViewResource(R.id.close_btn, R.drawable.notification_close_btn_v11);
                mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.pause_radio);
            } else {
                mNotificationView.setImageViewResource(R.id.close_btn, R.drawable.notification_close_btn);
                mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.pause_radio_2);
            }
        }


        mNotificationView.setViewVisibility(R.id.loader, View.GONE);
        setListeners(mNotificationView);
        mBuilder.setContent(mNotificationView);

        if (null != mNotificationMngr)
            mNotificationMngr.notify(RADIO_NOTIFICATION_ID, mBuilder.build());
    }


    private void setListeners(RemoteViews notificationView) {
        Intent pauseBtn = new Intent(LAUNCH_SERVICE);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 2, pauseBtn, 0);
        notificationView.setOnClickPendingIntent(R.id.play_pause, pendingSwitchIntent);
        Intent closeBtn = new Intent(ACTION_STOP_SERVICE);
        PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, 0, closeBtn, 0);
        notificationView.setOnClickPendingIntent(R.id.close_btn, pendingStopIntent);

    }

    private void updateNotification() {


        if (mNotificationView != null) {
            if (PreferencesManager.getInstance(this).isLiveRadioPlaying()) {
                if (Utility.isLollypopAndAbove()) {
                    if (ApplicationUtils.isInverseSet(getApplicationContext())) {
                        mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.play_radio_2);
                    } else
                        mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.play_radio);
                } else {
                    if (ApplicationUtils.isInverseSet(getApplicationContext())) {
                        mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.play_radio);
                    } else
                        mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.play_radio_2);
                }
            } else {
                if (Utility.isLollypopAndAbove()) {
                    if (ApplicationUtils.isInverseSet(getApplicationContext())) {
                        mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.pause_radio_2);
                    } else
                        mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.pause_radio);
                } else {
                    if (ApplicationUtils.isInverseSet(getApplicationContext())) {
                        mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.pause_radio);
                    } else
                        mNotificationView.setImageViewResource(R.id.play_pause, R.drawable.pause_radio_2);
                }

            }
            mNotificationMngr.notify(RADIO_NOTIFICATION_ID, mBuilder.build());
        }
    }


    private void showLoaderInNotification() {
        if (mNotificationView != null) {
            mNotificationView.setViewVisibility(R.id.loader, View.VISIBLE);
            mNotificationView.setViewVisibility(R.id.play_pause, View.GONE);
            mNotificationMngr.notify(RADIO_NOTIFICATION_ID, mBuilder.build());

        }
    }

    private void hideLoaderInNotification() {
        if (mNotificationView != null) {
            mNotificationView.setViewVisibility(R.id.loader, View.GONE);
            mNotificationView.setViewVisibility(R.id.play_pause, View.VISIBLE);
            mNotificationMngr.notify(RADIO_NOTIFICATION_ID, mBuilder.build());
        }
    }


    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if ((state == TelephonyManager.CALL_STATE_RINGING) || (state == TelephonyManager.CALL_STATE_OFFHOOK)) {
                stopSelf();
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

    private void registerCallListener() {
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void unregisterCallListener() {
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private void releaseAudioFocus() {
        if (null != mAudioManager)
            mAudioManager.abandonAudioFocus(audioFocusChangeListener);

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
//        Toast.makeText(this,"Buffered:"+ percent,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == mp.MEDIA_INFO_BUFFERING_START) {
            Log.d(TAG, "Buffering Started");
            Intent intent = new Intent(RADIO_BUFFERING_START);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            showLoaderInNotification();
        } else if (what == mp.MEDIA_INFO_BUFFERING_END) {
            Log.d(TAG, "Buffering Ended");
            Intent intent = new Intent(RADIO_BUFFERING_END);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            hideLoaderInNotification();
        }
        return false;
    }


    class AudioReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();

        }

    }

    private void pauseRadio() {
        if (PreferencesManager.getInstance(this) != null) {
            PreferencesManager.getInstance(this).setLiveRadioPlayStatus(false);
            try {
                if (mMediaPlayer != null)
                    mMediaPlayer.pause();

                Log.d(TAG, "Media Player stopped");
            } catch (Exception e) {

            }

        }
    }

    private void resumeRadio() {
        if (PreferencesManager.getInstance(this) != null) {
            PreferencesManager.getInstance(this).setLiveRadioPlayStatus(true);
            try {
                if (mMediaPlayer != null)
                    mMediaPlayer.start();
                Log.d(TAG, "Media Player started");
            } catch (Exception e) {

            }

        }
    }

    private void stopService() {

        clear();
        cancelNotification();
        releaseMediaPlayer();
        if (PreferencesManager.getInstance(this) != null)
            PreferencesManager.getInstance(this).setLiveRadioStopped(true);
        Intent intent = new Intent(IS_VISIBLE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        stopSelf();


    }

    private void handleBtnIntent() {
        updateNotification();
        if (PreferencesManager.getInstance(this).isLiveRadioPlaying())
            pauseRadio();
        else
            resumeRadio();


        Intent intent = new Intent(UPDATE_UI);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    class NotiicationReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getAction() == ACTION_PAUSE_PLAY)
                    handleBtnIntent();
                else if (intent.getAction() == ACTION_STOP) {
                    stopService();

                }

            }


        }
    }


}
