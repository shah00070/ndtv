/**
 Project      : Awaaz
 Filename     : ShowsFragment.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.shows.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.shows.ShowsManager;
import com.ndtv.core.shows.dto.PrimeShowItem;
import com.ndtv.core.shows.dto.PrimeShows;
import com.ndtv.core.ui.BaseActivity;
import com.ndtv.core.ui.widgets.BaseFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @author Chandan kumar
 */
public class PrimeShowsFragment extends BaseFragment implements OnItemClickListener, ApplicationConstants.FragmentType, ApplicationConstants.BundleKeys {

    public ListView mShowsListView;
    public ShowsManager.ShowsDownloadListener mDownloadListener;
    public ProgressBar mProgressBar;
    public List<PrimeShowItem> mPrimeShows;
    public int mNavigationPos;
    public String mNavigationTitle;
    public BaseActivity mActivity;
    protected CharSequence mCurActionBarTitle;
    private BannerAdFragment.AdListener mAdUpdateListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
        mCurActionBarTitle = mActivity.getActionBarToolbar().getTitle();

        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractArguements();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.prime_shows, container, false);
        initViews(view);
        initListeners();
        initDownloadListeners();
        return view;
    }

    /**
     *
     */
    private void extractArguements() {
        if (getArguments() != null) {
            mNavigationPos = getArguments().getInt(NAVIGATION_POS);
            mNavigationTitle = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPos);
        }
    }

    /**
     * do not forget to call super.onActivityCreated(savedInstanceState); which
     * init's the ActionBarIconListener.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != getActivity()) {
            mListItemClkListner = castToListClkListner();
        }
        downloadData();
        loadBannerAds();
    }


    private void downloadData() {
        if (null != getActivity()) {
            if (Utility.isInternetOn(getActivity()))
                downloadShows();
            else {
                hideProgressBar();
            }
        }
    }

    /**
     * @param view
     */
    private void initViews(View view) {
        mShowsListView = (ListView) view.findViewById(R.id.prime_shows_list);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mShowsListView.setFastScrollEnabled(true);
        mShowsListView.setFastScrollAlwaysVisible(true);
        mShowsListView.setVerticalScrollBarEnabled(true);
    }

    private void initListeners() {
        mShowsListView.setOnItemClickListener(this);
    }

    Comparator<PrimeShowItem> nameComparator = new Comparator<PrimeShowItem>() {
        @Override
        public int compare(PrimeShowItem item1, PrimeShowItem item2) {
            return item1.name.compareTo(item2.name);
        }
    };

    private void initDownloadListeners() {
        mDownloadListener = new ShowsManager.ShowsDownloadListener() {

            @Override
            public void onShowsDownloaded(final PrimeShows shows) {
                if (null != getActivity()) {
                    if (shows != null && shows.primeShowsList != null) {
                        mPrimeShows = shows.primeShowsList;
                        Collections.sort(mPrimeShows, nameComparator);
                        PrimeShowsAdapter showsAdapter = new PrimeShowsAdapter(getActivity(), mPrimeShows);
                        if (mShowsListView != null) {
                            int firstVisiblePos = PreferencesManager.getInstance(getActivity()).getCurrentTvShowPos(PreferencesManager.CURRENT_TV_SHOWS);
                            mShowsListView.setAdapter(showsAdapter);
                            mShowsListView.setSelection(firstVisiblePos);
                        }
                    }
                    setEmptyTextView();
                    hideProgressBar();
                }
            }

            @Override
            public void onShowsDownloadFailed() {
                if (null != getActivity()) {
                    setEmptyTextView();
                    hideProgressBar();
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean isFromNavDrawer = PreferencesManager.getInstance(getActivity()).isFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER);
        if (isFromNavDrawer) {
            PreferencesManager.getInstance(getActivity()).setCurrentScreenForSearch(PreferencesManager.CURRENT_SEARCH_SCREEN, 3);
            PreferencesManager.getInstance(getActivity()).setIsFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER, false);
        }
    }

    protected void setEmptyTextView() {
        if (null != getView())
            mShowsListView.setEmptyView(getView().findViewById(R.id.empty_view));
    }

    private void hideProgressBar() {
        if (null != mProgressBar)
            mProgressBar.setVisibility(View.GONE);
    }

    private void downloadShows() {
        if (null != getActivity() && null != mDownloadListener)
            ShowsManager.getInstance().parseShows(getActivity(), mDownloadListener, mNavigationPos);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (null != mPrimeShows && mPrimeShows.size() > pos) {
            int firstPos = mShowsListView.getFirstVisiblePosition();
            PreferencesManager.getInstance(getActivity()).setCurrentTvShowPos(PreferencesManager.CURRENT_TV_SHOWS, firstPos);
            PrimeShowItem show = mPrimeShows.get(pos);
            if (null != mListItemClkListner && null != show && !TextUtils.isEmpty(show.link)) {
                if (Utility.isInternetOn(getActivity()))
                    mListItemClkListner.onPrimeShowsItemClicked(show.link, show.name, mNavigationPos);
                else
                    Toast.makeText(getActivity(), "Please check your internet connection!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void refresh() {
        if (null != mProgressBar && null != mShowsListView) {
            mShowsListView.setEmptyView(null);
            mShowsListView.setAdapter(null);
            mProgressBar.setVisibility(View.VISIBLE);
        }
        downloadData();
    }

    @Override
    public void setScreenName() {
        setScreenName(mNavigationTitle);
    }

    private void loadBannerAds() {
        if (mAdUpdateListener != null)
            mAdUpdateListener.loadBannerAd(mNavigationPos, 0, null, false, -1, false, false);
    }

}
