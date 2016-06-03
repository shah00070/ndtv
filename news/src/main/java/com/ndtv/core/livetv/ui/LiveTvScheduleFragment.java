package com.ndtv.core.livetv.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.XMLRequest;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.livetv.adapter.LiveTvScheduleAdapter;
import com.ndtv.core.livetv.dto.LiveTvSchedule;
import com.ndtv.core.livetv.dto.Program;
import com.ndtv.core.ui.ChromeCastFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by laveen on 4/2/15.
 */
public class LiveTvScheduleFragment extends ChromeCastFragment implements Response.Listener<LiveTvSchedule>, Response.ErrorListener {

    private static final String TAG = "LiveTvListing ";

    public static final String LIVETV_SCHEDULE_URL = "schedule_url";
    public static final String LIVETV_PRIME = "NDTV Prime";
    public static final String LIVETV_PROFIT = "NDTV Profit";

    protected String mScheduleUrl;
    protected String mPlayUrl;
    protected String mLiveTvName;

    protected RecyclerView mScheduleList;
    protected LinearLayoutManager mLayoutManager;
    protected ProgressBar mLoadingIndicator;

    public LiveTvSchedule mLiveTvSchedule;

    protected XMLRequest<LiveTvSchedule> request;

    protected int mNavigationPosition;
    protected int mSectionPosition;
    private String navigation;
    private BannerAdFragment.AdListener mAdUpdateListener;

    public static Fragment getInstance(String liveTvName, String scheduleUrl, String liveTVPlayUrl, int navigationPosition, int sectionPosition) {

        Fragment fragment;

        if (liveTvName.equalsIgnoreCase(LIVETV_PRIME)) {
            fragment = new LiveTvPrimeScheduleFragment();
        } else if (liveTvName.equalsIgnoreCase(LIVETV_PROFIT)) {
            //fragment = new LiveTvProfitScheduleFragment();

            fragment = new LiveTvPrimeScheduleFragment();
        } else {
            fragment = new LiveTvScheduleFragment();
        }

        Bundle bundle = new Bundle();
        bundle.putString(LiveTvPlayFragment.LIVETV_NAME, liveTvName);
        bundle.putString(LIVETV_SCHEDULE_URL, scheduleUrl);
        bundle.putString(LiveTvPlayFragment.LIVETV_URL, liveTVPlayUrl);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, navigationPosition);
        bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POS, sectionPosition);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBundleData();
        navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPosition);

    }

    @Override
    public void setScreenName() {
        setScreenName(navigation + " - " + mLiveTvName);
    }

    private void getBundleData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mScheduleUrl = bundle.getString(LIVETV_SCHEDULE_URL);
            mPlayUrl = bundle.getString(LiveTvPlayFragment.LIVETV_URL);
            mLiveTvName = bundle.getString(LiveTvPlayFragment.LIVETV_NAME);
            mNavigationPosition = bundle.getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS);
            mSectionPosition = bundle.getInt(ApplicationConstants.BundleKeys.SECTION_POS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_livetv_schedule, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mScheduleList = (RecyclerView) view.findViewById(R.id.recycler_view);
        mLoadingIndicator = (ProgressBar) view.findViewById(R.id.loading_indicator);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mScheduleList.setLayoutManager(mLayoutManager);
        mScheduleList.setHasFixedSize(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        downloadLiveTvSchedule(mScheduleUrl);
    }

    protected void setLiveTvListAdapter(List<Program> programs) {
        mScheduleList.setAdapter(new LiveTvScheduleAdapter(programs, mLiveTvName, mPlayUrl, mNavigationPosition, mSectionPosition));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser)
            loadBannerAds();
    }

    private void downloadLiveTvSchedule(String scheduleUrl) {
        if (!TextUtils.isEmpty(scheduleUrl)) {
            request = new XMLRequest<>(Request.Method.GET, scheduleUrl, LiveTvSchedule.class, this, this);
            request.setTag(mScheduleUrl);
            VolleyRequestQueue.getInstance(getActivity().getApplicationContext()).addToRequestQueue(request);
        }
    }

    @Override
    public void onResponse(LiveTvSchedule liveTvSchedule) {
        mLiveTvSchedule = liveTvSchedule;
        populateUI(liveTvSchedule);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (request != null)
            request.cancel();
    }

    private void populateUI(LiveTvSchedule schedule) {
        if (schedule != null && schedule.schedule != null && schedule.schedule.programeList != null) {
            setLiveTvListAdapter(schedule.schedule.programeList);
            int playingIndex = getCurrentPlayingShowIndex(schedule.schedule.programeList);
            if (playingIndex != -1) {
                mScheduleList.scrollToPosition(playingIndex);
            }
        }
        hideLoadingBar();
    }

    protected int getCurrentPlayingShowIndex(List<Program> programs) {
        Calendar deviceCalendar = Calendar.getInstance();
        Date deviceDate = deviceCalendar.getTime();

        for (int index = 0; index < programs.size(); index++) {
            Program currentProgram = programs.get(index);
            Date showDate = currentProgram.getProgramTime();
            if (showDate != null) {
                if (deviceDate.getTime() >= showDate.getTime()) {
                    if (programs.size() > index + 1) {
                        Date nextShowDate = Utility.getProgramTime(programs.get(index + 1));
                        if (nextShowDate != null)
                            if (deviceDate.getTime() < nextShowDate.getTime()) {
                                return index;
                            }
                    } else {
                        return index;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        hideLoadingBar();
    }

    private void hideLoadingBar() {
        mLoadingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
            //setting ad background to black color since adview container is part of activity
//            if (activity.findViewById(R.id.adContainer) != null) {
//                activity.findViewById(R.id.adContainer).setBackgroundColor(getResources().getColor(R.color.live_tv_schedule_background));
//            }
        } catch (Exception ex) {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        /*if (getActivity() != null && getActivity().findViewById(R.id.adContainer) != null) {
            getActivity().findViewById(R.id.adContainer).setBackgroundColor(getResources().getColor(R.color.white));
        }*/
    }

    public void loadBannerAds() {
        if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(mNavigationPosition, mSectionPosition, null, false, -1, true, false);
        }
    }
}
