package com.ndtv.core.livetv.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.ndtv.core.NdtvApplication;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.ads.utility.AdConstants;
import com.ndtv.core.common.util.util.UiUtility;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.ui.AppMainActivitty;

/**
 * Created by laveen on 10/2/15.
 */
public class LiveTVPlayActivity extends AppMainActivitty implements BannerAdFragment.AdListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livetv);
        if (savedInstanceState == null)
            addLiveTVPlayFragment();
        setTitle("");
        showAd();
    }

    private void showAd() {
        boolean isFromDeeplink = false;
        if (getIntent().getExtras() != null) {
            isFromDeeplink = getIntent().getExtras().getBoolean(Constants.FROM_DEEPLINK);
        }
        if(isFromDeeplink){
            loadBannerAd(-1, -1, null, false, -1, true, false);
        }
        else {
            Bundle bundle = getIntent().getExtras();
            int navigationIndex = bundle.getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, -1);
            if (navigationIndex != -1) {
                loadBannerAd(navigationIndex, bundle.getInt(ApplicationConstants.BundleKeys.SECTION_POS, 0), null, false, -1, true, false);
            }
        }
    }


    private void addLiveTVPlayFragment() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String liveTvName = bundle.getString(LiveTvPlayFragment.LIVETV_NAME);
            String playUrl = bundle.getString(LiveTvPlayFragment.LIVETV_URL);
            String showName = bundle.getString(LiveTvPlayFragment.LIVETV_SHOW_NAME);
            String showUrl = bundle.getString(LiveTvPlayFragment.LIVETV_SHOW_IMAGE_URL);

            if (!TextUtils.isEmpty(playUrl)) {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fullscreen_content, LiveTvPlayFragment.getInstance(liveTvName, playUrl, showName, showUrl), "lievtvplay");
                transaction.commit();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.adContainer).setVisibility(View.GONE);
        } else {
            showAdContainer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        UiUtility.activityStopped();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UiUtility.setCurrentActivity(null);
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
        ((BannerAdFragment) getSupportFragmentManager().findFragmentByTag(AdConstants.AD_FRAGMENT_TAG)).setAdListener(LiveTVPlayActivity.this);
        ((BannerAdFragment) getSupportFragmentManager().findFragmentByTag(AdConstants.AD_FRAGMENT_TAG)).loadAd(navigationPos, sectionPos, contentUrl, photoIndex);
        findViewById(R.id.adContainer).setVisibility(View.VISIBLE);

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

    private void showAdContainer() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(R.id.adContainer) != null) {
            findViewById(R.id.adContainer).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showIMBannerAd(boolean mIsPhotos, final boolean isLiveTv, final boolean isVideo) {

    }
}
