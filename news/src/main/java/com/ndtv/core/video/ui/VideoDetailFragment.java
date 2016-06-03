package com.ndtv.core.video.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.ndtv.core.R;
import com.ndtv.core.common.util.SplashAdManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.share.ShareApp;
import com.ndtv.core.share.ShareItem;
import com.ndtv.core.ui.ChromecastPlayFragment;
import com.ndtv.core.ui.listener.OnShareInterface;
import com.ndtv.core.util.ChromecastUtil;
import com.ndtv.core.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laveen on 17/2/15.
 */
public class VideoDetailFragment extends ChromecastPlayFragment {

    private static final String TAG = "Video";

    public static final String VIDEO_TITLE = "video_item_title";
    public static final String VIDEO_DESCRIPTION = "video_item_desc";
    public static final String VIDEO_PLAY_URL = "video_item_path";
    public static final String VIDEO_IMAGE = "video_item_image";
    public static final String VIDEO_LINK = "video_item_link";
    public static final String VIDEO_ID = "video_item_id";
    public static final String IS_FROM_SEARCH = "is_from_search";
    private static int RQ_CODE = 1001;

    protected LinearLayout mDetailContainer;

    protected TextView mVideoTitleView;
    protected TextView mVideoDescriptionView;

    protected PopupWindow window;

    protected OnShareInterface mShareListener;

    public String mVideoTitle;
    public String mVideoDescription;
    public String mVideoPlayUrl;
    public String mVideoWeblink;
    public String mVideoImage;
    public String mVideoId;
    //private BannerAdFragment.AdListener mAdUpdateListener;

    public static Fragment getInstance(String title, String description, String path, String url) {
        Fragment fragment = new VideoDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_TITLE, title);
        bundle.putString(VIDEO_DESCRIPTION, description);
        bundle.putString(VIDEO_PLAY_URL, path);
        bundle.putString(VIDEO_LINK, url);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static Fragment getInstance(Bundle bundle) {
        Fragment fragment = new VideoDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mShareListener = (OnShareInterface) activity;
            ///  mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (ClassCastException ex) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractBundleData();
    }

    public void extractBundleData() {
        Bundle bundle = getArguments();
        mVideoTitle = bundle.getString(VIDEO_TITLE);
        mVideoDescription = bundle.getString(VIDEO_DESCRIPTION);
        mVideoPlayUrl = bundle.getString(VIDEO_PLAY_URL);
        mVideoImage = bundle.getString(VIDEO_IMAGE);
        mVideoWeblink = bundle.getString(VIDEO_LINK);
        mVideoId = bundle.getString(VIDEO_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_detail, container, false);
        initViews(view);
        return view;
    }

    protected void initViews(View view) {
        mVideoTitleView = (TextView) view.findViewById(R.id.video_title);
        mVideoDescriptionView = (TextView) view.findViewById(R.id.video_desciption);
        mDetailContainer = (LinearLayout) view.findViewById(R.id.detail_container);
        //forcing it to portrait mode before video load
        forceVideoToPortraitMode();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleAndDescription();
        // loadBannerAds();
    }

    protected void setTitleAndDescription() {
        if (!TextUtils.isEmpty(mVideoTitle))
            mVideoTitleView.setText(Html.fromHtml(mVideoTitle));

        if (!TextUtils.isEmpty(mVideoDescription))
            mVideoDescriptionView.setText(Html.fromHtml(mVideoDescription));
    }

    @Override
    public void onResume() {
        super.onResume();
        playVideo();
    }

    protected void playVideo() {
        if (!TextUtils.isEmpty(mVideoPlayUrl)) {
            if (isAppConnectedToCastDevice()) {
                playVideoRemote();
            } else {
                addVideoPlayFragment();
            }
        }
    }

    private VideoPlayFragment fragment;

    protected void addVideoPlayFragment() {
        FragmentManager manager = getChildFragmentManager();
        if (manager.findFragmentById(R.id.media_container) == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            fragment = new VideoPlayFragment();
            Bundle bundle = new Bundle(1);
            bundle.putString(VideoPlayFragment.VIDEO_PLAY_URL, mVideoPlayUrl);
            bundle.putInt(VideoPlayFragment.VIDEO_PLAY_CURRENT_POSITION, mPlayPosition);
            bundle.putBoolean(VideoPlayFragment.IS_LIVE_TV,false);
            fragment.setArguments(bundle);
            transaction.replace(R.id.media_container, fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    protected MediaInfo getMediaInfo() {
        return ChromecastUtil.createMediaInfo(mVideoPlayUrl, mVideoTitle, mVideoDescription, mVideoImage, mVideoImage);
    }

    private void forceVideoToPortraitMode() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void setScreenName() {
        setScreenName(TAG + " - " + mVideoId + " - " + mVideoTitle);
    }

    private void hideDatailContainer() {
        mDetailContainer.setVisibility(View.GONE);
    }

    private void showDetailContainer() {
        mDetailContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideDatailContainer();
        } else {
            showDetailContainer();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu_video, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                // createSharePopup();
                if (SplashAdManager.getSplashAdMngrInstance(getActivity()) != null) {
                    SplashAdManager.getSplashAdMngrInstance(getActivity()).signInBtnClicked(true);
                }
                shareUsingIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void createSharePopup() {
        window = UiUtil.createSharePopup(getActivity(), new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShareApp app = (ShareApp) view.getTag(R.id.pop_up_item_title);
                startSocialShare(app);
                window.dismiss();
            }
        }, true);

    }

    public void shareUsingIntent() {
        ShareItem item = new ShareItem();

        String videoTitle = getVideoTitle();
        String videoPath = getVideoPathUrl();
        String videoDescription = getVideoDescription();

        if (!TextUtils.isEmpty(videoPath))
            item.link = Html.fromHtml(Utility.decodeString(videoPath)).toString();
        if (!TextUtils.isEmpty(videoTitle))
            item.title = Html.fromHtml(videoTitle).toString();
        item.itemType = "video";
        item.desc = videoDescription;
        item.itemID = getVideoId();
        item.category = ApplicationConstants.SectionType.VIDEO;

//        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, Utility.getExtraSubject(item, getActivity()));
//        if (!TextUtils.isEmpty(item.title))
//            shareIntent.putExtra(Intent.EXTRA_SUBJECT, item.title);
//        shareIntent.putExtra(Intent.EXTRA_TITLE, item.title);
//        startActivityForResult(shareIntent, RQ_CODE);

        startShareItem(item);
    }

    public void startShareItem(ShareItem item) {

        String shareViaNdtv = getResources().getString(R.string.shared_via_ndtv);

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_TEXT, item.link + "\n\n" + shareViaNdtv);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, item.title);

        PackageManager pm = getActivity().getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");

        Intent openInChooser = Intent.createChooser(emailIntent, "Complete action using");

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;

            if (packageName.contains("twitter") || packageName.contains("whatsapp") || packageName.contains("plus") || packageName.contains("bluetooth") || packageName.contains("talk") || packageName.contains("facebook") || packageName.contains("mms")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if (packageName.contains("twitter")) {
                    intent.putExtra(Intent.EXTRA_TEXT, item.title + ":\n\n" + item.link);
                } else if (packageName.contains("facebook")) {
                    intent.putExtra(Intent.EXTRA_TEXT, item.link);
                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, item.title + ":\n\n" + item.link + "\n\n" + shareViaNdtv);
                }
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivityForResult(openInChooser, RQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == RQ_CODE) {
            Toast.makeText(getActivity(), getResources().getString(R.string.share_success), Toast.LENGTH_LONG).show();
        }
    }

    public void startSocialShare(ShareApp app) {
        if (mShareListener == null)
            return;

        ShareItem item = new ShareItem();

        String videoTitle = getVideoTitle();
        String videoPath = getVideoPathUrl();
        String videoDescription = getVideoDescription();

        item.link = videoPath;
        item.title = Html.fromHtml(videoTitle).toString();
        item.itemType = "video";
        item.desc = Html.fromHtml(videoDescription).toString();
        item.itemID = getVideoId();
        item.category = ApplicationConstants.SectionType.VIDEO;

        if (ApplicationConstants.SocialShare.FACEBOOK_PKG_NAME.equalsIgnoreCase(app.packageName)) {
            mShareListener.onShareOnFacebook(item);
        } else if (ApplicationConstants.SocialShare.GOOGLE_PLUS_PKG_NAME.equalsIgnoreCase(app.packageName)) {
            mShareListener.onShareOnGooglePlus(item);
        } else if (ApplicationConstants.SocialShare.TWITTER_PKG_NAME.equalsIgnoreCase(app.packageName)) {
            item.desc = "";
            mShareListener.onShareOnTwitter(item, app);
        } else {
            mShareListener.onShareOnNormal(item, app);
        }
    }

    @Override
    protected int getSeekPosition() {
//TODO getposition from VideoPlayFragnent
        //      return getCurrentPosition();
        return fragment != null ? fragment.getCurrentPosition() : 0;
    }

    protected VideoCastConsumerImpl getCastConsumer() {
        return new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
                playVideo();
            }
        };
    }

    public String getVideoTitle() {
        return mVideoTitle;
    }

    public String getVideoPathUrl() {
        return mVideoWeblink;
    }

    public String getVideoDescription() {
        return mVideoDescription;
    }

    public String getVideoId() {
        return mVideoId;
    }

    private int mPlayPosition = 0;




   /* public void loadBannerAds() {
        if (mAdUpdateListener != null) {
            mAdUpdateListener.loadBannerAd(mNavigationPosition, mSectionPosition, null, true);
        }
    }*/
}
