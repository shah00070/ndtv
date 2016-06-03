package com.ndtv.core.video.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.ads.utility.AdConstants;
import com.ndtv.core.common.util.FragmentHelper;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.ui.custom.DeeplinkingVideoDetailFragment;
import com.ndtv.core.share.GooglePlusHelper;
import com.ndtv.core.share.ShareApp;
import com.ndtv.core.share.ShareItem;
import com.ndtv.core.ui.AppMainActivitty;
import com.ndtv.core.ui.listener.OnShareInterface;

/**
 * Created by laveen on 18/2/15.
 */
public class VideoPlayActivity extends AppMainActivitty implements OnShareInterface, BannerAdFragment.AdListener {

    private boolean bIsFromSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        if (savedInstanceState == null) {
            addVideoPlayFragment();
        }
        setTitle("");

        mGpHelper = GooglePlusHelper.getInstance(this);
        mGpHelper.setOnSignedInListener(null);

        if (getIntent().getExtras() != null) {
            bIsFromSearch = getIntent().getExtras().getBoolean(VideoDetailFragment.IS_FROM_SEARCH);
        }

        loadAd();
    }

    private void loadAd() {
        boolean isFromDeeplink = false;
        if (getIntent().getExtras() != null) {
            isFromDeeplink = getIntent().getExtras().getBoolean(Constants.FROM_DEEPLINK);
        }
        if(isFromDeeplink || bIsFromSearch){
            loadBannerAd(-1, -1, null, false, -1, false, true);
        }
        else {
            int navigationIndex = getIntent().getExtras().getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, -1);
            if (navigationIndex != -1) {
                int subsectionIndex = getIntent().getExtras().getInt(ApplicationConstants.BundleKeys.SECTION_POS, -1);
                loadBannerAd(navigationIndex, subsectionIndex, null, false, -1, false, true);
            }
        }
    }


    public void addVideoPlayFragment() {
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            Fragment fragment = null;
            Bundle bundle = intent.getExtras();
            if (bundle.getBoolean(Constants.FROM_DEEPLINK)) {
                setTitle("Deeplink Video");
                fragment = new DeeplinkingVideoDetailFragment();
                fragment.setArguments(bundle);
            } else {
                Bundle bundle1 = getIntent().getExtras();

                fragment = VideoDetailFragment.getInstance(getIntent().getExtras());
            }
            FragmentHelper.replaceFragment(this, R.id.play_container, fragment, "video_detail");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //setTitle("Videos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
            findViewById(R.id.adContainer).setVisibility(View.GONE);
        } else {
            getSupportActionBar().show();
            showAd();
        }
    }

    @Override
    public void onShareOnFacebook(ShareItem item) {
        if (mFbHelper != null)
            mFbHelper.shareOnFacebook(item, this);
    }

    @Override
    public void onShareOnGooglePlus(ShareItem item) {
        if (mGpHelper != null)
            mGpHelper.basicShare(this, item);
    }

    @Override
    public void onShareOnTwitter(ShareItem item, ShareApp app) {
        item.link = Utility.getExtraSubjectForTwitter(item, this);
        onShareOnNormal(item, app);
    }

    @Override
    public void onShareOnNormal(ShareItem item, ShareApp app) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(app.packageName, app.activityName));
        intent.setAction(Intent.ACTION_SEND);
        intent.setType(item.contentType);

        if (!TextUtils.isEmpty(item.title))
            intent.putExtra(Intent.EXTRA_SUBJECT, item.title);

        //item.link = Utility.getExtraSubject(item, this);
        // intent.putExtra(Intent.EXTRA_TEXT, item.link);
        intent.putExtra(Intent.EXTRA_TEXT, Utility.getExtraSubject(item, this));

        startActivity(intent);
    }


    @Override
    public void loadBannerAd(final int navigationPos, final int sectionPos, final String contentUrl, final boolean isPhotos, int photoIndex, final boolean isLiveTv, final boolean isVideo) {
        Fragment adFragment = new BannerAdFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(AdConstants.IS_PHOTOS, isPhotos);
        bundle.putBoolean(AdConstants.IS_LIVETV, isLiveTv);
        bundle.putBoolean(AdConstants.IS_VIDEO, isVideo);
        adFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.adContainer, adFragment, AdConstants.AD_FRAGMENT_TAG).commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        ((BannerAdFragment) getSupportFragmentManager().findFragmentByTag(AdConstants.AD_FRAGMENT_TAG)).setAdListener(VideoPlayActivity.this);
        ((BannerAdFragment) getSupportFragmentManager().findFragmentByTag(AdConstants.AD_FRAGMENT_TAG)).loadAd(navigationPos, sectionPos, contentUrl, photoIndex);
        // findViewById(R.id.adContainer).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideIMBannerAd() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(R.id.adContainer) != null) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(manager.findFragmentById(R.id.adContainer));
            findViewById(R.id.adContainer).setVisibility(View.GONE);
        }
    }

    private void showAd() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(R.id.adContainer) != null) {
            findViewById(R.id.adContainer).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showIMBannerAd(boolean mIsPhotos, final boolean isLiveTv, final boolean isVideo) {
        showAd();
    }
}
