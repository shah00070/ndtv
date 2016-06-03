package com.ndtv.core.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.ads.utility.AdConstants;
import com.ndtv.core.ads.utility.AdUtils;
import com.ndtv.core.ads.utility.InterstitialAdHelper;
import com.ndtv.core.common.util.FragmentHelper;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.util.URLUtility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Albums;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.deeplinking.io.OnDeepLinkingInterface;
import com.ndtv.core.deeplinking.ui.DeeplinkingActivity;
import com.ndtv.core.gcm.GcmUtility;
import com.ndtv.core.livetv.ui.LiveTVPlayActivity;
import com.ndtv.core.livetv.ui.LiveTvPlayFragment;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.newswidget.ui.DeeoLinkingPhotoFragment;
import com.ndtv.core.newswidget.ui.DeeplinkingNewsDetailFragment;
import com.ndtv.core.newswidget.ui.NewsWidgetFragment;
import com.ndtv.core.now.NowClient;
import com.ndtv.core.provider.ContentProviderUtils;
import com.ndtv.core.radio.ui.LiveRadioFragment;
import com.ndtv.core.settings.ui.AccountsFragment;
import com.ndtv.core.settings.ui.SettingsFragment;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.ui.widgets.BaseFragment.GCMListener;
import com.ndtv.core.ui.widgets.BaseFragment.ListItemClkListener;
import com.ndtv.core.ui.widgets.BaseFragment.OnPaginationListener;
import com.ndtv.core.video.ui.VideoPlayActivity;
import com.ndtv.core.video.ui.VideosListingFragment;

public class HomeActivity extends BaseActivity implements ApplicationConstants.NewsDigestConstants,
        ApplicationConstants.BundleKeys, BannerAdFragment.AdListener, NewsDetailFragment.OnInLineLinkClickListener, GCMListener, BaseFragment.OnAddDetailFragmentListener,
        NewsWidgetFragment.OnClickOfNewsWidget, ListItemClkListener, OnPaginationListener, OnDeepLinkingInterface, NewsListingFragment.SectionNewsClickListener {

    private AlertDialog mNewsIn60AlertDialog;
    private boolean mIsActivityRunning;
    private LinearLayout mAdContainer;
    public static final int DEFAULT_ITEM_POS = 9999;
    private static final int RESULT_CANCELLED = 1;

    private String intentNotification;
    private DrawerLayout mDrawer;
    private ProgressBar mProgBar;
    private boolean bIsRestarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }
        setContentView(R.layout.activity_home);
        mAdContainer = (LinearLayout) findViewById(R.id.adContainer);
        //addNavgationFragment(); //Existing Flow

        if (isFromFacebookNewsFeed(getIntent())) {
            launchDeeplinkingActivity(getIntent());
            finish();
            return;
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (!TextUtils.isEmpty(extras.getString(ApplicationConstants.BundleKeys.DEFAULT_NOTI_NAV))) {
                intentNotification = extras.getString(ApplicationConstants.BundleKeys.DEFAULT_NOTI_NAV);
                addNavgationFragmentFromGCM(intentNotification);
            } else if (extras.getBoolean(LiveRadioFragment.LiveRadioConstants.LIVE_RADIO_PUSH, false))

                addNavigationFragmentFromRadio(extras.getBoolean(LiveRadioFragment.LiveRadioConstants.LIVE_RADIO_PUSH, false));


        } else
            addNavgationFragment();

        registerToGCMServer();
    }

    void initNowCards(String regID) {

        NowClient nowClient = NowClient.getInstance(this);
        nowClient.init(regID);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AdUtils.isLaunchInterstitialEnabled()) {
            Log.d("SPLASH", "add enabled");
            if (!bIsRestarted) {
                InterstitialAdHelper.getInstance().showInterstitialAd();
                bIsRestarted = false;
            }
        } else {
            Log.d("SPLASH", "add not enabled");
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        bIsRestarted = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK || resultCode == RESULT_CANCELLED) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (null != fragment && fragment instanceof SettingsFragment) {
                fragment = ((SettingsFragment) fragment).getCurrentFragment();
                if (null != fragment && fragment instanceof AccountsFragment) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    private void addNavgationFragment() {
        mNavigationDrawerFragment = new NavigationDrawerFragment();
        Bundle bundle = new Bundle(1);
        try {
            bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, getDefaultNavPos());
            bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POS, getDefaultSecPos());
        } catch (Exception ex) {
            Crashlytics.log("Caught" + ex.getMessage());
        }
        setNavigationDrawer(bundle);

    }

    private void setNavigationDrawer(Bundle bundle) {
        mNavigationDrawerFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.navigation_container, mNavigationDrawerFragment)
                .commit();
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mNavigationDrawerFragment.setup(findViewById(R.id.navigation_container), getActionBarToolbar());
    }

    public void addNavgationFragmentFromGCM(String nav) {

        mNavigationDrawerFragment = new NavigationDrawerFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, getNavigationIndexFromGCM(nav));
        bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POS, getSectionIndexFromGCM(nav));
        String[] urlData = nav.split("/");
        if (urlData.length > 2 && nav.contains("/")) {
            bundle.putString(ApplicationConstants.BundleKeys.URL_STRING, getNewsDeatilFromGCM(nav));
            bundle.putString(ApplicationConstants.BundleKeys.NEWSITEMID, getNewsItemIDFromGCM(nav));
        } else {
            bundle.putString(ApplicationConstants.BundleKeys.URL_STRING, null);
            bundle.putString(ApplicationConstants.BundleKeys.NEWSITEMID, null);
        }
        setNavigationDrawer(bundle);
    }

    public void lockDrawer() {
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void unLockDrawer() {
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void addNavigationFragmentFromRadio(Boolean nav) {
        bFromRadioNotifctn = nav;
        mNavigationDrawerFragment = new NavigationDrawerFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, getNavigationIndexFromRadio(nav));
        bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POS, getDefaultSecPos());
        setNavigationDrawer(bundle);
    }

    private int getNavigationIndexFromRadio(Boolean nav) {

        return ConfigManager.getInstance().getNavigationPosition(this, ApplicationConstants.NavigationType.AUDIO);

    }

    public int getDefaultNavPos() {
        String defStr = ConfigManager.getInstance().getDefaultNav();
        String[] defNav = defStr.split("/", 2);
        //TODO exception check and give default value
        return ConfigManager.getInstance().getConfiguration().getNavIndex(defNav[0]);

        // return 4;
    }

    public int getDefaultSecPos() {
        String defStr = ConfigManager.getInstance().getDefaultNav();
        if (!defStr.contains("/")) return 0;
        String[] defNav = defStr.split("/", 2);
        if (ConfigManager.getInstance().getConfiguration() != null)
            return ConfigManager.getInstance().getConfiguration().getSec(defNav[0], defNav[1]);
        else
            return 0;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (null != fragment && fragment instanceof HomeFragment) {
                fragment = ((HomeFragment) fragment).getCurrentFragment();
                if (null != fragment && fragment instanceof LiveRadioFragment) {
                    ((LiveRadioFragment) fragment).onKeyUp();
                    return true;
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (null != fragment && fragment instanceof HomeFragment) {
                fragment = ((HomeFragment) fragment).getCurrentFragment();
                if (null != fragment && fragment instanceof LiveRadioFragment) {
                    ((LiveRadioFragment) fragment).onKeyDown();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void loadBannerAd(final int navigationPos, final int sectionPos, final String contentUrl, final boolean isPhotos, final int photoIndex, final boolean isLiveTv, final boolean isVideo) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mAdContainer != null)
                    mAdContainer.setVisibility(View.GONE);

                if (!isFinishing()) {
                    Fragment adFragment = new BannerAdFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(AdConstants.IS_PHOTOS, isPhotos);
                    bundle.putBoolean(AdConstants.IS_LIVETV, isLiveTv);
                    bundle.putBoolean(AdConstants.IS_VIDEO, isVideo);
                    adFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.adContainer, adFragment, AdConstants.AD_FRAGMENT_TAG).commitAllowingStateLoss();
                    getSupportFragmentManager().executePendingTransactions();

                    ((BannerAdFragment) getSupportFragmentManager().findFragmentByTag(AdConstants.AD_FRAGMENT_TAG)).setAdListener(HomeActivity.this);
                    ((BannerAdFragment) getSupportFragmentManager().findFragmentByTag(AdConstants.AD_FRAGMENT_TAG)).loadAd(navigationPos, sectionPos, contentUrl, photoIndex);
                }
            }
        });

    }

    @Override
    public void hideIMBannerAd() {
        if (mAdContainer != null)
            mAdContainer.setVisibility(View.GONE);
    }

    @Override
    public void showIMBannerAd(boolean mIsPhotos, boolean mIsLiveTv, boolean mIsVideo) {

        if (mAdContainer != null) {
            if(getCurrentFragment() instanceof SettingsFragment)
                mAdContainer.setVisibility(View.GONE);
            else {
                if (mIsPhotos) // Changed background of ad container to Black for Photos section.
                    mAdContainer.setBackgroundColor(getResources().getColor(R.color.theme_navigation_color));
                else if (mIsLiveTv)
                    mAdContainer.setBackgroundColor(getResources().getColor(R.color.live_tv_schedule_background));
                else if (mIsVideo)
                    mAdContainer.setBackgroundColor(getResources().getColor(R.color.video_list_background));
                else
                    mAdContainer.setBackgroundColor(getResources().getColor(R.color.white));
                mAdContainer.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mIsActivityRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActivityRunning = true;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isFromFacebookNewsFeed(intent)) {
            launchDeeplinkingActivity(intent);
        }
        if (intent != null) {
            if (!TextUtils.isEmpty(intent.getStringExtra(ApplicationConstants.BundleKeys.DEFAULT_NOTI_NAV))) {
                intentNotification = intent.getStringExtra(ApplicationConstants.BundleKeys.DEFAULT_NOTI_NAV);
                addNavgationFragmentFromGCM(intentNotification);
            } else if (intent.getBooleanExtra(LiveRadioFragment.LiveRadioConstants.LIVE_RADIO_PUSH, false))
                addNavigationFragmentFromRadio(intent.getBooleanExtra(LiveRadioFragment.LiveRadioConstants.LIVE_RADIO_PUSH, false));

        }
    }


    private void registerToGCMServer() {

        if (PreferencesManager.getInstance(getApplicationContext()).getPushStatus() == false) {
            /**
             * User does not want push notifications hence disabling the push,
             * so no need to register, just return from this function
             */
            return;
        }
        if (GcmUtility.checkPlayServices(getApplicationContext(), this) && TextUtils.isEmpty(GcmUtility.getRegistrationId(getApplicationContext()))) {
            GcmUtility.registerInBackground(getApplicationContext());
        } else if (!TextUtils.isEmpty(GcmUtility.getRegistrationId(getApplicationContext()))) {
            String storedTimeZone = PreferencesManager.getInstance(getApplicationContext()).getCurrentTimeZone();
            String currentTimeZone = Utility.getCurrentTimeZone().getID();
            if (!storedTimeZone.equalsIgnoreCase(currentTimeZone)) {
                registerGCM();
            }
            initNowCards(GcmUtility.getRegistrationId(getApplicationContext()));
        }

    }


    @Override
    public void registerGCM() {
        // registerToGCMServer();
        updateGcmToNdtvServer();

    }

    /*
          * (non-Javadoc)
          *
          * @see com.july.ndtv.common.ui.BaseFragment.GCMListener#unregisterGCM()
          */
    @Override
    public void unregisterGCM() {
        // TODO Auto-generated method stub

    }

    public void lauchDeepLinking(String type) {

    }

    @Override
    public void onNotificationHubShareClick(String message) {
        launchShareforNotificationhub(message);
    }


    @Override
    public void onPaginationStarted() {
        // TODO Auto-generated method stub
    }


    @Override
    public void onpPaginationCompleted() {
        // TODO Auto-generated method stub

    }

    //Launching Based on Implementation
    private String getNewsDeatilFromGCM(String nav) {
        String sectionTitle = null;
        String newsStoryID = null;

        String[] urlData = nav.split("/");
        sectionTitle = urlData[1];
        newsStoryID = urlData[2];
        String url = formNewsDetailUrl(sectionTitle, newsStoryID);
        return url;
    }

    private String getNewsItemIDFromGCM(String nav) {
        String newsStoryID = null;
        String[] urlData = nav.split("/");
        newsStoryID = urlData[2];
        return newsStoryID;
    }

    @Override
    public void onAddDetailFragment(Fragment fragment, String tag) {
        FragmentHelper.replaceAndAddToBackStack(this, R.id.container, fragment, tag);
        mNavigationDrawerFragment.addNavigationToStack(-1);
    }

    //   @Override
    //   public void onBackPressed() {
//        super.onBackPressed();
//        if (mNavigationDrawerFragment.isDrawerOpen())
//            mNavigationDrawerFragment.closeDrawer();
//        else {
//            mNavigationDrawerFragment.popNavigationFromStack();
//            super.onBackPressed();
//        }
    // }


    String mPreTitle;

    @Override
    public void onClickOfNewsWidget(String url) {
        /*
        * to reset the item pos and state for photos
        * */

        NewsWidgetManager.getInstance().setMIsFromNewsWidget(false);
        NewsWidgetManager.getInstance().setmFullPhotoClickPosition(0);
        PreferencesManager.getInstance(this).setIsFromDeepLinking(true);
        setTitle("");

        String type = NewsWidgetManager.getDeeplinkCategory(url);

        if (type == null) return;

        if (type.equalsIgnoreCase(Constants.WIDGET_TYPE_NEWS)) {
            launchDeeplinkNews(url);
        } else if (type.equals(Constants.WIDGET_TYPE_VIDEO)) {
            launchDeeplinkningVideo(url);
        } else if (type.equalsIgnoreCase(Constants.WIDGET_TYPE_PHOTO)) {
            launchDeeplinkningPhoto(url);
            NewsWidgetManager.getInstance().setMIsFromNewsWidget(true);
        } else if (type.equalsIgnoreCase(Constants.WIDGET_TYPE_LIVETV)) {
            launchDeepLinkingLiveTV(url);
        } else {
            launchDeepLinkingListingPage(url);
            mNavigationDrawerFragment.addNavigationToStack(-1);
        }

    }

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();

        if (PreferencesManager.getInstance(this).isFromDeepLinking() && !mNavigationDrawerFragment.isStackEmpty() && mNavigationDrawerFragment.getLastElementIndex() == -1) {
            PreferencesManager.getInstance(this).setIsFromDeepLinking(false);
            setTitle("");
        }

        if (mNavigationDrawerFragment.stackCount() == 1) {
            setTitle(mPreTitle);
        }
    }*/

    @Override
    public void onHandleDeepLink(String url) {
        final String type = NewsWidgetManager.getDeeplinkCategory(url);
        if (type == null)
            return;
        //  ndtv://category=news&subcategory=Top Stories
        final String id = NewsWidgetManager.getDeeplinkingId(url);
        if (type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_NEWS) && (!TextUtils.isEmpty(id))) {
            launchDeeplinkNews(url);
        } else if ((type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_VIDEO) || type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_VIDEOS)) && (!TextUtils.isEmpty(id))) {
            launchDeeplinkningVideo(url);
        } else if (type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_LIVETV)) {
            launchDeepLinkingLiveTV(url);
        } else if ((type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_PHOTO) || type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_PHOTOS)) && (!TextUtils.isEmpty(id))) {
            launchDeeplinkningPhoto(url);
        } else {
            launchDeepLinkingListingPage(url);
        }
    }

    private void launchDeepLinkingLiveTV(String url) {
        String navigationTitle = ConfigManager.getDeeplinkCategory(url);
        String sectionTitle = ConfigManager.getDeeplinkSubcategory(url);
        String liveTVURL = ConfigManager.getInstance().getSectionURL(navigationTitle, sectionTitle);

        Intent intent = new Intent(this, LiveTVPlayActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LiveTvPlayFragment.LIVETV_URL, liveTVURL);
        bundle.putBoolean(Constants.FROM_DEEPLINK, true);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void launchDeeplinkningPhoto(final String url) {
        final DeeoLinkingPhotoFragment deeoLinkingPhotoFragment = new DeeoLinkingPhotoFragment();

        NewsWidgetManager.getInstance().downloadAlbum(HomeActivity.this, generateDeeplinkUrl(url), new Response.Listener<Albums>() {
            @Override
            public void onResponse(Albums albums) {
                if (albums != null)
                    deeoLinkingPhotoFragment.setAlbums(albums);
                addDeeplinkFragment(deeoLinkingPhotoFragment, url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(HomeActivity.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateDeeplinkUrl(String appLink) {
        String photoDetailApi = ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CustomApiType.PHOTO_DETAIL_API);

        String albumId = ConfigManager.getDeeplinkingId(appLink);
        String argKeys[] = {ApplicationConstants.UrlKeys.DEEPLINK_PHOTO_ALBUM_ID},
                albumValues[] = {albumId};

        String photoAlbumUrl = URLUtility.getFinalUrl(argKeys, albumValues, photoDetailApi, HomeActivity.this);
        return photoAlbumUrl;
    }

    private void launchDeeplinkningVideo(String url) {

        Intent intent = new Intent(this, VideoPlayActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.FROM_DEEPLINK, true);
        bundle.putString(Constants.DEEP_LINK_URL, url);
        intent.putExtras(bundle);
        startActivity(intent);
        /*Fragment fragment = new DeeplinkingVideoDetailFragment();
        addDeeplinkFragment(fragment, url);*/
    }

    private void launchDeeplinkNews(String url) {
        Fragment newsDetail = new DeeplinkingNewsDetailFragment();
        addDeeplinkFragment(newsDetail, url);
    }

    private void addDeeplinkFragment(Fragment newsDetail, String url) {
        final Bundle bundle;
        bundle = new Bundle();
        bundle.putString(Constants.DEEP_LINK_URL, url);
        mNavigationDrawerFragment.addNavigationToStack(-1);//Sri279
        newsDetail.setArguments(bundle);
        replaceFragmentInContainer(newsDetail);
    }

    private void replaceFragmentInContainer(Fragment newsDetail) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.container) == null) {
            FragmentHelper.getInstance().replaceFragment(this, R.id.container, newsDetail);
        } else {
            FragmentHelper.getInstance().replaceAndAddToBackStack(this, R.id.container, newsDetail, null);
        }
    }

    public void launchDeepLinkingListingPage(String url) {
        String categoryName = NewsWidgetManager.getInstance().getDeeplinkCategory(url);
        String subsectionName = NewsWidgetManager.getInstance().getDeeplinkSubcategory(url);

        if (categoryName.equalsIgnoreCase("Election Results")) {
            categoryName = "Elections";
        }

        if (!TextUtils.isEmpty(categoryName)) {
            ConfigManager manager = ConfigManager.getInstance();
            final int navigationIndex = manager.getNavigationIndexBasedOntitle(categoryName);
            if (navigationIndex >= 0) {
                final int subsectionIndex = manager.getSectionPositionBasedOntitle(this, subsectionName, navigationIndex);
                //mDrawerList.setItemChecked(navigationIndex, true);
                launchPager(navigationIndex, subsectionIndex, HomeActivity.DEFAULT_ITEM_POS);
            }
        }
    }

    protected void launchPager(final int position, int sectionPosition, int itemPosition) {
        Fragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, position);
        bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POS, sectionPosition);
        fragment.setArguments(bundle);
        addContentFragment(fragment);
    }

    @Override
    protected void onDestroy() {
        //TODO Clearing 10 day old records
        ContentProviderUtils.CleanDB(this, 10);
        super.onDestroy();
    }

    private int getSectionIndexFromGCM(String nav) {

        String sectionTitle = null;
        String navTitle = null;
        int mSectionPos;

        if (!nav.contains("/")) return 0;

        String[] urlData = nav.split("/");
        // Corner case like "Notification Hub/";
        if (nav.contains("/") && urlData.length > 1) {
            navTitle = urlData[0];
            sectionTitle = urlData[1];
            if (null != ConfigManager.getInstance().getConfiguration()) {
                mSectionPos = ConfigManager.getInstance().getConfiguration().getSec(navTitle, sectionTitle);
            } else {
                mSectionPos = 0;
            }
        } else {
            mSectionPos = 0;
        }

        return mSectionPos;
    }

    private int getNavigationIndexFromGCM(String nav) {
        String navTitle = null;
        String[] urlData = nav.split("/");
        if (nav.contains("/")) {
            navTitle = urlData[0];

        } else {
            navTitle = nav;
        }
        if (navTitle != null && null != ConfigManager.getInstance().getConfiguration()) {
            mNavigationPos = ConfigManager.getInstance().getConfiguration().getNavIndex(navTitle);
        } else {
            mNavigationPos = 0;
        }
        return mNavigationPos;

    }

    private boolean isFromFacebookNewsFeed(Intent intent) {
        Uri uri = intent.getData();
        String action = intent.getAction();
        if (uri != null && action != null && action.equalsIgnoreCase(DeeplinkingActivity.FACEBOOK_ACTION)) {
            return true;
        }
        return false;
    }

    private void launchDeeplinkingActivity(Intent intent) {
        intent.setClass(this, DeeplinkingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClickOfSectionNews(String url, String newsItemType) {
        String type = NewsWidgetManager.getDeeplinkCategory(url);

        if (type == null) return;

        if (type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_NEWS)) {
            launchDeeplinkNews(url);
        } else if (type.equals(OnDeepLinkingInterface.DEEP_LINK_VIDEO) || type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_VIDEOS)) {
            launchDeeplinkningVideo(url);
        } else if (type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_PHOTO) || type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_PHOTOS)) {
            launchDeeplinkningPhoto(url);
            NewsWidgetManager.getInstance().setMIsFromNewsWidget(true);
        } else if (type.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_LIVETV) || newsItemType.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_LIVETV) || newsItemType.equalsIgnoreCase(OnDeepLinkingInterface.DEEP_LINK_LIVETV_TYPE)) {
            launchDeepLinkingLiveTV(url);
        } else {
            launchDeepLinkingListingPage(url);
            mNavigationDrawerFragment.addNavigationToStack(-1);
        }
    }

    @Override
    public void onPrimeShowsItemClicked(String link, String title, int navigationPos) {
        final Fragment fragment = VideosListingFragment.getInstance(link, title, navigationPos, 0);
        Bundle bundle = new Bundle();
        bundle.putString(VideosListingFragment.VIDEOS_LISTING_URL, link);
        bundle.putInt(NAVIGATION_POS, navigationPos);
        bundle.putString(VideosListingFragment.VIDEOS_LISTING_SECTION, "");
        bundle.putString(NAVIGATION_TITLE, title);
        bundle.putInt(SECTION_POS, 0);
        fragment.setArguments(bundle);
        FragmentHelper.replaceAndAddToBackStack(HomeActivity.this, R.id.container, fragment, "Primeshows");
    }

    private Fragment getCurrentFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.container);
    }
}
