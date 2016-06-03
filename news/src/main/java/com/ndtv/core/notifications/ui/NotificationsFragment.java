/**
 Project      : Awaaz
 Filename     : VideosFragment.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.notifications.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.ListFragment;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants.BundleKeys;
import com.ndtv.core.constants.ApplicationConstants.NavigationType;
import com.ndtv.core.notifications.NotificationsManager;
import com.ndtv.core.notifications.dto.NotificationItem;
import com.ndtv.core.notifications.dto.Notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ndtv.core.util.LogUtils.makeLogTag;


/**
 * @author nagaraj
 */
public class NotificationsFragment extends ListFragment implements OnItemClickListener, BundleKeys,
        NavigationType {

    private static final String TAG = makeLogTag(NotificationsFragment.class);

    private int mSectionPosition, mNavigationPos;
    private NotificationsManager.NotificationsDownloadListener mDownloadListener;
    private int mSubTabPosition;
    private String mNavigationTitle;
    private AtomicInteger mCurrentPageSize = new AtomicInteger(1);
    private AtomicBoolean mIsPaginationRunning = new AtomicBoolean(false);
    private List<NotificationItem> mNotificationsList = new ArrayList<NotificationItem>();
    public BannerAdFragment.AdListener mAdUpdateListener;
    private String navigation, mSectionTitle;

    public static Fragment newInstance(int pos, String section, int navigationPos) {
        NotificationsFragment newsListingFragment = new NotificationsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SECTION_TITLE, section);
        bundle.putInt(SECTION_POSITION, pos);
        bundle.putInt(NAVIGATION_POS, navigationPos);
        newsListingFragment.setArguments(bundle);
        return newsListingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().invalidateOptionsMenu();
        extractArguements();
    }

    /**
     * do not forget to call super.onActivityCreated(savedInstanceState); which
     * init's the ActionBarIconListener.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null) {

            if (mNotificationsList != null) {
                setNotificationsAdapter();
                swipeToRefresh(true);
                if (mNotificationsList.size() == 0) {
                    initDownloadListeners();
                    showProgressBar();
                    downloadData();
                } else {
                    mListView.setSelection(mSelectedItemPos);
                    hideProgressBar();
                }
            }
            if (Utility.isInternetOn(getActivity().getApplicationContext())) {
                paginationListener = castToOnPaginationListener();
            }
        }

    }

    private void downloadData() {
        if (null != getActivity()) {
            if (Utility.isInternetOn(getActivity())) {

                downloadNotifications();

            } else
                hideProgressBar();
        }
    }

    private void setNotificationsAdapter() {
        if (null != mListView) {
            addFooterView();
            NotificationsAdapter adapter = new NotificationsAdapter(getActivity(), mNotificationsList);
            mListView.setAdapter(adapter);
            removeFooterView();
            mListView.setOnItemClickListener(this);
        }

    }

    private void extractArguements() {
        Bundle bundle = getArguments();
        if (null != bundle) {
            mSectionTitle = bundle.getString(SECTION_TITLE);
            mSectionPosition = bundle.getInt(SECTION_POSITION);
            mNavigationPos = bundle.getInt(NAVIGATION_POS);
        }
        navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPos);
    }


    private void downloadNotifications() {
        if (getActivity() != null && mDownloadListener != null)
            NotificationsManager.getInstance().downloadNotifications(mSectionPosition, getActivity(), mDownloadListener,
                    mNavigationPos, mSubTabPosition, mCurrentPageSize.get());
    }

    private void initDownloadListeners() {
        final long requestBirthTime = SystemClock.elapsedRealtime();
        mDownloadListener = new NotificationsManager.NotificationsDownloadListener() {

            @Override
            public void onNotificationsDownloaded(Notifications notifications) {
                if (null != getActivity()) {
                    if (notifications != null && notifications.notificationList != null && notifications.notificationList.size() > 0
                            && null != mNotificationsList) {
                        mNotificationsList.addAll(notifications.notificationList);
                        notifyDataSetChanged();
                    }
                    hideProgressBar();
                    if (mSwipeView.isRefreshing())
                        mSwipeView.setRefreshing(false);
                    //setEmptyText(getString(R.string.empty_string));
                    //trackLoadTime(mNavigationTitle, SystemClock.elapsedRealtime() - requestBirthTime);
                }

            }

            @Override
            public void onNotificationsDownloadFailed() {
                if (null != getActivity()) {
                    //trackLoadTime(mNavigationTitle, SystemClock.elapsedRealtime() - requestBirthTime);
                    hideProgressBar();
                    setEmptyText(getString(R.string.empty_string));
                }

            }
        };
    }

    private void clearList() {
        if (null != mNotificationsList) {
            mNotificationsList.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception ex) {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mCurrentPageSize != null)
            mCurrentPageSize.set(1);
        if (null != mNotificationsList) {
            clearList();
            mNotificationsList = null;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {

        if (null != mListItemClkListner) {
            mSelectedItemPos = position;
            String notificationTitle = mNotificationsList.get(mSelectedItemPos).title;
            String deeplinkUrl = mNotificationsList.get(mSelectedItemPos).applink;

            if (!TextUtils.isEmpty(notificationTitle)) {

                if (!TextUtils.isEmpty(deeplinkUrl)) {

                    if (mDeeplinkListener != null) {
                        mDeeplinkListener.onHandleDeepLink(deeplinkUrl);
                    }
                } else {
                    mListItemClkListner.onNotificationHubShareClick(notificationTitle);
                    mListView.setItemChecked(position, true);
                }
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public void refresh() {
        if (null != getActivity()) {
            if (mCurrentPageSize != null)
                mCurrentPageSize.set(1);
            if (mProgressBar != null && null != mNotificationsList) {
                reSetEmptyText();
                mNotificationsList.clear();
                notifyDataSetChanged();
                if (Utility.isInternetOn(getActivity())) {
                    showProgressBar();
                    downloadData();
                }
            }
        }
    }


    public void refreshThePageOnSwipe() {
        if (null != getActivity()) {
            if (mCurrentPageSize != null)
                mCurrentPageSize.set(1);
            if (mProgressBar != null && null != mNotificationsList) {
                reSetEmptyText();
                mNotificationsList.clear();
                notifyDataSetChanged();
                if (Utility.isInternetOn(getActivity())) {
                    //showProgressBar();
                    downloadData();
                }
            }
        }
    }


    @Override
    public void setScreenName() {
        setScreenName(navigation + " - " + mSectionTitle);
    }

    public NotificationItem getOnClickNotification() {
        NotificationItem notificationItem = null;
        if (null != mNotificationsList && mNotificationsList.size() > 0 && mSectionPosition > -1)
            notificationItem = mNotificationsList.get(mSelectedItemPos);
        return notificationItem;
    }

    private void notifyDataSetChanged() {
        if (null != mListView && null != getActivity()) {
            ListAdapter adapter = mListView.getAdapter();
            NotificationsAdapter notificationsAdapter;
            if (adapter instanceof HeaderViewListAdapter) {
                notificationsAdapter = (NotificationsAdapter) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
            } else {
                notificationsAdapter = (NotificationsAdapter) mListView.getAdapter();
            }
            if (null != notificationsAdapter && null != getActivity()) {
                notificationsAdapter.notifyDataSetChanged();

            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (null != getActivity() && isVisibleToUser) {
            loadBannerAds();
        }
    }

    public void loadBannerAds() {
        if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(mNavigationPos, mSectionPosition, null, false, -1, false, false);
        }
    }

    protected void swipeToRefresh(boolean isPageSelected) {
        if (mAdUpdateListener != null)
            mAdUpdateListener.hideIMBannerAd();

        if (isPageSelected) {

            //mSwipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
            mSwipeView.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
            mSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    mSwipeView.setRefreshing(true);

//                    if (mProgressBar != null) {
//                        mProgressBar.setVisibility(View.VISIBLE);
//                    }

                    refreshThePageOnSwipe();

                    //Load the Banner Ad, only if fragment is visible
                    if (getUserVisibleHint())
                        loadBannerAds();

                }
            });
            mSwipeView.setVisibility(View.VISIBLE);
        } else {
            mSwipeView.setVisibility(View.GONE);
            mSwipeView.setOnRefreshListener(null);
        }

    }
}
