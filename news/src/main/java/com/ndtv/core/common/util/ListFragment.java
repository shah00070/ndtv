/**
 Project      : Awaaz
 Filename     : ListFragment.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ndtv.core.R;
import com.ndtv.core.ui.widgets.BaseFragment;


/**
 * @author anudeep
 */
public abstract class ListFragment extends BaseFragment implements OnScrollListener {

    final String LOG_TAG = ListFragment.class.getSimpleName();

    protected ListView mListView;
    protected int mSelectedItemPos;
    protected View mFooterView;
    protected ProgressBar mProgressBar;
    protected TextView mEmptyView;
    protected SwipeRefreshLayout mSwipeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_view_layout, null);
        initViews(view, inflater);
        return view;
    }

    protected void initViews(View view, LayoutInflater inflater) {
        mListView = (ListView) view.findViewById(R.id.list_view);
        // mListviView.setLayoutAnimation(getListViewFadeInAnimation());
    	mSwipeView = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mFooterView = inflater.inflate(R.layout.progressbar_layout, null);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mEmptyView = (TextView) view.findViewById(R.id.empty_view);
//        if (this instanceof NotificationHubFragment) {
//            mListView.setDivider(null);
//        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListItemClkListner = castToListClkListner();

        ListView listView = (ListView) getView().findViewById(R.id.list_view);
        listView.setOnScrollListener(this);
    }

    /**
     * Do not forget to call super.OnDetach() to clear all listeners in
     * BaseFragment.
     */
    @Override
    public void onDetach() {
        mListView = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

//        if (mImageLoader != null) {
//            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
//                mImageLoader.stopProcessingQueue();
//            } else {
//                mImageLoader.startProcessingQueue();
//            }
//        }

        if (null != mFooterView)
            mFooterView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // Log.d(LOG_TAG, "firstVisibleItem" + firstVisibleItem +
        // "  visibleItemCount" + visibleItemCount
        // + "  totalItemCount" + totalItemCount);
        if (Utility.isInternetOn(getActivity())) {
            if (firstVisibleItem + visibleItemCount == totalItemCount
                    && mListView.getChildAt(visibleItemCount - 1) != null
                    && mListView.getChildAt(visibleItemCount - 1).getBottom() <= mListView.getHeight()) {
                fetchNextPage(totalItemCount, totalItemCount);
            }
        }
    }

    /**
     * Let the child fragment implement the pagination logic
     *
     * @param currentlyAvailableCount
     * @param totalItemCount
     */
    protected synchronized void fetchNextPage(int currentlyAvailableCount, int totalItemCount) {

    }

    /**
     *
     */
    protected void addFooterView() {
        if (null != getActivity()) {
            if (null != mListView && null != mFooterView) {
                if (mListView.getFooterViewsCount() == 0) {
                    mListView.addFooterView(mFooterView);
                }
            }
        }
    }

    /**
     *
     */
    protected void removeFooterView() {
        if (null != mListView && null != mFooterView && mListView.getFooterViewsCount() > 0) {
            mListView.removeFooterView(mFooterView);
        }

    }

    protected void hideFooterView() {
        if (null != mFooterView)
            mFooterView.setVisibility(View.GONE);
    }

    protected void showFooterView() {
        if (null != mFooterView)
            mFooterView.setVisibility(View.VISIBLE);
    }


    protected void hideProgressBar() {
        if (null != mProgressBar) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    protected void showProgressBar() {
        if (null != mProgressBar) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }


    /**
     *
     */
    protected void setEmptyText(String text) {
        if (null != mEmptyView && null != mListView) {
            mEmptyView.setText(text);
            mListView.setEmptyView(mEmptyView);
        }
    }

    protected void reSetEmptyText() {
        if (null != mListView) {
            mListView.setEmptyView(null);
        }
    }

}
