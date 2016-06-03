package com.ndtv.core.newswidget.ui;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.R;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.newswidget.dto.NewsWidget;
import com.ndtv.core.ui.NewsListingFragment;
import com.ndtv.core.ui.widgets.ZoomOutPageTransformer;
import com.ndtv.core.util.LogUtils;

/**
 * Created by Harisha B on 18/2/15.
 */
public class NewsWidgetFragment extends NewsListingFragment implements Response.ErrorListener, Response.Listener, Constants,
        View.OnClickListener, NewsWidgetManager.WidgetItemsFromBreakingUpdateListener {

    private ViewGroup mHeaderView;
    protected ViewPager mWidgetPager;
    private NewsWidget mNewsWidget;
    private boolean isListDataSet;
    private boolean mIsWidgetAvailable = false;


    private boolean isWidgetLoading = false;
    CirclePageIndicator indicator;
    private static final String TAG = LogUtils.makeLogTag(NewsWidgetFragment.class);

    @Override
    public void onWidgetItemsUpdated(NewsWidget nw) {
        LogUtils.LOGD(TAG, "i am update called!!");
        if (getActivity()!=null) {
            if ((nw.item==null)||((nw.item!=null)&&(nw.item.size()==0)))
            {
                onErrorResponse(null);
            }
/*            if ((nw.item==null)||((nw.item!=null)&&(nw.item.size()==0)) && mListView!=null && mHeaderView!=null)
            {
                mListView.removeHeaderView(mHeaderView);
                return;
            }
            if ((!mIsWidgetAvailable) && (!NewsWidgetManager.getInstance().isBreakingStoriesAvailable()) && (mListView!=null) && (mHeaderView!=null)) {
                mListView.removeHeaderView(mHeaderView);
                return;
            }*/
            else {
                addNewsWidget();
                setNewsWidegtAdapter();
            }
        }
    }

    public static interface OnClickOfNewsWidget {
        public void onClickOfNewsWidget(String url);
    }

    private OnClickOfNewsWidget mOnClickOfNewsWidget;

    public static Fragment newInstance(String url, int position, String title, int order, int navigationPos) {
        NewsWidgetFragment newsWidgetFragment = new NewsWidgetFragment();
        Bundle bundle = new Bundle();
        bundle.putString("com.july.ndtv.EXTRA_FEED_SLUG", url);
        bundle.putInt("position", position);
        bundle.putString("section", title);
        bundle.putInt("order", order);
        bundle.putInt("navigation_positon", navigationPos);
        newsWidgetFragment.setArguments(bundle);
        return newsWidgetFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        isListDataSet = false;
        super.onActivityCreated(savedInstanceState);
        downloadWidgetData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((!mIsWidgetAvailable) && (!NewsWidgetManager.getInstance().isBreakingStoriesAvailable()) && (mListView!=null) && (mHeaderView!=null)) {
            mListView.removeHeaderView(mHeaderView);
        }
        if (!Utility.isInternetOn(getActivity())) {
            mListView.removeHeaderView(mHeaderView);
            // TODO add scheduled refresh
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        NewsWidgetManager.getInstance().setWidgetUpdateListener(this);
        mOnClickOfNewsWidget = (OnClickOfNewsWidget) activity;
    }

    public void downloadWidgetData() {

        if (getActivity() == null) return;
        if (mProgressBar != null) {
           // mProgressBar.setVisibility(View.VISIBLE);
            isWidgetLoading = true;
        }
        String url = ConfigManager.getInstance().getCustomApiUrl(BREAKING_WIDGET);
        NewsWidgetManager.getInstance().downloadWidgetData(getActivity(), url, this, this);
        /*if (NewsWidgetManager.getInstance().getmWidget().item == null) {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
                isWidgetLoading = true;
            }
            String url = ConfigManager.getInstance().getCustomApiUrl(BREAKING_WIDGET);
            NewsWidgetManager.getInstance().downloadWidgetData(getActivity(), url, this, this);
        } else {
            onResponse(NewsWidgetManager.getInstance().getmWidget());
        }
*/
    }

    public void addNewsWidget() {
        if (mListView.getHeaderViewsCount() > 0) {  //|| mListView.getAdapter() != null
            return;
        }
        if (getActivity()==null) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mHeaderView = (ViewGroup) inflater.inflate(R.layout.fragment_news_widget, mListView, false);
        indicator = (CirclePageIndicator) mHeaderView.findViewById(R.id.page_indicator);
        indicator.setIsRefreshCalledListener(this);
        mWidgetPager = (ViewPager) mHeaderView.findViewById(R.id.pager_widget);
        mWidgetPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        mSwipeView.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mSwipeView.setEnabled(true);
                        break;
                }
                return false;
            }

        });

        mListView.addHeaderView(mHeaderView);
        mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePageHeight(mWidgetPager.getCurrentItem());
                if (Build.VERSION.SDK_INT >= 16)
                    mHeaderView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    protected void setNewsAdapter() {
        if (mIsWidgetAvailable || NewsWidgetManager.getInstance().isBreakingStoriesAvailable()) {
            addNewsWidget();
            setNewsWidegtAdapter();
        }
        if (!isListDataSet ){//|| NewsWidgetManager.getInstance().isBreakingStoriesAvailable()) {
            if (null != mListView) {  //&& mListView.getAdapter() == null
                super.setNewsAdapter();
                // }else{
                // mAdapter.notifyDataSetChanged();
                //}
                isListDataSet = true;
            }
            if (mProgressBar != null && !mIsFetching) {
                mProgressBar.setVisibility(View.GONE);
            }
            isWidgetLoading = false;
        }

    }

    private void updatePageHeight(int pageIndex) {
        String widgetType = NewsWidgetManager.getInstance().getWidgetType(pageIndex);
        if (getActivity() != null) {
            if (widgetType != null && widgetType.equalsIgnoreCase(Constants.WIDGET_TYPE_CUSTOM)) {
                ViewGroup.LayoutParams params = mHeaderView.getLayoutParams();
                Resources resources = getActivity().getResources();
                DisplayMetrics metrics = resources.getDisplayMetrics();
                params.height = (int) (ConfigManager.getInstance().getWidgetHeight(pageIndex, NewsWidgetManager.getInstance().getmWidget()) * (metrics.densityDpi / 160f));
            } else {
                ViewGroup.LayoutParams params = mHeaderView.getLayoutParams();
                Resources resources = getActivity().getResources();
                DisplayMetrics metrics = resources.getDisplayMetrics();
                params.height = (int) (230 * (metrics.densityDpi / 160f));
                //based on aspect ratio of image
                //params.height = (int) Math.round(metrics.widthPixels * 0.75);
            }
        }
        mHeaderView.requestLayout();
    }

    private ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }

        @Override
        public void onPageSelected(int i) {
            updatePageHeight(mWidgetPager.getCurrentItem());
        }

        @Override
        public void onPageScrollStateChanged(int i) {
            if (ViewPager.SCROLL_STATE_IDLE == i) {
            }
        }
    };

    private void setNewsWidegtAdapter() {
        if(getActivity()==null)
        {
            return;
        }
        if (NewsWidgetManager.getInstance().getmWidget().item == null) {
            onRefreshData();
        }
        if (mWidgetPager.getAdapter() == null) {
            mWidgetPager.setPageTransformer(true, new ZoomOutPageTransformer());
            mWidgetPager.setAdapter(new NewsWidgetPagerAdapter(getActivity(), this));

            mWidgetPager.setOffscreenPageLimit(mWidgetPager.getAdapter().getCount());
            LogUtils.LOGD(TAG, "pager adapter count" + mWidgetPager.getAdapter().getCount());
            if(mWidgetPager.getAdapter().getCount()>0) {
                mWidgetPager.getAdapter().instantiateItem(mWidgetPager, 0);
                //if (mWidgetPager.getAdapter().getCount() > 1) {

                    indicator.setFillColor(getResources().getColor(R.color.white)); //list_item_light_grey
                    indicator.setViewPager(mWidgetPager);
                    indicator.setOnPageChangeListener(listener);
                    indicator.notifyDataSetChanged();

                //}
            }
        } else {
            mWidgetPager.getAdapter().notifyDataSetChanged();
            indicator.setFillColor(getResources().getColor(R.color.white)); //list_item_light_grey
            indicator.setViewPager(mWidgetPager);
            indicator.setOnPageChangeListener(listener);
            indicator.notifyDataSetChanged();
        }
    }

    public void refreshWidgetList() {
        if (mWidgetPager != null)
            mWidgetPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        //Toast.makeText(getActivity(),R.string.no_network_msg, Toast.LENGTH_SHORT ).show();
        mIsWidgetAvailable=false;
        if (!isListDataSet) {
            setNewsAdapter();
            isListDataSet = true;
        }
        if ((NewsWidgetManager.getInstance().getmWidget().item != null)&&(NewsWidgetManager.getInstance().getmWidget().item.size()>0)) {
            addNewsWidget();
            setNewsWidegtAdapter();
        } else if (null != mListView) {
            mListView.removeHeaderView(mHeaderView);
            setNewsAdapter();
            isListDataSet = false;
        }
        if (mProgressBar != null && !mIsFetching) {
            mProgressBar.setVisibility(View.GONE);
        }
        isWidgetLoading = false;
    }

    @Override
    public void onResponse(Object o) {
        if (getActivity() == null) return;

        mNewsWidget = (NewsWidget) o;
        if (mNewsWidget.item != null) {
            mIsWidgetAvailable = true;
            addNewsWidget();
            setNewsWidegtAdapter();
        } else {
            mListView.removeHeaderView(mHeaderView);
        }
        if (null != mListView) {
            setNewsAdapter();
            isListDataSet = false;
            //TODO to add footer view
        }

        if (mProgressBar != null && !mIsFetching) {
            mProgressBar.setVisibility(View.GONE);
        }
        isWidgetLoading = false;
    }

    @Override
    public void onClick(View view) {
        int index = (Integer) view.getTag();
        //String url = "ndtv://category=Election Results&subcategory=Results";
        String url = NewsWidgetManager.getInstance().getWidgetAppLink(index);
        mOnClickOfNewsWidget.onClickOfNewsWidget(url);
    }

   /* @Override
    protected void swipeToRefresh(boolean isPageSelected) {
        infiniteScroll();
        super.swipeToRefresh(isPageSelected);
    }*/

    @Override
    public void onReceiveResult(int i, Bundle bundle) {

        if (mSwipeView.isRefreshing())
            mSwipeView.setRefreshing(false);
        if (mProgressBar != null && !isWidgetLoading) {
            mProgressBar.setVisibility(View.GONE);
            if (indicator != null)
                indicator.notifyDataSetChanged();
        }
        mIsFetching = false;

        if (!bundle.getBoolean("more", true)) {
            mListView.setOnScrollListener(null);
            if (mContProgressBar != null)
                mContProgressBar.setVisibility(View.INVISIBLE);
            //mListView.removeFooterView(mFooterView);
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    else
        mAdapter.notifyDataSetChanged();
        //Class cast exception not sure need to verify. .
        // ((NewsListAdapter)((HeaderViewListAdapter)mListView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onRefreshData() {
        super.onRefreshData();
        mNewsWidget = null;
        downloadWidgetData();
    }
}
