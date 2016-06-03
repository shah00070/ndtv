package com.ndtv.core.cricket.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Section;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.util.VideoEnabledWebChromeClient;
import com.ndtv.core.util.VideoEnabledWebView;

import static com.ndtv.core.util.LogUtils.LOGE;

/**
 * Created by Chandan kumar on 2/16/2015.
 */

public class WebViewFragment extends BaseFragment implements ApplicationConstants.BundleKeys {

    private static final int DEFAULT_TAB_POS = 999;
    private static final String TAG = "WebviewFragment";


    public VideoEnabledWebView mWebView;
    private int mSectionPosition;
    private int mNavigationPosition;
    private String mNavigationUrl;
    private ProgressBar mProgressBar;
    private String title;
    private VideoEnabledWebChromeClient mWebChromeClient;
    private boolean mIsDeepLinkFlag = false;
    private BannerAdFragment.AdListener mAdUpdateListener;
    private String navigation;
    private int checkedId;
    private boolean bIsFromSettings;


    public static WebViewFragment newInstance(String mNavigationUrl, int position, String title, int navigationPos, boolean isFromSettings) {
        WebViewFragment webViewFragment = new WebViewFragment();
        webViewFragment.mSectionPosition = position;
        webViewFragment.title = title;
        webViewFragment.mNavigationUrl = mNavigationUrl;
        webViewFragment.mNavigationPosition = navigationPos;
        webViewFragment.bIsFromSettings = isFromSettings;
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("section", title);
        bundle.putString(URL_STRING, mNavigationUrl);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, navigationPos);
        webViewFragment.setArguments(bundle);
        return webViewFragment;
    }

    public static WebViewFragment newInstance(int checkedId, String mNavigationUrl, int position, String title, int navigationPos) {
        WebViewFragment webViewFragment = new WebViewFragment();
        webViewFragment.checkedId = checkedId;
        webViewFragment.mSectionPosition = position;
        webViewFragment.title = title;
        webViewFragment.mNavigationUrl = mNavigationUrl;
        webViewFragment.mNavigationPosition = navigationPos;
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("section", title);
        bundle.putString(URL_STRING, mNavigationUrl);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, navigationPos);
        webViewFragment.setArguments(bundle);
        return webViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideKeyBoardIfVisible();
        extractArguements();
        if (bIsFromSettings && mAdUpdateListener != null)
            mAdUpdateListener.hideIMBannerAd();
    }


    private void extractArguements() {
        if (getArguments() != null) {
            mSectionPosition = getArguments().getInt(SECTION_POSITION);
            mNavigationPosition = getArguments().getInt(NAVIGATION_POS);
            mNavigationUrl = getArguments().getString(URL_STRING);
            //For Deeplinking
            mIsDeepLinkFlag = getArguments().getBoolean(IS_DEEPLINK_URL);
        }
        if (ConfigManager.getInstance() != null && ConfigManager.getInstance().getConfiguration() != null)
            navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPosition);
        else
            navigation = TAG;
    }

    @Override
    public void setScreenName() {
        // setScreenName(TAG + " " + title);
        Section section = ConfigManager.getInstance().getSection(mSectionPosition, mNavigationPosition);
        if (section != null && section.getTabList() != null)
            setScreenName(navigation + " - " + title + " - " + section.getTabList().get(checkedId).getTitle());
        else
            setScreenName(navigation + " - " + title);
    }

    private void hideKeyBoardIfVisible() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_webview, container, false);

        mWebView = (VideoEnabledWebView) rootView.findViewById(R.id.webView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        // mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebChromeClient = new VideoEnabledWebChromeClient(rootView.findViewById(R.id.non_video_view),
                (ViewGroup) rootView.findViewById(R.id.fullscreen_layout));
        mWebChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                // Your code to handle the full-screen change, for example
                // showing and hiding the title bar
            }
        });
        mWebView.setWebChromeClient(mWebChromeClient);
        initListeners();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadWebPageUrl();
        if (!bIsFromSettings)
            loadBannerAds();
    }

    @Override
    public void onResume() {
        super.onResume();
        //For NewsWidgetCustom,setting empty title.
        if (getActivity() != null && PreferencesManager.getInstance(getActivity()).isFromDeepLinking()) {
            getActivity().setTitle("");
            PreferencesManager.getInstance(getActivity()).setIsFromDeepLinking(false);
        }
    }

    private void loadWebPageUrl() {
        String url = null;
        if (mIsDeepLinkFlag)
            url = getArguments().getString(DEEP_LINK_URL);
        else
            url = mNavigationUrl;
        mWebView.loadUrl(url);
    }

    private void initListeners() {

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                showProgressBar();
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("about:blank");
                if (getActivity() != null)
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_network_msg), Toast.LENGTH_SHORT).show();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                hideProgressBar();
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("URLS", "URL: " + url);
                view.setVisibility(View.VISIBLE);
                view.requestFocus();
                view.loadUrl(url);

                return true;
            }
        });
    }

    private void hideProgressBar() {
        if (null != mProgressBar) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     *
     */
    private void showProgressBar() {
        if (null != mProgressBar) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public boolean handleBackPressed() {
        boolean shouldNotCallSuper = false;
        WebView webView = mWebView;
        if (webView != null) {
            VideoEnabledWebChromeClient chromeClient = mWebChromeClient;
            if (chromeClient.onBackPressed()) {
                shouldNotCallSuper = true;
                return shouldNotCallSuper;
            }
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                shouldNotCallSuper = true;
                return shouldNotCallSuper;
            } else {
                mWebView.loadUrl("about:blank");
            }
        }
        return shouldNotCallSuper;
    }

    @Override
    public void onDetach() {
        mWebView = null;
        super.onDetach();
    }

    private void loadBannerAds() {
        if (mAdUpdateListener != null)
            mAdUpdateListener.loadBannerAd(mNavigationPosition, mSectionPosition, null, false, -1, false, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mWebView != null) {
            mWebView.reload();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(mWebView, (Object[]) null);

        } catch (Exception e) {
            LOGE("WebViewFragment", e.getMessage());
        }
    }
}
