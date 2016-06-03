package com.ndtv.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ndtv.core.R;
import com.ndtv.core.common.util.util.UiUtility;
import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.ui.widgets.DetailFragment;
import com.ndtv.core.util.AppReviewHelper;
import com.ndtv.core.util.FileUtils;
import com.ndtv.core.util.VideoEnabledWebChromeClient;
import com.ndtv.core.util.VideoEnabledWebView;

import java.io.File;
import java.io.IOException;

import static com.ndtv.core.provider.ContentProviderUtils.updateReadStatus;
import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.LOGE;

/**
 * Created by Srihari S Reddy on 23/12/14.
 */
public class NewsDetailFragment extends BaseFragment {

    private static final String TAG = "Story Detail";
    private String mId;
    private String mTitle;
    private String mStoryImage;
    private String mContentUrl;
    private int mNavigationPos;
    private int mSectionPos;
    private OnInLineLinkClickListener mONOnInLineLinkClickListener;
    private BaseActivity mActivity;
    //protected ProgressBar pBar;
    public VideoEnabledWebView vWebView;
    private int isRFPrompt;
    private TextView RatingsPrompt;
    private ProgressBar mProgressBar;
    private Button R_button_off;
    private Button R_button_on;
    private RelativeLayout Ratingslayout;

    //File upload related for eyewitness
    private static final String IMAGE = "image/*";
    private static final String VIDEO = "video/*";
    //by default video is selected
    private String mTypeCap = VIDEO;
    // variables for camera and choosing files methods
    private ValueCallback<Uri> mFilePathCallback;
    // the same for Android 5.0 methods only
    private ValueCallback<Uri[]> mFilePathCallbackArray;
    private String mCameraPhotoPath;
    public static final int FILE_CHOOSER_RESULT_CODE = 700;
    private static final String IWITNESS_NEWS = "Iwitness News";

    public boolean handleBackPress() {
        if (getView() != null) {
            WebView webView = (WebView) getView().findViewById(R.id.item_story_content);
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return false;
    }

    public static interface OnInLineLinkClickListener {
        public void lauchDeepLinking(String type);
    }


    public static NewsDetailFragment newInstance(String id, String title, String storyImage, String contentUrl, int mNavigationPos, int mSectionPos) {

        NewsDetailFragment f = new NewsDetailFragment();

        Bundle args = new Bundle();
        args.putString("Title", title);
        args.putString("StoryImage", storyImage);
        args.putString("ContentUrl", contentUrl);
        args.putInt("navigation_positon", mNavigationPos);
        args.putInt("setion_positon", mSectionPos);
        args.putString("id", id);
        f.setArguments(args);
        return f;
    }

    public static NewsDetailFragment newInstance(String id, String title, String storyImage, String contentUrl) {
        NewsDetailFragment f = new NewsDetailFragment();

        Bundle args = new Bundle();
        args.putString("Title", title);
        args.putString("StoryImage", storyImage);
        args.putString("ContentUrl", contentUrl);
        args.putString("id", id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Added to fix Audio Focus issue in Webkit Pages
        UiUtility.setCurrentFragment(this.getClass());
        LOGD(TAG, this.getClass().toString());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        mTitle = bundle.getString("Title");
        mStoryImage = bundle.getString("StoryImage");
        mContentUrl = bundle.getString("ContentUrl");
        mNavigationPos = bundle.getInt("navigation_positon");
        mSectionPos = bundle.getInt("setion_positon");
        mId = bundle.getString("id");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
        // mActivity.showOverflowMenu(true);
        mONOnInLineLinkClickListener = (OnInLineLinkClickListener) activity;
    }

    @Override
    public void setScreenName() {
        setScreenName(TAG + "-" + mId + " - " + mTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.news_detail_fragment, container, false);
        //pBar = (ProgressBar) view.findViewById(R.id.DetailProgressBar);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        vWebView = (VideoEnabledWebView) view.findViewById(R.id.item_story_content);
        vWebView.getSettings().setJavaScriptEnabled(true);
        vWebView.getSettings().setDomStorageEnabled(true);
        vWebView.setBackgroundColor(Color.parseColor("#ffffff"));
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);

        //Needed for file upload feature
        vWebView.setWebChromeClient(new WebChromeClient() {

            // file upload callback (Android 2.2 (API level 8) -- Android 2.3 (API level 10)) (hidden method)
            public void openFileChooser(ValueCallback<Uri> filePathCallback) {
                showAttachmentDialog(filePathCallback);
            }

            // file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
            public void openFileChooser(ValueCallback filePathCallback, String acceptType) {
                showAttachmentDialog(filePathCallback);
            }

            // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
            public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
                showAttachmentDialog(filePathCallback);

            }

            // file upload callback (Android 5.0 (API level 21) -- current) (public method)

            // for Lollipop, all in one
            @Override
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                // Double check that we don't have any existing callbacks
                if (mFilePathCallbackArray != null) {
                    mFilePathCallbackArray.onReceiveValue(null);
                }
                mFilePathCallbackArray = filePathCallback;
                // Set up the take picture intent

                if (mTypeCap == IMAGE) {
                    Intent takePictureIntent = pictureIntentSetup();
                    return showChooserDialog(takePictureIntent);
                }
                //set up video capture intent
                else {
                    Intent takeVideoIntent = videoIntentSetUp();
                    return showChooserDialog(takeVideoIntent);
                }

            }

        });

        View loadingView = LayoutInflater.from(getActivity()).inflate(R.layout.view_loading_video, null); // Your own view, read class comments

        VideoEnabledWebChromeClient chromeClient;
        vWebView.setWebChromeClient(chromeClient = new VideoEnabledWebChromeClient(view.findViewById(R.id.non_video_view),
                (ViewGroup) view.findViewById(R.id.video_container), loadingView, vWebView) {
            // file upload callback (Android 2.2 (API level 8) -- Android 2.3 (API level 10)) (hidden method)
            public void openFileChooser(ValueCallback<Uri> filePathCallback) {
                showAttachmentDialog(filePathCallback);
            }

            // file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
            public void openFileChooser(ValueCallback filePathCallback, String acceptType) {
                showAttachmentDialog(filePathCallback);
            }

            // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
            public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
                showAttachmentDialog(filePathCallback);

            }

            // file upload callback (Android 5.0 (API level 21) -- current) (public method)

            // for Lollipop, all in one
            @Override
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                // Double check that we don't have any existing callbacks
                if (mFilePathCallbackArray != null) {
                    mFilePathCallbackArray.onReceiveValue(null);
                }
                mFilePathCallbackArray = filePathCallback;
                // Set up the take picture intent

                if (mTypeCap == IMAGE) {
                    Intent takePictureIntent = pictureIntentSetup();
                    return showChooserDialog(takePictureIntent);
                }
                //set up video capture intent
                else {
                    Intent takeVideoIntent = videoIntentSetUp();
                    return showChooserDialog(takeVideoIntent);
                }

            }

        });
        chromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                if (getActivity() != null)
                    if (fullscreen) {
                        ((ActionBarActivity) getActivity()).getSupportActionBar().hide();
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    } else {
                        showActionAndStatusBar();
                    }
            }
        });
        vWebView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void performClick(String type) throws Exception {
                Log.d("LOGIN::", "Clicked");
                if (type.equalsIgnoreCase("image")) {
                    mTypeCap = IMAGE;
                } else {
                    mTypeCap = VIDEO;
                }

            }
        }, "videos");


        vWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
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
            public void onPageFinished(WebView dview, String url) {
                super.onPageFinished(dview, url);
                hideProgressBar();
                //pBar.setVisibility(View.GONE);
                AppReviewHelper.incrementCount(getActivity());
                if (AppReviewHelper.shouldShowRatings(getActivity())) {
                    initRFLayout(view);
                    AppReviewHelper.resetReviewCount(getActivity());
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (pBar != null)
//                    pBar.setVisibility(View.VISIBLE);


                if (handleInlineDeeplinking(view, url))
                    return true;
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        if (mContentUrl != null) {
            //vWebView.clearCache(true);
            vWebView.loadUrl(mContentUrl);
        }


        return view;
    }

    private void showActionAndStatusBar() {
        ((ActionBarActivity) getActivity()).getSupportActionBar().show();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getActivity().getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private boolean handleInlineDeeplinking(WebView view, String url) {
        if (url.startsWith("mailto:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            startActivity(intent);
            return true;
        } else if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL,
                    Uri.parse(url));
            startActivity(intent);
            return true;
        } else {
            if (NewsWidgetManager.getDeeplinkCategory(url) == null)
                view.loadUrl(url);
            else {
                if (mDeeplinkListener != null)
                    mDeeplinkListener.onHandleDeepLink(url);
            }
            return true;
        }

    }

    protected void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }


    private void showAttachmentDialog(ValueCallback<Uri> filePathCallback) {
        mFilePathCallback = filePathCallback;
//        if (mTypeCap == IMAGE) {
//            //set up image capture intent
//            Intent takePictureIntent = pictureIntentSetup();
//             showChooserDialog(takePictureIntent);
//        }
//        //set up video capture intent
//        else {
//            Intent takeVideoIntent = videoIntentSetUp();
//             showChooserDialog(takeVideoIntent);
//        }
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        if (mTypeCap.equalsIgnoreCase(IMAGE))
            i.setType(IMAGE);
        else
            i.setType(VIDEO);
        getActivity().startActivityForResult(Intent.createChooser(i, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    // creating image files (Lollipop only)
    private File createImageFile() throws IOException {

        // Create an image file name

        String fileName = new String(FileUtils.getDiskCacheDir(getActivity(), "iWitness").getAbsolutePath()
                + System.currentTimeMillis() + ".jpg");
        File imageFile = new File(fileName);

        return imageFile;
    }


    // creating video files (Lollipop only)
    private File createVideoFile() throws IOException {

        // Create an video file name

        String fileName = new String(FileUtils.getDiskCacheDir(getActivity(), "iWitness").getAbsolutePath()
                + System.currentTimeMillis() + ".mp4");
        File videoFile = new File(fileName);
        return videoFile;
    }


    private Intent pictureIntentSetup() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

            // create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("Failed", "Unable to create Image File", ex);
            }

            // continue only if the file was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }
        return takePictureIntent;

    }


    private Intent videoIntentSetUp() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {

            // create the file where the video should go
            File videoFile = null;
            try {
                videoFile = createVideoFile();
                takeVideoIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("Failed", "Unable to create Video File", ex);
            }

            // continue only if the file was successfully created
            if (videoFile != null) {
                mCameraPhotoPath = "file:" + videoFile.getAbsolutePath();
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(videoFile));
            } else {
                takeVideoIntent = null;
            }
        }
        return takeVideoIntent;
    }


    private boolean showChooserDialog(Intent intent) {
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        if (mTypeCap.equalsIgnoreCase(IMAGE))
            contentSelectionIntent.setType(IMAGE);
        else
            contentSelectionIntent.setType(VIDEO);

        Intent[] intentArray;
        if (intent != null) {
            intentArray = new Intent[]{intent};
        } else {
            intentArray = new Intent[0];
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        if (mTypeCap.equalsIgnoreCase(IMAGE))
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        else
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Video Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

        getActivity().startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);

        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intentData) {
        super.onActivityResult(requestCode, resultCode, intentData);
        // code for all versions except of Lollipop
        // code for all versions except of Lollipop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Uri result = null;
            // check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FILE_CHOOSER_RESULT_CODE) {
                    if (null == this.mFilePathCallback) {
                        return;
                    }
                    if (null == mFilePathCallback) return;


                    if (intentData == null) {
                        // if there is not data, then we may have taken a photo
                        if (mCameraPhotoPath != null) {
                            result = Uri.parse(mCameraPhotoPath);
                        }
                    } else {
                        String dataString = intentData.getDataString();
                        if (dataString != null) {
                            result = Uri.parse(dataString);
                        }
                    }
                }

                //  for Lollipop only
            }
            mFilePathCallback.onReceiveValue(result);
            mFilePathCallback = null;

            //  for Lollipop only
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Uri[] results = null;
            // check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FILE_CHOOSER_RESULT_CODE) {
                    if (null == this.mFilePathCallbackArray) {
                        return;
                    }
                    if (intentData == null) {
                        // if there is not data, then we may have taken a photo
                        if (mCameraPhotoPath != null) {
                            results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                        }
                    } else {
                        String dataString = intentData.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mFilePathCallbackArray.onReceiveValue(results);
            mFilePathCallbackArray = null;


        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity.showOverflowMenu(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Added to fix the issue observed in lollypop devices
        Fragment fragment = getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.container);
        if (fragment != null && fragment instanceof DetailFragment) {
            if (((DetailFragment) fragment).getSectionTitle() != null) {
                if (!((DetailFragment) fragment).getSectionTitle().equalsIgnoreCase(IWITNESS_NEWS)) {

                    try {
                        Class.forName("android.webkit.WebView")
                                .getMethod("onPause", (Class[]) null)
                                .invoke(vWebView, (Object[]) null);


                    } catch (Exception e) {
                        LOGE(TAG, e.getMessage());
                    }
                }
            }
        }
//        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            updateReadStatus(getActivity(), mId);
        } else {
            if (vWebView != null) {
                vWebView.reload();
            }
        }

    }

    private void initRFLayout(View view) {
        Ratingslayout = (RelativeLayout) view.findViewById(R.id.Ratingslayout);
        RatingsPrompt = (TextView) view.findViewById(R.id.RatingsPrompt);
        R_button_off = (Button) view.findViewById(R.id.R_button_off);
        R_button_on = (Button) view.findViewById(R.id.R_button_on);

        Ratingslayout.setVisibility(View.VISIBLE);

        R_button_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRFPrompt != 0) {
                    Ratingslayout.setVisibility(View.GONE);

                } else {
                    isRFPrompt = -1;
                    RatingsPrompt.setText(R.string.f_prompt);
                    R_button_off.setText(R.string.f_prompt_no);
                    R_button_off.setText(R.string.f_prompt_yes);
                }

            }
        });

        R_button_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRFPrompt == 1) {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getActivity().getPackageName())));
                    Ratingslayout.setVisibility(View.GONE);
                    AppReviewHelper.setUserStatus(getActivity());
                }
                if (isRFPrompt == -1) {
                    // ((HomeActivity) getActivity()).addNavgationFragmentFromGCM("Settings/Feedback");

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", getResources().getString(R.string.f_email_address), null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.f_email_subject));
                    startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.f_email_prompt)));

                    Ratingslayout.setVisibility(View.GONE);

                } else if (isRFPrompt == 0) {
                    isRFPrompt = 1;
                    RatingsPrompt.setText(R.string.r_prompt);
                    R_button_off.setText(R.string.r_prompt_no);
                    R_button_off.setText(R.string.r_prompt_yes);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        //Added to fix Audio Focus issue in Webkit Pages
        UiUtility.setCurrentFragment(null);
    }
}
