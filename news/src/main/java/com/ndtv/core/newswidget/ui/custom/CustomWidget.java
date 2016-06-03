package com.ndtv.core.newswidget.ui.custom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ndtv.core.R;
import com.ndtv.core.newswidget.ui.BreakingNewsWidget;

/**
 * Created by laveen on 8/12/14.
 */
public class CustomWidget extends BreakingNewsWidget {

    private WebView mWebview;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmemt_widget_item_custom, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mWebview = (WebView) view.findViewById(R.id.widget_webview);
    }

    @Override
    public void loadWidgetData() {
        loadWebPage();
    }

    private void loadWebPage() {
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.loadUrl(mWidgetData.link);
        mWebview.setWebViewClient(new WebViewClient());
    }

}
