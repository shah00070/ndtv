package com.ndtv.core.newswidget.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.NewsManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.util.URLUtility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Comments;
import com.ndtv.core.config.model.NewsItems;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.ui.BaseActivity;
import com.ndtv.core.ui.NewsDetailFragment;
import com.ndtv.core.util.VideoEnabledWebChromeClient;

import static com.ndtv.core.util.LogUtils.LOGE;

/**
 * Created by Harisha B on 20/2/15.
 */
public class DeeplinkingNewsDetailFragment extends NewsDetailFragment implements ApplicationConstants.CommentConstants {

    private static final String TAG = "News Deeplinking";

    private String mDeeplinkUrl, mNewsObjectId, mSubCategory;
    private BannerAdFragment.AdListener mAdUpdateListener;
    private BaseActivity mActivity;
    private NewsItems newsItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       /* View view = inflater.inflate(R.layout.webview_layout, container, false);

        initViews(view);

        return view;*/
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        extractDeeplinkingUrlAndLoad();
        loadBannerAds();
    }

    private void extractDeeplinkingUrlAndLoad() {

        Bundle bundle = getArguments();
        mDeeplinkUrl = bundle.getString(Constants.DEEP_LINK_URL);


        if (!TextUtils.isEmpty(mDeeplinkUrl)) {

            mSubCategory = NewsWidgetManager.getDeeplinkSubcategory(mDeeplinkUrl);
            mNewsObjectId = NewsWidgetManager.getDeeplinkingId(mDeeplinkUrl);
            mDeeplinkUrl = formNewsDetailUrl(mSubCategory, mNewsObjectId);
            if (!TextUtils.isEmpty(mDeeplinkUrl)) {
                loadWebPage(mDeeplinkUrl);
            }
        }

    }

/*    @Override
    public void onPause() {
        super.onPause();
        mHideActionToolBarInterface.setActionBarTitle(mPreviousTitle);
    }*/

    private void loadWebPage(String mDeeplinkUrl) {
        vWebView.getSettings().setJavaScriptEnabled(true);
        vWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Utility.isInternetOn(view.getContext())) {
                    //pass the deeplinking url to HomeActivity onHandleDeepLink method
                    view.loadUrl(url);
                } else {
                    Toast.makeText(view.getContext().getApplicationContext(), R.string.feature_disabled_alert,
                            Toast.LENGTH_LONG).show();
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                mLoadingBar.setVisibility(View.VISIBLE);
//                if (pBar != null)
//                    pBar.setVisibility(View.VISIBLE);
                showProgressBar();
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("about:blank");
                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.no_network_msg), Toast.LENGTH_SHORT).show();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.setVisibility(View.VISIBLE);
//                if (pBar != null)
//                    pBar.setVisibility(View.GONE);
                hideProgressBar();
                view.requestFocus();
//                mLoadingBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
                loadJavaScript();
            }

        });
        VideoEnabledWebChromeClient chromeClient = new VideoEnabledWebChromeClient(
                getView().findViewById(R.id.non_video_view),
                (ViewGroup) getView().findViewById(R.id.fullscreen_layout));
        //chromeClient.setOnToggledFullscreen(mToggledFullscreenCallback);

        //vWebView.setTag(chromeClient);
        //vWebView.setWebChromeClient(chromeClient);
        vWebView.setWebChromeClient(new WebChromeClient());
        vWebView.getSettings().setBuiltInZoomControls(true);
        vWebView.getSettings().setLoadWithOverviewMode(true);
        vWebView.getSettings().setUseWideViewPort(true);
        vWebView.requestFocus();
        vWebView.loadUrl(mDeeplinkUrl);
        vWebView.addJavascriptInterface(new DeeplinkJavaScriptInterface(), "deeplinkinterface");
        // loadJavaScript();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem comentMenuItem = menu.findItem(R.id.menu_comment);
        comentMenuItem.setVisible(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
        mActivity.invalidateOptionsMenu();
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJavaScript() {
        vWebView.loadUrl("javascript:window.deeplinkinterface.setResult( getStoryDetails() )");
    }

    private String formNewsDetailUrl(String subCategory, String mNewsObjectId) {
        String newsDetailApi = ConfigManager.getInstance().getCustomApiUrl(Constants.NEWS_DETAIL_CUSTOM_API);
        String strToReplaceEpisode[] = new String[]{Constants.DEEPLINK_CATEGORY_URL_TAG, Constants.DEEPLINK_URL_TAG_ID};
        String replacementEpisode[] = new String[]{subCategory, mNewsObjectId};
        newsDetailApi = URLUtility.getFinalUrl(strToReplaceEpisode, replacementEpisode, newsDetailApi, getActivity());

        return newsDetailApi;
    }

    @Override
    public void setScreenName() {
        setScreenName(TAG + " - " + mDeeplinkUrl);
    }

    public class DeeplinkJavaScriptInterface {

        @JavascriptInterface
        public void setResult(String daResult) {
            showRetunData(daResult);
        }
    }

    @JavascriptInterface
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

    private void createNewsItem(String javascriptResponse) {
        String data[] = javascriptResponse.split("&&&");
        newsItem = new NewsItems();
        newsItem.id = data[0].split("==")[1];
        newsItem.title = data[1].split("==")[1];
        newsItem.link = data[2].split("==")[1];
        newsItem.identifier = data[3].split("==")[1];
       /* newsItem.keywords = data[5].split("==")[1];*/
        newsItem.device = mDeeplinkUrl;
        getComments(newsItem.identifier);
    }


    public NewsItems getNewsItem() {
        return newsItem;
    }

    public void loadBannerAds() {
        if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(-1, -1, null, false, -1, false, false);
        }
    }

    public void getComments(String storyIdentifier) {
        String url = ConfigManager.getInstance().getCustomApiUrl(GET_COMMENTS_API);
        String strToReplace[] = new String[]{"@identifier"};
        String replacement[] = new String[]{storyIdentifier};
        url = Utility.getFinalUrl(strToReplace, replacement, url, getActivity());
        downloadComments(url);
    }

    protected void downloadComments(String url) {

        NewsManager.getNewsInstance().downloadComments(mActivity, url, new CommentsDownloadListener() {

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


    @Override
    public void onPause() {
        super.onPause();
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(vWebView, (Object[]) null);

        } catch (Exception e) {
            LOGE("WebViewFragment", e.getMessage());
        }
    }


}
