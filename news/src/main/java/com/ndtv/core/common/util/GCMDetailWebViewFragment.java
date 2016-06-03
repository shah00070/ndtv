package com.ndtv.core.common.util;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Comments;
import com.ndtv.core.config.model.NewsItems;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.ui.BaseActivity;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.ui.widgets.DetailFragment;
import com.ndtv.core.util.VideoEnabledWebChromeClient;

import static com.ndtv.core.util.LogUtils.LOGE;


public class GCMDetailWebViewFragment extends BaseFragment implements ApplicationConstants.BundleKeys {
    private static final String LOG_TAG = "Push Notification Deeplinking";
    private static final int DEFAULT_TAB_POS = 999;
    public WebView mWebView;
    private int mSectionPosition;
    private int mNavigationPosition;
    private String mNavigationUrl;
    private String mNewsItemID;
    private ProgressBar mProgressBar;
    private String title;
    private VideoEnabledWebChromeClient mWebChromeClient;
    private boolean mIsDeepLinkFlag = false;
    private BaseActivity mActivity;
    private BannerAdFragment.AdListener mAdUpdateListener;
    private NewsItems newsItem;
    private DetailFragment detailFragment;


    public static Fragment newInstance(String mNavigationUrl, int position, String title, int navigationPos, String mNewsItemID) {
        GCMDetailWebViewFragment webViewFragment = new GCMDetailWebViewFragment();
        webViewFragment.mSectionPosition = position;
        webViewFragment.title = title;
        webViewFragment.mNavigationUrl = mNavigationUrl;
        webViewFragment.mNavigationPosition = navigationPos;
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("section", title);
        bundle.putString(URL_STRING, mNavigationUrl);
        bundle.putString(NEWSITEMID, mNewsItemID);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, navigationPos);
        webViewFragment.setArguments(bundle);

        return webViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractArguements();
        getActivity().invalidateOptionsMenu();

    }

    private void extractArguements() {
        if (getArguments() != null) {
            mSectionPosition = getArguments().getInt(SECTION_POSITION);
            mNavigationPosition = getArguments().getInt(NAVIGATION_POS);
            mNavigationUrl = getArguments().getString(URL_STRING);
            mNewsItemID = getArguments().getString(NEWSITEMID);
            //For Deeplinking
            mIsDeepLinkFlag = getArguments().getBoolean(IS_DEEPLINK_URL);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_webview_gcmdetail, container, false);

        mWebView = (WebView) rootView.findViewById(R.id.webView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(new GCMDetailJavaScriptInterface(), "gcmdetailinterface");
        mWebChromeClient = new VideoEnabledWebChromeClient(rootView.findViewById(R.id.non_video_view),
                (ViewGroup) rootView.findViewById(R.id.fullscreen_layout));
        mWebChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                // Your code to handle the full-screen change, for example
                // showing and hiding the title bar
            }
        });
        //  mWebView.setWebChromeClient(mWebChromeClient);

        initListeners();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadWebPageUrl();
        loadBannerAds();
    }


    @Override
    public void onResume() {
        super.onResume();
        //For NewsWidgetCustom,setting empty title.
//        if (getActivity() != null && PreferencesManager.getInstance(getActivity()).isFromDeepLinking()) {
//            getActivity().setTitle("");
//            PreferencesManager.getInstance(getActivity()).setIsFromDeepLinking(false);
//        }
    }

    @Override
    public void setScreenName() {
        if (mNavigationUrl != null)
            setScreenName(LOG_TAG + " - " + mNewsItemID + " - " + mNavigationUrl);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.detail_menu_video, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void loadWebPageUrl() {
        String url = null;
        if (mIsDeepLinkFlag)
            url = getArguments().getString(DEEP_LINK_URL);
        else
            url = mNavigationUrl;
        mWebView.loadUrl(url);
    }

    public void loadJavaScript() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            mWebView.evaluateJavascript("javascript:getStoryDetails();", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String dataResult) {
                    if (dataResult != null)
                        showRetunData(dataResult);
                }
            });

        } else {
            mWebView.loadUrl("javascript:window.gcmdetailinterface.setResult(getStoryDetails());");
        }
    }

    public class GCMDetailJavaScriptInterface {

        @JavascriptInterface
        public void setResult(String daResult) {
            if (daResult != null)
                showRetunData(daResult);
        }
    }

    public void showRetunData(final String data) {
        Log.d("JDATA", "RESPONSE:" + data);
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    createNewsItem(data);
                }
            });
    }


    private void initListeners() {

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showProgressBar();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("about:blank");
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_network_msg), Toast.LENGTH_SHORT).show();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideProgressBar();
                loadJavaScript();
            }

            @Override
            public void onLoadResource(WebView view, String url) {
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.setVisibility(View.VISIBLE);
                view.requestFocus();
                view.loadUrl(url);
                return true;
            }
        });
    }

    public void hideProgressBar() {
        if (null != mProgressBar) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     *
     */
    public void showProgressBar() {
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

    private void createNewsItem(String javascriptResponse) {
        String data[] = javascriptResponse.split("&&&");
        newsItem = new NewsItems();
        newsItem.id = data[0].split("==")[1];
        newsItem.title = data[1].split("==")[1];
        newsItem.link = data[2].split("==")[1];
        newsItem.identifier = data[3].split("==")[1];
        getComments(newsItem.identifier);
    }

    public NewsItems getNewsItem() {
        return newsItem;
    }


    public void getComments(String storyIdentifier) {
        String url = ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CommentConstants.GET_COMMENTS_API);
        String strToReplace[] = new String[]{"@identifier"};
        String strForStoryId = storyIdentifier;
        String replacement[] = new String[]{strForStoryId};
        url = Utility.getFinalUrl(strToReplace, replacement, url, getActivity());
        downloadComments(url);
    }

    protected void downloadComments(String url) {

        NewsManager.getNewsInstance().downloadComments(getActivity(), url, new CommentsDownloadListener() {

            @Override
            public void onDownloadFailed() {
                if (mActivity != null) {
                    mActivity.setCommentCount("0");
                }
            }

            @Override
            public void onDownloadComplete(Comments comments) {
                if (comments != null && mActivity != null) {
                    mActivity.setCommentCount(comments.pager.count_with_replies.toString());
                }
            }
        });
    }
}
