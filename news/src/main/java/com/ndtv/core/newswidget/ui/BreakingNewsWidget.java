package com.ndtv.core.newswidget.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.newswidget.dto.NewsWidget;

/**
 * Created by laveen on 8/12/14.
 */
public abstract class BreakingNewsWidget {

    protected int mWidgetIndex;
    protected NewsWidget.NewsWidgetItem mWidgetData;

    /* @Override
     public void onActivityCreated(@Nullable Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         extractBundleData();
     }

     protected void extractBundleData() {
         Bundle bundle = getArguments();
         if (bundle != null) {
             mWidgetIndex = bundle.getInt(Constants.BundleKeys.WIDGET_INDEX, 0);
         }

         mWidgetData = NewsManager.getNewsInstance().getWidgetData(mWidgetIndex);
         if (mWidgetData != null)
             loadWidgetData();
     }
 */
    public View createView(LayoutInflater inflater, ViewGroup container, int position) {
        View view = onCreateView(inflater, container, null);
        mWidgetIndex = position;
        mWidgetData = NewsWidgetManager.getInstance().getWidgetData(mWidgetIndex);
        if (mWidgetData != null)
            loadWidgetData();
        return view;
    }

    public abstract View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    public abstract void loadWidgetData();
}
