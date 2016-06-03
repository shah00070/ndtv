package com.ndtv.core.newswidget.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.newswidget.ui.custom.CustomWidget;
import com.ndtv.core.newswidget.ui.custom.TextWidget;
import com.ndtv.core.newswidget.ui.custom.VideoWidgetFragment;

/**
 * Created by Harisha B on 18/2/15.
 */
public class NewsWidgetPagerAdapter extends PagerAdapter {

    private LayoutInflater mInflater;

    public View.OnClickListener mListener;

    public NewsWidgetPagerAdapter(Context context, View.OnClickListener mListener) {
        mInflater = LayoutInflater.from(context);
        this.mListener = mListener;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return NewsWidgetManager.getInstance().getWidgetCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        BreakingNewsWidget widget = getWidgetItem(position);
        View view = widget.createView(mInflater, container, position);
        if (mListener != null) {
            view.setOnClickListener(mListener);

            //Temporary fix need to change this
            //Added this because widget of type custom uses webview and webview is not taking click listner
            //As a workaround to this problem a dummy view is added infront of webview
            //setting onclick listener to dummy view and getting its onclick listener
            if (((ViewGroup) view).getChildCount() > 1) {
                ((ViewGroup) view).getChildAt(1).setOnClickListener(mListener);
                ((ViewGroup) view).getChildAt(1).setTag(position);
            }
            view.setTag(position);
        }
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

        public BreakingNewsWidget getWidgetItem(int index) {
            String widgetType = NewsWidgetManager.getInstance().getWidgetType(index);
            BreakingNewsWidget fragment = null;
            if (widgetType.equalsIgnoreCase(Constants.WIDGET_TYPE_CUSTOM)) {
                fragment = new CustomWidget();
            } else if ((widgetType.equalsIgnoreCase(Constants.WIDGET_TYPE_TEXT))||(widgetType.equalsIgnoreCase(Constants.WIDGET_TYPE_COLLAGE))) {
                fragment = new TextWidget();
            } else if (widgetType.equalsIgnoreCase(Constants.WIDGET_TYPE_PHOTO)) {
                fragment = new PhotoWidgetFragment();
            } else if (widgetType.equalsIgnoreCase(Constants.WIDGET_TYPE_VIDEO)
                    || widgetType.equalsIgnoreCase(Constants.WIDGET_TYPE_VIDEOS) ||
                    widgetType.equalsIgnoreCase(Constants.WIDGET_TYPE_TV)) {
                fragment = new VideoWidgetFragment();
            }
            /*} else if (widgetType.equalsIgnoreCase(Constants.WIDGET_TYPE_LIVETV)) {
                fragment = new VideoWidgetFragment();
            }*//* else {
                fragment = new TextWidget();*//*
            }*/
            else {
                fragment = new CustomWidget();
            }
            return fragment;
        }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
