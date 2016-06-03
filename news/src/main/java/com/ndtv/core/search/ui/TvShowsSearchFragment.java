package com.ndtv.core.search.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.shows.ShowsManager;
import com.ndtv.core.shows.dto.PrimeShowItem;
import com.ndtv.core.shows.dto.PrimeShows;
import com.ndtv.core.shows.ui.PrimeShowsAdapter;
import com.ndtv.core.ui.BaseActivity;
import com.ndtv.core.ui.widgets.BaseFragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Chandan kumar on 8/7/15.
 */
public class TvShowsSearchFragment extends BaseFragment implements AdapterView.OnItemClickListener, ApplicationConstants.BundleKeys {
    private String mSearchText;
    public ListView mShowsListView;
    public ShowsManager.ShowsDownloadListener mDownloadListener;
    public ProgressBar mProgressBar;
    public int mNavigationPos;
    public String mNavigationTitle;
    public BaseActivity mActivity;
    private List<PrimeShowItem> searchShowsList;
    private TextView mShowsSearchCount;
    private BannerAdFragment.AdListener mAdUpdateListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BaseActivity) {
            mActivity = (BaseActivity) activity;
        }
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity.getActionBarToolbar().setTitle(getString(R.string.shows));
        if (((ActionBarActivity) getActivity()).getSupportActionBar() != null)
            ((ActionBarActivity) getActivity()).getSupportActionBar().show();
    }

    public static Fragment newInstance(String searchText) {
        Fragment searchFragment = new TvShowsSearchFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("Search_Text", searchText);
        searchFragment.setArguments(bundle);
        return searchFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        mSearchText = bundle.getString("Search_Text");
        mNavigationPos = ConfigManager.getInstance().getConfiguration().getNavIndex(getString(R.string.shows));
        searchShowsList = new ArrayList<>();
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
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser)
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
        mShowsSearchCount = (TextView) view.findViewById(R.id.search_shows_count);
        mShowsListView = (ListView) view.findViewById(R.id.prime_shows_list);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
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
                if (null != mActivity) {
                    if (shows != null && shows.primeShowsList != null) {
                        List<PrimeShowItem> mPrimeShows;
                        mPrimeShows = shows.primeShowsList;
                        Collections.sort(mPrimeShows, nameComparator);
                        searchShowsList = getShowsSearchResult(mPrimeShows);
                        setSearchResultTextView(searchShowsList.size());
                        PrimeShowsAdapter showsAdapter = new PrimeShowsAdapter(getActivity(), searchShowsList);
                        if (mShowsListView != null) {
                            mShowsListView.setAdapter(showsAdapter);
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


        mShowsListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                for (int i = 0; i <= mShowsListView.getLastVisiblePosition(); i++) {
                    if (mShowsListView.getChildAt(i) != null) {
                        count++;
                    }
                }
                if (count == searchShowsList.size()) {
                    mShowsListView.setFastScrollEnabled(false);
                    mShowsListView.setFastScrollAlwaysVisible(false);
                    mShowsListView.setVerticalScrollBarEnabled(false);
                } else {
                    mShowsListView.setFastScrollEnabled(true);
                    mShowsListView.setFastScrollAlwaysVisible(true);
                    mShowsListView.setVerticalScrollBarEnabled(true);
                }
            }
        }, 500);
    }

    public List<PrimeShowItem> getShowsSearchResult(List<PrimeShowItem> allShows) {
        List<PrimeShowItem> showsList = new ArrayList<PrimeShowItem>();

        for (int i = 0; i < allShows.size(); i++) {
            PrimeShowItem primeShowItem = allShows.get(i);
            String showsTitle = primeShowItem.getName().toLowerCase();
            if (showsTitle.equalsIgnoreCase(mSearchText.toLowerCase())) {
                showsList.add(primeShowItem);
            }
        }

        for (int i = 0; i < allShows.size(); i++) {
            PrimeShowItem primeShowItem = allShows.get(i);
            String showsTitle[] = primeShowItem.getName().split(" ");
            if (showsTitle[0].toLowerCase().equalsIgnoreCase(mSearchText.toLowerCase()) && !showsList.contains(primeShowItem)) {
                showsList.add(primeShowItem);
            }
        }

        for (int i = 0; i < allShows.size(); i++) {
            PrimeShowItem primeShowItem = allShows.get(i);
            String showsTitle[] = primeShowItem.getName().split(" ");
            if (showsTitle[0].toLowerCase().contains(mSearchText.toLowerCase()) && !showsList.contains(primeShowItem)) {
                showsList.add(primeShowItem);
            }
        }

        for (int i = 0; i < allShows.size(); i++) {
            PrimeShowItem primeShowItem = allShows.get(i);
            String showsTitle = primeShowItem.getName().toLowerCase();
            if (showsTitle.contains(mSearchText.toLowerCase()) && !showsList.contains(primeShowItem)) {
                showsList.add(primeShowItem);
            }
        }

        return showsList;
    }

    /**
     *
     */
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
        if (null != searchShowsList && searchShowsList.size() > pos) {
            PrimeShowItem show = searchShowsList.get(pos);
            if (null != mListItemClkListner && null != show && !TextUtils.isEmpty(show.link))
                mListItemClkListner.onPrimeShowsItemClicked(show.link, show.name, mNavigationPos);
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

    }

    private void setSearchResultTextView(int showsCount) {
        if (getActivity() != null && showsCount > 0) {
            String shows;
            //if (showsCount == 1) {
            shows = getActivity().getString(R.string.search_shows);
//            } else {
//                gallery = getActivity().getString(R.string.search_gelleries);
//            }
            mShowsSearchCount.setVisibility(View.VISIBLE);
            mShowsSearchCount.setText((MessageFormat.format(getActivity().getString(R.string.search_header_text), mSearchText, showsCount + " " + shows)));
        } else {
            mShowsSearchCount.setVisibility(View.GONE);
        }
    }

    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }

    public void loadBannerAds() {
        if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(-1, -1, null, false, -1, false, false);
        }
    }
}
