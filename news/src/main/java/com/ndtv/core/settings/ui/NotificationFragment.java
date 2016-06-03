package com.ndtv.core.settings.ui; /**
 * Project      : Awaaz
 * Filename     : NotificationFragment.java
 * Author       : nagaraj
 * Comments     :
 * Copyright    : © Copyright NDTV Convergence Limited 2011
 * Developed under contract by Robosoft Technologies
 * History      : NA
 */

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.views.RobotoBoldTextView;
import com.ndtv.core.common.util.views.StyledTextView;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.constants.ApplicationConstants.PreferenceKeys;
import com.ndtv.core.constants.ApplicationConstants.Tags;
import com.ndtv.core.gcm.GcmUtility;
import com.ndtv.core.settings.adapter.NotificationAdapter;
import com.ndtv.core.ui.widgets.BaseFragment;

import java.util.Arrays;
import java.util.List;

import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * @author nagaraj
 */
public class NotificationFragment extends BaseFragment implements OnCheckedChangeListener, Tags, PreferenceKeys {

    private static final String TAG = "SettingsFragment " + makeLogTag(NotificationFragment.class);

    private ListView mListview;
    private boolean isNotificationAtNight, isNotificationSound, isNotificationVibration, isNotificationInverse, isAppRefreshEnable;
    private Switch mSoundBtn, mVibrateBtn, mNightNotificationBtn, mEnableNotification, mInvertNotification, mAppRefreshBtn;
    private GCMListener mGCMListener;
    private ProgressBar mProgressBar;
    private StyledTextView mNewsCategory;
    private RelativeLayout mNightNotification, mVibrateNotification, mNotificationSound;
    private RelativeLayout invertNotificationsLayout,mRefreshAppSetting;
    private int mNavigationPosition;
    private String navigation, section;
    private BannerAdFragment.AdListener mAdUpdateListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            section = bundle.getString(SettingsFragment.SettingsConstants.SECTION_TITLE);
            mNavigationPosition = bundle.getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS);
        }
        navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.notification_settings_layout, container, false);
        initViews(view, inflater);
        return view;
    }

    /**
     * @param view
     * @param inflater
     */
    private void initViews(ViewGroup view, LayoutInflater inflater) {
        mListview = (ListView) view.findViewById(R.id.push_section_listview);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        View settingsHeader = inflater.inflate(R.layout.notification_settings_header, null, false);

        /** Enable notification */
        LinearLayout enableNotificationsLayout = (LinearLayout) settingsHeader.findViewById(R.id.enable_notifications);
        RobotoBoldTextView enableNotification = (RobotoBoldTextView) enableNotificationsLayout.findViewById(R.id.section_title);
//		enableNotification.setCustomFont(getActivity(), getString(R.string.roboto_bold));
        enableNotification.setText(getString(R.string.notification));
        mEnableNotification = (Switch) enableNotificationsLayout.findViewById(R.id.checkbox);
        mEnableNotification.setTag(ENABLE_NOTIFICATION);
        mEnableNotification.setChecked(PreferencesManager.getInstance(getActivity().getApplicationContext())
                .getPushStatus());
        mEnableNotification.setOnCheckedChangeListener(this);


        /**Invert Notification*/
        invertNotificationsLayout = (RelativeLayout) settingsHeader.findViewById(R.id.invert_notifications);
        StyledTextView invertNotifications = (StyledTextView) invertNotificationsLayout.findViewById(R.id.section_title);
//		enableNotification.setCustomFont(getActivity(), getString(R.string.roboto_bold));
        invertNotifications.setText(getString(R.string.invert_notification));
        mInvertNotification = (Switch) invertNotificationsLayout.findViewById(R.id.checkbox);
        mInvertNotification.setTag(INVERT_NOTIFICATION);
        mInvertNotification.setOnCheckedChangeListener(this);

        /** Notifications at night */
        mNightNotification = (RelativeLayout) settingsHeader.findViewById(R.id.notification_at_night);
        StyledTextView textview = (StyledTextView) mNightNotification.findViewById(R.id.section_title);
        textview.setText(getString(R.string.notifications_at_night));
        StyledTextView subTitle = (StyledTextView) mNightNotification.findViewById(R.id.sub_title);
        subTitle.setVisibility(View.VISIBLE);

        mNightNotificationBtn = (Switch) mNightNotification.findViewById(R.id.checkbox);
        mNightNotificationBtn.setTag(NIGHT_TAG);
        mNightNotificationBtn.setOnCheckedChangeListener(this);
        /** Notification sound */
        mNotificationSound = (RelativeLayout) settingsHeader.findViewById(R.id.notification_sound);
        StyledTextView sound = (StyledTextView) mNotificationSound.findViewById(R.id.section_title);
        sound.setCustomFont(getActivity(), getString(R.string.roboto_bold));
        sound.setText(getString(R.string.notification_sound));
        mSoundBtn = (Switch) mNotificationSound.findViewById(R.id.checkbox);
        mSoundBtn.setTag(SOUND_TAG);

        mSoundBtn.setOnCheckedChangeListener(this);

        /** Vibrate on notification */
        mVibrateNotification = (RelativeLayout) settingsHeader.findViewById(R.id.notification_vibration);
        StyledTextView vibration = (StyledTextView) mVibrateNotification.findViewById(R.id.section_title);
        vibration.setCustomFont(getActivity(), getString(R.string.roboto_bold));
        vibration.setText(getString(R.string.vibrate_notification));
        mVibrateBtn = (Switch) mVibrateNotification.findViewById(R.id.checkbox);
        mVibrateBtn.setTag(VIBRATION_TAG);
        mVibrateBtn.setOnCheckedChangeListener(this);

        /** App refresh in background */
        mRefreshAppSetting = (RelativeLayout) settingsHeader.findViewById(R.id.refresh_app);
        StyledTextView app_refresh = (StyledTextView) mRefreshAppSetting.findViewById(R.id.section_title);
        app_refresh.setText(getString(R.string.app_refresh));
        StyledTextView refresh_time = (StyledTextView) mRefreshAppSetting.findViewById(R.id.sub_title);
        refresh_time.setVisibility(View.VISIBLE);

        mAppRefreshBtn = (Switch) mRefreshAppSetting.findViewById(R.id.checkbox);
        mAppRefreshBtn.setTag(REFRESH_TAG);
        mAppRefreshBtn.setOnCheckedChangeListener(this);

        mNewsCategory = (StyledTextView) settingsHeader.findViewById(R.id.news_categories);

        mListview.addHeaderView(settingsHeader, null, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mGCMListener = (GCMListener) activity;
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferencesManager preManager = PreferencesManager.getInstance(getActivity().getApplicationContext());
        if (null != mNightNotificationBtn && preManager.getNotificationSettings(NOTIFICATION_AT_NIGHT)) {
            mNightNotificationBtn.setChecked(true);
        }
        if (null != mInvertNotification && preManager.getInverseNotificationSettings(NOTIFICATION_INVERSE)) {
            mInvertNotification.setChecked(true);
        }
        if (null != mSoundBtn && preManager.getNotificationSettings(NOTIFICATION_SOUND)) {
            mSoundBtn.setChecked(true);
        }
        if (null != mVibrateBtn && preManager.getNotificationSettings(NOTIFICATION_VIBRATION)) {
            mVibrateBtn.setChecked(true);
        }
        if (null != mAppRefreshBtn && preManager.getNotificationSettings(APP_REFRESH_ENABLE)) {
            mAppRefreshBtn.setChecked(true);
        }
        NotificationAdapter adapter = getAdapter();
        if (null != adapter) {
            adapter.setSelectedSections(getActivity(), getResources().getStringArray(R.array.custom_push_section_list));
        }
        if (mEnableNotification.isChecked())
            showOtherSections();
        else
            hideOtherSections();

    }

    @Override
    public void setScreenName() {
        setScreenName(navigation + " - " + section);
    }

    private void updateListview() {
        final NotificationAdapter adapter = getAdapter();
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        NotificationAdapter adapter = new NotificationAdapter(getActivity(), getResources().getStringArray(
                R.array.custom_push_section_list));
        mListview.setAdapter(adapter);
        if (null != mProgressBar)
            mProgressBar.setVisibility(View.GONE);

    }

    @Override
    public void refresh() {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch ((Integer) buttonView.getTag()) {
            case NIGHT_TAG:
                isNotificationAtNight = isChecked;
                break;
            case SOUND_TAG:
                isNotificationSound = isChecked;
                break;
            case VIBRATION_TAG:
                isNotificationVibration = isChecked;
                break;
            case ENABLE_NOTIFICATION:
                handleActiviateNotification(isChecked);
                break;
            case INVERT_NOTIFICATION:
                isNotificationInverse = isChecked;
                break;
            case REFRESH_TAG:
                isAppRefreshEnable = isChecked;
                break;


        }

    }

    /**
     * @param isEnabled
     */
    private void handleActiviateNotification(boolean isEnabled) {
        if (Utility.isInternetOn(getActivity().getApplicationContext())) {
            if (null != getActivity()) {
                PreferencesManager preManager = PreferencesManager.getInstance(getActivity().getApplicationContext());
                String regId = GcmUtility.getRegistrationId(getActivity().getApplicationContext());
                Log.i("GCM", "in settings the regid is (regId = " + regId + ")");
                if (!Utility.isInternetOn(getActivity().getApplicationContext())) {
                    mEnableNotification.setChecked(!isEnabled);
                    Toast.makeText(getActivity().getApplicationContext(), "Please Connect to network",
                            Toast.LENGTH_SHORT).show();
                } else if (isEnabled) {
                    showOtherSections();
                    if (GcmUtility.checkPlayServices(getActivity().getApplicationContext(), getActivity())) {
                        resetAllSeactions(true);
                        GcmUtility.registerInBackground(getActivity().getApplicationContext());
                        mEnableNotification.setChecked(isEnabled);
                    }
                    // preManager.setPushStatus(true);

                } else {
                    resetAllSeactions(false);
                    GcmUtility.unregisteInBackGround(getActivity().getApplicationContext(),
                            GcmUtility.getRegistrationId(getActivity().getApplicationContext()));
                    mEnableNotification.setChecked(false);
                    hideOtherSections();
                }
            }
        }
    }

    private void hideOtherSections() {
        invertNotificationsLayout.setVisibility(View.GONE);
        mNightNotification.setVisibility(View.GONE);
        mNotificationSound.setVisibility(View.GONE);
        mVibrateNotification.setVisibility(View.GONE);
        mNewsCategory.setVisibility(View.GONE);
        NotificationAdapter adapter = getAdapter();
        if (adapter != null)
            adapter.disableNotification(false);


    }

    private void showOtherSections() {
//        if (Utility.isLollypopAndAbove()) {
        invertNotificationsLayout.setVisibility(View.VISIBLE);
//        } else {
//            invertNotificationsLayout.setVisibility(View.GONE);
//        }
        mNightNotification.setVisibility(View.VISIBLE);
        mNotificationSound.setVisibility(View.VISIBLE);
        mVibrateNotification.setVisibility(View.VISIBLE);
        mNightNotification.setVisibility(View.VISIBLE);
        mNewsCategory.setVisibility(View.VISIBLE);
        NotificationAdapter adapter = getAdapter();
        if (adapter != null)
            adapter.enableNotification(true);

    }

    /**
     *
     */
    private void resetAllSeactions(boolean isEnabled) {
        // public void saveSelectedSections(Context context) {
        // List<String> sectionList = Arrays.asList(context.getResources()
        // .getStringArray(R.array.custom_push_section_list));
        // if (null != sectionList) {
        // PreferencesManager preManager =
        // PreferencesManager.getInstance(context.getApplicationContext());
        // for (int i = 0; i < sectionList.size(); i++)
        // preManager.setNotificationSettings(sectionList.get(i),
        // mcachedArray.get(i));
        // }
        // }
        List<String> sectionList = Arrays.asList(getActivity().getResources().getStringArray(
                R.array.custom_push_section_list));
        if (null != sectionList) {
            PreferencesManager pfs = PreferencesManager.getInstance(getActivity().getApplicationContext());
            for (int i = 0; i < sectionList.size(); i++)
                pfs.setNotificationSettings(sectionList.get(i), isEnabled);
        }
        NotificationAdapter adapter = getAdapter();
        if (null != adapter)
            adapter.resetSelections(getActivity(), getResources().getStringArray(R.array.custom_push_section_list), isEnabled);
        updateListview();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveSettings();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && mAdUpdateListener != null){
             mAdUpdateListener.hideIMBannerAd();
        }
        if (null != getActivity()) {
            if (!isVisibleToUser)
                saveSettings();

        }

    }

    private NotificationAdapter getAdapter() {
        if (null != mListview) {
            ListAdapter adapter = mListview.getAdapter();
            if (adapter instanceof HeaderViewListAdapter) {
                return (NotificationAdapter) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
            } else {
                return (NotificationAdapter) mListview.getAdapter();
            }
        }
        return null;
    }

    private void saveSettings() {
        if (null != getActivity()) {
            PreferencesManager prefMngr = PreferencesManager.getInstance(getActivity().getApplicationContext());
            prefMngr.setNotificationSettings(NOTIFICATION_SOUND, isNotificationSound);
            prefMngr.setNotificationSettings(NOTIFICATION_VIBRATION, isNotificationVibration);
            prefMngr.setNotificationSettings(NOTIFICATION_AT_NIGHT, isNotificationAtNight);
            prefMngr.setNotificationSettings(NOTIFICATION_INVERSE, isNotificationInverse);
            prefMngr.setNotificationSettings(APP_REFRESH_ENABLE, isAppRefreshEnable);
            if (null != getAdapter())
                getAdapter().saveSelectedSections(getActivity());
            if (null != mGCMListener)
                mGCMListener.registerGCM();
            //
        }
    }
}
