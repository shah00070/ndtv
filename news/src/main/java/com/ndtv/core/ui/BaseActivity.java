package com.ndtv.core.ui;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.comscore.analytics.comScore;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.plus.model.people.Person;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.R;
import com.ndtv.core.ads.utility.InterstitialAdHelper;
import com.ndtv.core.common.util.FragmentHelper;
import com.ndtv.core.common.util.GCMDetailWebViewFragment;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.SplashAdManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.util.URLUtility;
import com.ndtv.core.common.util.util.UiUtility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Albums;
import com.ndtv.core.config.model.NewsItems;
import com.ndtv.core.config.model.UserInfo;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.constants.ApplicationConstants.BuildType;
import com.ndtv.core.constants.ApplicationConstants.PreferenceKeys;
import com.ndtv.core.cricket.ui.WebViewFragment;
import com.ndtv.core.gcm.GcmUtility;
import com.ndtv.core.gcm.ServerUtilities;
import com.ndtv.core.newswidget.ui.DeeoLinkingPhotoFragment;
import com.ndtv.core.newswidget.ui.DeeplinkingNewsDetailFragment;
import com.ndtv.core.radio.ui.LiveRadioFragment;
import com.ndtv.core.radio.ui.RadioTabsFragment;
import com.ndtv.core.search.ui.NewsDetailSearchFragment;
import com.ndtv.core.search.ui.SearchFragment;
import com.ndtv.core.settings.ui.SettingsFragment;
import com.ndtv.core.share.FacebookHelper;
import com.ndtv.core.share.GooglePlusHelper;
import com.ndtv.core.share.ShareApp;
import com.ndtv.core.share.ShareItem;
import com.ndtv.core.shows.ui.PrimeShowsFragment;
import com.ndtv.core.ui.widgets.AlbumDetailFragment;
import com.ndtv.core.ui.widgets.DetailFragment;
import com.ndtv.core.util.ApplicationUtils;
import com.ndtv.core.util.BitmapCache;
import com.ndtv.core.util.ConfigUtils;
import com.ndtv.core.util.ConnectivityChangeReceiver;
import com.ndtv.core.util.GAHandler;
import com.ndtv.core.util.LogUtils;
import com.ndtv.core.util.UiUtil;
import com.ndtv.core.video.ui.VideosListingFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Srihari S Reddy on 17/12/14.
 */
public class BaseActivity extends ActionBarActivity implements NavigationDrawerCallbacks, ApplicationConstants.FragmentType, ApplicationConstants.SocialShare, ApplicationConstants.NavigationType, PreferenceKeys, BuildType {

    private static final String TAG = "BaseActivity";
    private static final int RC_SIGN_IN = 0;
    protected Toolbar mActionBarToolbar;
    protected NavigationDrawerFragment mNavigationDrawerFragment;
    private String mAppName;
    private ActionBarDrawerToggle mDrawerToggle;
    protected int mNavigationPos;
    protected int mCurrentSectionPos;
    public Menu menu;
    private int currentFragment = 0;
    protected PopupWindow mSharePopup;
    private boolean isShare = false;
    public boolean isAllowShare = true;
    private FacebookHelper mFbHelper;
    private GooglePlusHelper mGpHelper;
    private UserInfo mUserInfo;
    public Boolean bFromRadioNotifctn = false;
    private int RQ_SHARE = 1001;
    protected PopupWindow window;
    public TextView comentCountTxtVw;
    private SearchView mSearchView;
    public ProgressBar comentProgressBar;
    private BroadcastReceiver br;
    //to dismiss notification, if it's already shared from notification try
    private int mBreakingNewsId;


    private String currentComentCount = ApplicationUtils.DEFAULT_COMMENT;

    private final BroadcastReceiver mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
    private PendingIntent pi;
    private AlarmManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.LOGD(TAG, " I am create called baseactivity");
        super.onCreate(savedInstanceState);

        GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

        /** handling social share */
        mFbHelper = FacebookHelper.getInstance(this);
        mFbHelper.oncreate(savedInstanceState);
        mFbHelper.setOnFBSignedInListener(null);

        mGpHelper = GooglePlusHelper.getInstance(this);
        mGpHelper.setOnSignedInListener(null);
        scheduleRefresh();
    }

    private void scheduleRefresh() {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NdtvApplication.getInstance().setNeedsRefresh(true);
            }
        };
        registerReceiver(br, new IntentFilter("com.july.ndtv.apprefresh"));

        pi = PendingIntent.getBroadcast(this, 0, new Intent("com.july.ndtv.apprefresh"),
                PendingIntent.FLAG_UPDATE_CURRENT);

        am = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /** For facebook, do not remove this */
        if (mFbHelper != null)
            mFbHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //AppReviewHelper.ONLaunch(this);
        //For GCM

        handleTimeZoneForGcm();

        //Connect the PlusClient, if user is already logged in
        if (mGpHelper != null)
            mGpHelper.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof DetailFragment) {
            currentFragment = NEWS_DETAIL_FRAGMENT;
        } else if (fragment instanceof AlbumDetailFragment) {
            currentFragment = PHOTO_DETAIL_FRAGMENT;
        } else if (fragment instanceof CommentsFragment) {
            currentFragment = COMMENT_FRAGMENT;
        } else if (fragment instanceof DeeplinkingNewsDetailFragment) {
            currentFragment = DEEPLINKING_NEWS_DETAIL_FRAGMENT;
        } else if (fragment instanceof DeeoLinkingPhotoFragment) {
            currentFragment = DEEPLINKING_PHOTO_FRAGMENT;
        } else if (fragment instanceof GCMDetailWebViewFragment) {
            currentFragment = GCM_NEWS_DETAIL_FRAGMENT;
        } else if (fragment instanceof PrimeShowsFragment) {
            currentFragment = PRIME_SHOWS_FRAGMENT;
        } else if (fragment instanceof HomeFragment) {
            currentFragment = HOME_FRAGMENT;
        } else if (fragment instanceof NewsDetailSearchFragment) {
            currentFragment = NEWS_DETAIL_SEARCH_FRAGMENT;
        } else if (fragment instanceof SearchFragment) {
            currentFragment = SEARCHING_FRAGMENT;
        } else if (fragment instanceof VideosListingFragment) {
            currentFragment = PRIME_VIDEOS_FRAGMENT;
        } else if (fragment instanceof LiveRadioFragment) {
            currentFragment = LIVE_RADIO_FRAGMENT;
        } else {
            showOverflowMenu(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (currentFragment == COMMENT_FRAGMENT) {
            MenuItem postMenuItem = menu.findItem(R.id.menu_post_comment);
            postMenuItem.setVisible(true);
            MenuItem shareMenuItem = menu.findItem(R.id.menu_share);
            shareMenuItem.setVisible(false);
            MenuItem comentMenuItem = menu.findItem(R.id.menu_comment);
            comentMenuItem.setVisible(false);
            MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
            searchMenuItem.setVisible(false);
            currentFragment = 0;
        } else if (currentFragment == LIVE_RADIO_FRAGMENT) {
            MenuItem postMenuItem = menu.findItem(R.id.menu_post_comment);
            postMenuItem.setVisible(false);
            MenuItem shareMenuItem = menu.findItem(R.id.menu_share);
            shareMenuItem.setVisible(false);
            MenuItem comentMenuItem = menu.findItem(R.id.menu_comment);
            comentMenuItem.setVisible(false);
            MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
            searchMenuItem.setVisible(false);
            currentFragment = 0;
        } else if (currentFragment == PHOTO_DETAIL_FRAGMENT) {
            MenuItem postMenuItem = menu.findItem(R.id.menu_post_comment);
            postMenuItem.setVisible(false);
            MenuItem comentmenuItem = menu.findItem(R.id.menu_comment);
            comentmenuItem.setVisible(false);
            MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
            searchMenuItem.setVisible(false);
            currentFragment = 0;
        } else if (currentFragment == DEEPLINKING_NEWS_DETAIL_FRAGMENT) {
            setTitle("");
            MenuItem shareMenuItem = menu.findItem(R.id.menu_share);
            shareMenuItem.setVisible(true);
            View countView = menu.findItem(R.id.menu_comment).getActionView();
            comentCountTxtVw = (TextView) countView.findViewById(R.id.comment_count_value);
            comentProgressBar = (ProgressBar) countView.findViewById(R.id.progresBarComment);
            loadCommentCount();
            countView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String comentTxt = comentCountTxtVw.getText().toString();
                    if (!comentTxt.equals(ApplicationUtils.DEFAULT_COMMENT))
                        launchComments();
                }
            });
            MenuItem postcomentMenuItem = menu.findItem(R.id.menu_post_comment);
            postcomentMenuItem.setVisible(false);
            MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
            searchMenuItem.setVisible(false);
            currentFragment = 0;
        } else if (currentFragment == DEEPLINKING_PHOTO_FRAGMENT) {
            MenuItem shareMenuItem = menu.findItem(R.id.menu_share);
            shareMenuItem.setVisible(true);
            MenuItem comentMenuItem = menu.findItem(R.id.menu_comment);
            comentMenuItem.setVisible(false);
            MenuItem postcomentMenuItem = menu.findItem(R.id.menu_post_comment);
            postcomentMenuItem.setVisible(false);
            MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
            searchMenuItem.setVisible(false);
            currentFragment = 0;
        } else if (currentFragment == NEWS_DETAIL_FRAGMENT || currentFragment == NEWS_DETAIL_SEARCH_FRAGMENT) {
            MenuItem menuItem = menu.findItem(R.id.menu_post_comment);
            menuItem.setVisible(false);
            MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
            searchMenuItem.setVisible(false);
            View countView = menu.findItem(R.id.menu_comment).getActionView();
            comentCountTxtVw = (TextView) countView.findViewById(R.id.comment_count_value);
            comentProgressBar = (ProgressBar) countView.findViewById(R.id.progresBarComment);
            loadCommentCount();
            countView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String comentTxt = comentCountTxtVw.getText().toString();
                    if (!comentTxt.equals(ApplicationUtils.DEFAULT_COMMENT))
                        launchComments();
                }
            });
            currentFragment = 0;
        } else if (currentFragment == GCM_NEWS_DETAIL_FRAGMENT) {
            MenuItem shareMenuItem = menu.findItem(R.id.menu_share);
            shareMenuItem.setVisible(true);
            MenuItem comentMenuItem = menu.findItem(R.id.menu_comment);
            comentMenuItem.setVisible(true);
            MenuItem podtcomentMenuItem = menu.findItem(R.id.menu_post_comment);
            podtcomentMenuItem.setVisible(false);
            MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
            searchMenuItem.setVisible(false);
            View countView = menu.findItem(R.id.menu_comment).getActionView();
            comentCountTxtVw = (TextView) countView.findViewById(R.id.comment_count_value);
            comentProgressBar = (ProgressBar) countView.findViewById(R.id.progresBarComment);
            loadCommentCount();
            countView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String comentTxt = comentCountTxtVw.getText().toString();
                    if (!comentTxt.equals(ApplicationUtils.DEFAULT_COMMENT))
                        launchComments();
                }
            });
            currentFragment = 0;
        }
        /*
        * This part is used in searching Module
        * */
        else if (currentFragment == HOME_FRAGMENT || currentFragment == SEARCHING_FRAGMENT || currentFragment == PRIME_SHOWS_FRAGMENT || currentFragment == PRIME_VIDEOS_FRAGMENT) {
            if (currentFragment == SEARCHING_FRAGMENT) {
                setTitle("");
            }
            MenuItem shareMenuItem = menu.findItem(R.id.menu_share);
            shareMenuItem.setVisible(false);
            MenuItem comentMenuItem = menu.findItem(R.id.menu_comment);
            comentMenuItem.setVisible(false);
            MenuItem postcomentMenuItem = menu.findItem(R.id.menu_post_comment);
            postcomentMenuItem.setVisible(false);
            MenuItem searchMenuItem = menu.findItem(R.id.menu_item_search);
            searchMenuItem.setVisible(true);
            View searchView = menu.findItem(R.id.menu_item_search).getActionView();
            mSearchView = (SearchView) searchView.findViewById(R.id.searchView);
            int serchIconId = mSearchView.getContext().getResources().getIdentifier("android:id/search_button", null, null);
            ImageView searchIcon = (ImageView) mSearchView.findViewById(serchIconId);
            searchIcon.setImageResource(R.drawable.search);
            initSearchView();
            currentFragment = 0;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void initSearchView() {
        mSearchView.setQueryHint(getString(R.string.search_hint));

        //*** setOnQueryTextFocusChangeListener ***
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        //*** setOnQueryTextListener here we submit the query string ***
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (Utility.isInternetOn(getApplicationContext())) {
                    launchSearchPage(query);
                } else {
                    showFeatureDisabledToast();
                }
                hideKeyBoard();
                if (mNavigationDrawerFragment.isDrawerOpen()) {
                    mNavigationDrawerFragment.closeDrawer();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void hideKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void showOverflowMenu(boolean showMenu) {
        if (menu == null)
            return;
        menu.setGroupVisible(R.id.main_menu_group, showMenu);
    }

    private void showFeatureDisabledToast() {
        Toast.makeText(getApplicationContext(), R.string.feature_disabled_alert, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                if (Utility.isInternetOn(getApplicationContext())) {
                    // if (isAllowShare) {
                    //isAllowShare = false;
                    // createSharePopup();
                    //to avoid splash ads
                    if (SplashAdManager.getSplashAdMngrInstance(this) != null) {
                        SplashAdManager.getSplashAdMngrInstance(this).signInBtnClicked(true);
                    }
                    startSocialShare(null);
                    //  }
                } else {
                    showFeatureDisabledToast();
                }
                break;
            case R.id.menu_post_comment:
                if (Utility.isInternetOn(getApplicationContext())) {
                    if (SplashAdManager.getSplashAdMngrInstance(this) != null) {
                        SplashAdManager.getSplashAdMngrInstance(this).signInBtnClicked(true);
                    }
                    if (isAllowShare) {
                        isAllowShare = false;
                        createCustomLoginPopup();
                    }
                } else {
                    showFeatureDisabledToast();
                }
                break;
            case R.id.menu_item_search:
                if (Utility.isInternetOn(getApplicationContext())) {
                    //launchSearchPage();
                } else {
                    showFeatureDisabledToast();
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        am.cancel(pi);
        if (mBreakingNewsAlertDialogue != null && mBreakingNewsAlertDialogue.isShowing()) {
            //If dialogue is visible, check if saved id and id of the alert dialog is same
            if (mBreakingNewsId == PreferencesManager.getInstance(this).getSavedNotificaionId())
                mBreakingNewsAlertDialogue.dismiss();
        }

        GAHandler.getInstance(getApplicationContext()).SendScreenView("BaseActivity");

        //Splash ads code,

        if (this.getClass() == UiUtility.getCurrentActivity()) {
            if (!UiUtility.isAppRefresh()) {
                Log.d("App refresh", "Not refreshed");
                SplashAdManager splashAdManager = SplashAdManager.getSplashAdMngrInstance(this);

                splashAdManager.increaseLaunchCount(BaseActivity.this);
            } else {
                Log.d("App refresh", "App refreshed");
                UiUtility.setAppREfresh(false);
            }
        }

        UiUtility.setCurrentActivity(this.getClass());
        if (mFbHelper != null) {
            mFbHelper.onResume();
        }

        //For GCM
        if (PreferencesManager.getInstance(getApplicationContext()).getPushStatus() == true) {
            GcmUtility.checkPlayServices(getApplicationContext(), this);
        }

        // TODO
        // Implement register to check for for Breaking News Pop Up
        registerReceiver();
        //comScore.onEnterForeground();

        // Notify comScore about lifecycle usage
        comScore.onEnterForeground();


        if (!ConnectivityChangeReceiver.isOnline(this)) {
            Toast.makeText(this, getString(R.string.no_network_msg), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (mFbHelper != null)
            mFbHelper.onPause();

        //Adding try catch block to fix ComScore IllegalArgumentException
        try {
            unregisterReceiver(mBreakingNewsStatusReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        unregisterReceiver(mConnectivityChangeReceiver);

        // Notify comScore about lifecycle usage
        comScore.onExitForeground();

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarToolbar = getActionBarToolbar();
    }

    public Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, int section, String url, String itemid) {
        Fragment fragment = getFragment(position, section, url, itemid);
        if (fragment != null)
            addContentFragment(fragment);

    }

    /**
     * @param status
     * @return
     */
    private boolean getAdStatus(String status) {
        if ("1".equalsIgnoreCase(status))
            return true;
        return false;
    }

    protected Fragment getFragment(int position, int section, String url, String itemid) {
        String navigationType = ConfigManager.getInstance().getNavigation(position).type;
        Fragment fragment;
        Bundle bundle = new Bundle();
        if (navigationType.equalsIgnoreCase(AUDIO)) {
            fragment = new RadioTabsFragment();
            bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, position);
            bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POS, section);
            bundle.putBoolean(LiveRadioFragment.LiveRadioConstants.FROM_NOTIFICATION, bFromRadioNotifctn);
            fragment.setArguments(bundle);
        } else if (navigationType.equalsIgnoreCase(SETTINGS)) {
            fragment = new SettingsFragment();
            bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, position);
            bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POSITION, section);
            fragment.setArguments(bundle);
        } else if ((navigationType.equalsIgnoreCase(NEWSHOME)) && (url != null)) {
            //fragment = new WebViewFragment();
            fragment = new GCMDetailWebViewFragment();
            bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, position);
            bundle.putString(ApplicationConstants.BundleKeys.URL_STRING, url);
            bundle.putString(ApplicationConstants.BundleKeys.NEWSITEMID, itemid);
            fragment.setArguments(bundle);

        } else if ((navigationType.equalsIgnoreCase(EXT_URL))) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ConfigManager.getInstance().getNavigation(position).url));
            startActivity(browserIntent);
            return null;
        } else if ((navigationType.equalsIgnoreCase(SHOWS))) {
            PreferencesManager.getInstance(this).setCurrentTvShowPos(PreferencesManager.CURRENT_TV_SHOWS, 0);
            fragment = new PrimeShowsFragment();
            bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, position);
            fragment.setArguments(bundle);
            PreferencesManager.getInstance(getApplicationContext()).setIsFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER, true);
        } else {
            fragment = new HomeFragment();
            bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, position);
            bundle.putInt(ApplicationConstants.BundleKeys.SECTION_POS, section);
            fragment.setArguments(bundle);
            PreferencesManager.getInstance(getApplicationContext()).setIsFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER, true);
        }
        return fragment;
    }

    public String formNewsDetailUrl(String category, String objectId) {
        //http://www.ndtv.com/article/view/@category/@id/site=classic/?device=androidv2&showads=no
        String newsDetailsAPI = ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CustomApiType.NEWS_DETAIL_CUSTOM_API);
        String strToReplaceEpisode[] = new String[]{ApplicationConstants.UrlKeys.DEEPLINK_CATEGORY_URL_TAG, ApplicationConstants.UrlKeys.DEEPLINK_URL_TAG_ID};
        String replacementEpisode[] = new String[]{category, objectId};
        newsDetailsAPI = URLUtility.getFinalUrl(strToReplaceEpisode, replacementEpisode, newsDetailsAPI, this);
        return newsDetailsAPI;
    }

    protected void addContentFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.container) == null) {
            FragmentHelper.getInstance().replaceFragment(this, R.id.container, fragment);
        } else {
            FragmentHelper.getInstance().replaceAndAddToBackStack(this, R.id.container, fragment, null);
        }
    }

    public void setTitle(CharSequence charsequence) {
        if (charsequence != null) {
            super.setTitle(charsequence);
            return;
        } else {
            super.setTitle(getString(R.string.app_name));
            return;
        }
    }

    protected void updateGcmToNdtvServer() {
        NdtvApplication.getApplication(getApplicationContext()).getMultiThreadExecutorService().submit(new Runnable() {

            @Override
            public void run() {
                ServerUtilities.register(getApplicationContext(), GcmUtility.getRegistrationId(getApplicationContext()));
            }
        });
    }

    private void handleTimeZoneForGcm() {
        if (!TextUtils.isEmpty(GcmUtility.getRegistrationId(getApplicationContext()))) {
            final String storedTimeZone = PreferencesManager.getInstance(getApplicationContext()).getCurrentTimeZone();
            final String currentTimeZone = Utility.getCurrentTimeZone().getID();
            if (!storedTimeZone.equalsIgnoreCase(currentTimeZone)) {
                updateGcmToNdtvServer();
            }
        }
    }
    // Display POP UP message when new Notification Arrives

    /**
     * *************************************************************************
     */
    // GCMIntentService.generateNotification(getActivity(),
    // "From News Fragment");
    private void registerReceiver() {
        String Buildflavor;
        Buildflavor = BaseActivity.this.getPackageName();
        if (Buildflavor.equalsIgnoreCase(NDTVNEWS)) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BREAKING_NEWS);
            registerReceiver(mBreakingNewsStatusReceiver, intentFilter);
        } else if (Buildflavor.equalsIgnoreCase(NDTVINDIA)) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BREAKING_NEWS_HINDI);
            registerReceiver(mBreakingNewsStatusReceiver, intentFilter);
        } else if (Buildflavor.equalsIgnoreCase(NDTVPRIME)) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BREAKING_NEWS_PRIME);
            registerReceiver(mBreakingNewsStatusReceiver, intentFilter);

        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mConnectivityChangeReceiver, intentFilter);

    }

    private BroadcastReceiver mBreakingNewsStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String message = intent.getExtras().getString(MESSAGE);
            //save the Notification id
            mBreakingNewsId = intent.getExtras().getInt(ApplicationConstants.GCMKeys.GCM_NOTIFICATION_ID);
            if (BREAKING_NEWS.equals(action)) {
                ConfigManager.getInstance().setPushNewsMessageText(message);
                generateNotifications(message);
            }
            if (BREAKING_NEWS_HINDI.equals(action)) {
                ConfigManager.getInstance().setPushNewsMessageText(message);
                generateNotifications(message);
            }
            if (BREAKING_NEWS_PRIME.equals(action)) {

                ConfigManager.getInstance().setPushNewsMessageText(message);
                generateNotifications(message);
            }
        }
    };

    private boolean mIsNewsPopUpShare = false;
    private AlertDialog mBreakingNewsAlertDialogue;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void generateNotifications(String message) {

        if (mBreakingNewsAlertDialogue != null && mBreakingNewsAlertDialogue.isShowing()) {
            mBreakingNewsAlertDialogue.dismiss();
            // mBreakingNewsAlertDialogue = null;
        }

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.breaking_news_push_dialogue, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView notificationTitleTv = (TextView) view.findViewById(R.id.notification_title);
        TextView notificationTextTv = (TextView) view.findViewById(R.id.notification_text);
        notificationTextTv.setMovementMethod(new ScrollingMovementMethod());
        notificationTitleTv.setText(message);
        notificationTextTv.setText(getResources().getString(R.string.breaking_news_appended_text));
        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mBreakingNewsAlertDialogue != null) {
                    mBreakingNewsAlertDialogue.dismiss();
                }
            }
        });


        final Button shareBtn = (Button) view.findViewById(R.id.share_btn);
        shareBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                List<ShareApp> appsList = getResolveInfoList();
//                ListView optionsListView = (ListView) view.findViewById(R.id.share_list_alert);
//                optionsListView.setVisibility(View.VISIBLE);
//                optionsListView.setLayoutAnimation(Utility.getListanimation());
//                ShareAdapterForAlertDialog adapter = new ShareAdapterForAlertDialog(BaseActivity.this, appsList);
//                optionsListView.setAdapter(adapter);
//                setOnItemClickListenerForAlert(optionsListView, appsList);
                launchShareforForAlert();
            }
        });
        builder.setView(view);

        mBreakingNewsAlertDialogue = builder.create();
        mBreakingNewsAlertDialogue.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        mBreakingNewsAlertDialogue.show();

        if (mBreakingNewsAlertDialogue != null) {
            mBreakingNewsAlertDialogue.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    // mBreakingNewsAlertDialogue = null;
                }
            });
        }
    }


    private void setOnItemClickListenerForAlert(ListView listview, final List<ShareApp> appsList) {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                isShare = true;
                ShareItem item = new ShareItem();
                item.title = ConfigManager.getInstance().getPushNewsMessageText();
                item.link = getResources().getString(R.string.shared_via_ndtv);
                launchShareIntent(appsList.get(pos), item);
            }

        });
    }


    @Override
    protected void onDestroy() {
        LogUtils.LOGD(TAG, " I am ondestroy called baseactivity");
        super.onDestroy();

        if (mFbHelper != null) {
            mFbHelper.onDestroy();
            mFbHelper.clear();
        }
        UiUtility.setCurrentActivity(null);
        am.cancel(pi);
        unregisterReceiver(br);

    }

    @Override
    public void onBackPressed() {

        PreferencesManager.getInstance(this).setIsFromNavigationDrawer(PreferencesManager.IS_FROM_DRAWER, true);

        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else {
            Fragment containerFragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (containerFragment != null && containerFragment instanceof DetailFragment) {
                if (((DetailFragment) containerFragment).handleBackPress())
                    return;
            } else if (containerFragment != null && containerFragment instanceof AlbumDetailFragment) {
                PreferencesManager.getInstance(this).setIsBackFromAlbum(PreferencesManager.IS_BACK_FROM_ALBUM, true);
            } else if (containerFragment != null && containerFragment instanceof HomeFragment) {
                Fragment fragment = ((HomeFragment) containerFragment).getCurrentFragment();
                if (null != fragment && fragment instanceof WebViewFragment) {
                    boolean shouldNotCallSuper = ((WebViewFragment) fragment).handleBackPressed();
                    if (shouldNotCallSuper) {
                        return;
                    }
                }

            } else {
                final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.sub_body);

                if (fragment != null && fragment instanceof WebViewFragment) {
                    boolean shouldNotCallSuper = ((WebViewFragment) fragment).handleBackPressed();
                    if (shouldNotCallSuper) {
                        return;
                    }
                }
            }

            invalidateOptionsMenu();
            mNavigationDrawerFragment.popNavigationFromStack();
            super.onBackPressed();
        }
    }

    protected void launchComments() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            Fragment cmntsFragment = new CommentsFragment();
            Bundle args = new Bundle();
            String currNewsIdentifier = null;
            if (fragment instanceof DetailFragment) {
                int currentNewspos = ((DetailFragment) fragment).getCurrentPosition();

                if (currentNewspos >= 0) {
                    String contentUrl = ((DetailFragment) fragment).getContentLink();
                    String title = ((DetailFragment) fragment).getTitle();
                    String newsId = ((DetailFragment) fragment).getNewsItemID();
                    String newsCategory = ((DetailFragment) fragment).getNewsCategory();
                    String identifier = ((DetailFragment) fragment).getIdentifier();

                    if (identifier != null && newsId != null)
                        currNewsIdentifier = identifier;

                    args.putInt("CURRENT_NEWS_POS", currentNewspos);
                    args.putString("CONTENT_URL", contentUrl);
                    args.putString("CONTENT_TITLE", title);
                    args.putString("NEWS_ID", newsId);
                    args.putString("NEWS_CATEGORY", newsCategory);
                    args.putInt("SECTION_POSITION", currentNewspos);
                    args.putString("IDENTIFIER", identifier);
                }
            } else if (fragment instanceof DeeplinkingNewsDetailFragment) {
                NewsItems newsItems = ((DeeplinkingNewsDetailFragment) fragment).getNewsItem();

                if (newsItems != null) {
                    currNewsIdentifier = newsItems.identifier;
                    args.putString("CONTENT_URL", newsItems.link);
                    args.putString("CONTENT_TITLE", newsItems.title);
                    args.putString("NEWS_ID", newsItems.id);
                    args.putString("NEWS_CATEGORY", "news");
                    args.putString("IDENTIFIER", newsItems.identifier);
                }

            } else if (fragment instanceof GCMDetailWebViewFragment) {
                NewsItems newsItems = ((GCMDetailWebViewFragment) fragment).getNewsItem();

                if (newsItems != null) {
                    currNewsIdentifier = newsItems.identifier;
                    args.putString("CONTENT_URL", newsItems.link);
                    args.putString("NEWS_ID", newsItems.id);
                    args.putString("NEWS_CATEGORY", "news");
                    args.putString("IDENTIFIER", newsItems.identifier);
                }
            }

            cmntsFragment.setArguments(args);

            if (currNewsIdentifier != null) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(fragment.getClass().getName());
                fragmentTransaction.replace(R.id.container, cmntsFragment).commit();

                PreferencesManager.getInstance(this).setIsBackFromCommentList(PreferenceKeys.IS_BACK_FROM_COMMENT, true);
                PreferencesManager.getInstance(this).setNewsIdentifierToHandleBackPress(PreferenceKeys.CURRENT_NEWS_IDENTIFIER, currNewsIdentifier);
            }
        }
    }

    private void launchSearchPage(String searchText) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            //if (fragment instanceof SearchFragment) {
            Fragment searchFragment = SearchFragment.newInstance(searchText);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.addToBackStack(fragment.getClass().getName());
            fragmentTransaction.replace(R.id.container, searchFragment).commit();
            PreferencesManager.getInstance(getApplicationContext()).setIsSerachTabs(PreferencesManager.IS_SEARCH_TABS, true);
            /*} else {
                Fragment searchFragment = SearchFragment.newInstance(searchText);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(fragment.getClass().getName());
                fragmentTransaction.replace(R.id.container, searchFragment).commit();*/
            //}
        }
    }

    protected void createSharePopup() {
        window = UiUtil.createSharePopup(this, new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShareApp app = (ShareApp) view.getTag(R.id.pop_up_item_title);
                startSocialShare(app);
                window.dismiss();

            }
        }, true);

        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isAllowShare = true;
            }
        });
    }

    public void createCustomLoginPopup() {
        window = UiUtil.createSharePopup(this, new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShareApp app = (ShareApp) view.getTag(R.id.pop_up_item_title);
                launchAccountLogin(app);
                window.dismiss();
            }
        }, false);

        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isAllowShare = true;
            }
        });
    }

    public void startSocialShare(ShareApp app) {
        isShare = true;
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        ShareItem item = new ShareItem();

        if (fragment instanceof DetailFragment) {

            int currentNewspos = ((DetailFragment) fragment).getCurrentPosition();

            if (currentNewspos >= 0) {
                String contentUrl = ((DetailFragment) fragment).getShareContentLink();
                String title = ((DetailFragment) fragment).getTitle();
                String newsId = ((DetailFragment) fragment).getNewsItemID();
                String newsCategory = ((DetailFragment) fragment).getNewsCategory();

                if (contentUrl != null && Html.fromHtml(contentUrl) != null)
                    item.link = Html.fromHtml(contentUrl).toString();
                if (title != null && Html.fromHtml(title) != null)
                    item.title = Html.fromHtml(title).toString();
                item.itemType = "news";
                item.category = newsCategory;
                item.itemID = newsId;
            }
        } else if (fragment instanceof AlbumDetailFragment) {
            String albumLink = ((AlbumDetailFragment) fragment).getAlbumLinkUrl();
            String albumTitle = ((AlbumDetailFragment) fragment).getAlbumTitle();
            String albumId = ((AlbumDetailFragment) fragment).getAlbumId();

            item.link = albumLink;
            item.title = albumTitle;
            item.itemType = "Photos";
            item.itemID = albumId;
        } else if (fragment instanceof DeeplinkingNewsDetailFragment) {
            NewsItems newsItems = ((DeeplinkingNewsDetailFragment) fragment).getNewsItem();
            if (newsItems != null) {
                item.link = newsItems.link;
                item.title = newsItems.title;
                item.category = newsItems.category;
                item.itemID = newsItems.id;
                item.itemType = "news";
            }
        } else if (fragment instanceof DeeoLinkingPhotoFragment) {
            Albums albumItems = ((DeeoLinkingPhotoFragment) fragment).getAlbums();
            if (albumItems != null) {
                item.link = albumItems.results.link;
                item.title = albumItems.results.title;
                item.itemID = albumItems.results.id;
                item.itemType = "DeepLinking Photo";
            }
        } else if (fragment instanceof GCMDetailWebViewFragment) {
            NewsItems newsItems = ((GCMDetailWebViewFragment) fragment).getNewsItem();
            if (newsItems != null) {
                item.link = newsItems.link;
                item.title = "";
                item.category = newsItems.category;
                item.itemID = newsItems.id;
                item.itemType = "news";
            }
        }

        if (item.link != null && item.title != null && item.itemID != null) {
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("text/plain");
//            intent.putExtra(Intent.EXTRA_TITLE, item.title);
//            intent.putExtra(Intent.EXTRA_TEXT, item.title + ":\n\n" + item.link + "\n\n" + ApplicationConstants.NDTV_SITE);
//            startActivityForResult(intent, RQ_SHARE);

            startShareItem(item, true);
        }
    }

    public void startShareItem(ShareItem item, boolean isAllowFbShare) {
        String shareViaNdtv = getResources().getString(R.string.shared_via_ndtv);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        /*emailIntent.setAction(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        */
        emailIntent.putExtra(Intent.EXTRA_TEXT, item.link + "\n\n" + shareViaNdtv);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, item.title);

        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");

        Intent openInChooser = Intent.createChooser(emailIntent, "Complete action using");

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;

            GAHandler.getInstance(this).SendScreenView("Share-" + item.title);

            if (isAllowFbShare) {
                if (packageName.contains("twitter") || packageName.contains("whatsapp") || packageName.contains("plus") || packageName.contains("bluetooth") || packageName.contains("skype") || packageName.contains("talk") || packageName.contains("facebook") || packageName.contains("mms")) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    if (packageName.contains("twitter")) {
                        intent.putExtra(Intent.EXTRA_TEXT, item.title + ":\n\n" + item.link);
                    } else if (packageName.contains("facebook")) {
                        intent.putExtra(Intent.EXTRA_TEXT, item.link);
                    } else {
                        if (item.link != "")
                            intent.putExtra(Intent.EXTRA_TEXT, item.title + ":\n\n" + item.link + "\n\n" + shareViaNdtv);
                        else
                            intent.putExtra(Intent.EXTRA_TEXT, item.title + ".\n\n" + shareViaNdtv);
                    }
                    intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                }
            } else {
                if (packageName.contains("twitter") || packageName.contains("whatsapp") || packageName.contains("plus") || packageName.contains("bluetooth") || packageName.contains("skype") || packageName.contains("talk") || packageName.contains("mms")) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    if (packageName.contains("twitter")) {
                        if (item.link != "")
                            intent.putExtra(Intent.EXTRA_TEXT, item.title + ":\n\n" + item.link);
                        else
                            intent.putExtra(Intent.EXTRA_TEXT, item.title);
                    } else if (packageName.contains("facebook")) {
                        intent.putExtra(Intent.EXTRA_TEXT, item.link);
                    } else {
                        if (item.link != "")
                            intent.putExtra(Intent.EXTRA_TEXT, item.title + ":\n\n" + item.link + "\n\n" + shareViaNdtv);
                        else
                            intent.putExtra(Intent.EXTRA_TEXT, item.title + ".\n\n" + shareViaNdtv);
                    }
                    intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                }
            }
        }

        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

        startActivityForResult(openInChooser, RQ_SHARE);
    }

    public void launchShareforNotificationhub(String message) {
//        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TITLE, message);
//        shareIntent.putExtra(Intent.EXTRA_TEXT, message + ":\n\n" + ApplicationConstants.NDTV_SITE);
//        startActivity(shareIntent);
        // GAHandler.getInstance(this).SendScreenView("launchShareforNotificationhub");
        ShareItem item = new ShareItem();
        item.title = message;
        item.link = "";
        startShareItem(item, false);
    }

    public void launchShareforForAlert() {
        String sharemsg = ConfigManager.getInstance().getPushNewsMessageText();
//        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TITLE, sharemsg);
//        shareIntent.putExtra(Intent.EXTRA_TEXT, sharemsg + ":\n\n" + ApplicationConstants.NDTV_SITE);
//        startActivity(shareIntent);

        // GAHandler.getInstance(this).SendScreenView("Sharing of Alerts");
        ShareItem item = new ShareItem();
        item.title = sharemsg;
        item.link = "";
        startShareItem(item, false);
    }

    private void launchShareIntent(ShareApp appInfo, ShareItem item) {
        if (null != item) {
            item.contentType = MIME_DATA_TYPE;

            if (GOOGLE_PLUS_PKG_NAME.equalsIgnoreCase(appInfo.packageName)) {
                if (mGpHelper != null)
                    mGpHelper.basicShare(this, item);
            } else if (FACEBOOK_PKG_NAME.equalsIgnoreCase(appInfo.packageName)) {
                if (mFbHelper != null)

                    mFbHelper.shareOnFacebook(item, this);
            } else if (null != appInfo) {
                String extraSubject = "";
                if (TWITTER_PKG_NAME.equalsIgnoreCase(appInfo.packageName) || FB_MSNGR_PKG_NAME.equalsIgnoreCase(appInfo.packageName)) {
                    extraSubject = Utility.getExtraSubjectForTwitter(item, this);
                } else if (TextUtils.isEmpty(item.desc) || HANGOUTS_PKG_NAME.equalsIgnoreCase(appInfo.packageName)) {
                    if (WHATSAPP_PKG_NAME.equalsIgnoreCase(appInfo.packageName))
                        extraSubject = Utility.getExtraSubject(item, this);
                    else
                        extraSubject = Utility.getExtraSubjectNoDesc(item, this);
                } else {
                    extraSubject = Utility.getExtraSubject(item, this);
                }
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(appInfo.packageName, appInfo.activityName));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType(item.contentType);
                if (!TextUtils.isEmpty(item.title)) {
                    intent.putExtra(Intent.EXTRA_SUBJECT, item.title);

                    intent.putExtra(Intent.EXTRA_TEXT, extraSubject);
                }
                GAHandler.getInstance(this).SendScreenView("Share-" + appInfo.packageName);
                startActivityForResult(intent, RQ_SHARE);
            }
        }
    }

    /**
     * @return All applications(installed in a device) that can handle
     * Intent.ACTION_SEND.
     */

    private List<ShareApp> getResolveInfoList() {
        PackageManager packageMngr = getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(MIME_DATA_TYPE);
        // NOTE: Provide some data to help the Intent resolver
        List<ResolveInfo> resolveInfoList = new ArrayList<ResolveInfo>();
        resolveInfoList = packageMngr.queryIntentActivities(intent, 0);
        Collections.sort(resolveInfoList, comparator);

        List<ShareApp> list = new ArrayList<ShareApp>();

        for (ResolveInfo info : resolveInfoList) {
            ShareApp app = new ShareApp();
            app.title = info.loadLabel(packageMngr).toString();
            app.icon = info.loadIcon(getPackageManager());
            app.packageName = info.activityInfo.packageName;
            app.activityName = info.activityInfo.name;
            list.add(app);
        }
        return list;
    }

    Comparator<ResolveInfo> comparator = new Comparator<ResolveInfo>() {

        @Override
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            if (lhs.activityInfo.packageName.equalsIgnoreCase(FACEBOOK_PKG_NAME))
                return -1;
            else if (rhs.activityInfo.packageName.equalsIgnoreCase(FACEBOOK_PKG_NAME))
                return 1;
            else if (lhs.activityInfo.packageName.equalsIgnoreCase(TWITTER_PKG_NAME))
                return -1;
            else if (rhs.activityInfo.packageName.equalsIgnoreCase(TWITTER_PKG_NAME))
                return 1;
            else if (lhs.activityInfo.packageName.equalsIgnoreCase(GOOGLE_PLUS_PKG_NAME))
                return -1;
            else if (rhs.activityInfo.packageName.equalsIgnoreCase(GOOGLE_PLUS_PKG_NAME))
                return 1;
            return 0;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RQ_SHARE) {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentById(R.id.container);
            if (fragment != null && fragment instanceof AlbumDetailFragment) {
                fragment.onActivityResult(requestCode, resultCode, data);
            } else if (fragment != null && fragment instanceof DetailFragment) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
            if (mBreakingNewsAlertDialogue != null && mBreakingNewsAlertDialogue.isShowing()) {
                //If dialogue is visible, and is shared, cancel the notification
                Utility.cancelNotification(mBreakingNewsId, this);
            }
            if (mBreakingNewsAlertDialogue != null) {
                mBreakingNewsAlertDialogue.dismiss();
            }

        } else {
            if (resultCode == RESULT_OK && (requestCode == GP_RESOLVE_ERR_REQUEST_CODE || requestCode == RC_SIGN_IN)) {
                if (mGpHelper != null)
                    mGpHelper.onActivityResult(requestCode, resultCode, data);
            }

            if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
                if (mFbHelper != null)
                    mFbHelper.onActivityResult(requestCode, resultCode, data);
            }

            if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentById(R.id.container);
                if (fragment != null && fragment instanceof CommentsFragment) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
            //File upload related
            if (requestCode == NewsDetailFragment.FILE_CHOOSER_RESULT_CODE && (resultCode == RESULT_OK || resultCode == RESULT_CANCELED)) {
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentById(R.id.container);
                if (fragment != null && fragment instanceof DetailFragment) {
                    Fragment currentFragment = ((DetailFragment) fragment).getCurrentFragment();
                    if (currentFragment instanceof NewsDetailFragment)
                        currentFragment.onActivityResult(requestCode, resultCode, data);
                }
            }

        }
    }

    public void launchAccountLogin(ShareApp appInfo) {
        mGpHelper.setOnSignedInListener(mGplusSignedinListener);
        // mFbHelper.setOnFBSignedInListener(mFbSignedinListener);
        if (GOOGLE_PLUS_PKG_NAME.equalsIgnoreCase(appInfo.packageName)) {
            if (mGpHelper != null)
                mGpHelper.getUserId(this);
        } else if (FACEBOOK_PKG_NAME.equalsIgnoreCase(appInfo.packageName)) {

            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof CommentsFragment) {
                ((CommentsFragment) getSupportFragmentManager().findFragmentById(R.id.container))
                        .onLaunchFacebookAccountLogin(appInfo);
            }

        } else if (TWITTER_PKG_NAME.equalsIgnoreCase(appInfo.packageName)) {

            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof CommentsFragment) {
                ((CommentsFragment) getSupportFragmentManager().findFragmentById(R.id.container))
                        .onLaunchTwitterAccountLogin(appInfo);
            }
        }
    }


    private GooglePlusHelper.GooglePlusListeners mGplusSignedinListener = new GooglePlusHelper.GooglePlusListeners() {

        @Override
        public void onSignedOut() {
        }

        @Override
        public void onSignedIn(Person person) {
            mUserInfo = new UserInfo();
            if (null != person) {
                mUserInfo.uid = person.getId();
                mUserInfo.first_name = person.getDisplayName();
                mUserInfo.last_name = "";
                mUserInfo.profile_image = person.getImage().getUrl();
                mUserInfo.site_name = "googleplus";

            }
            mGpHelper.getGplusToken();
        }

        @Override
        public void OnTokenAccessed(String token) {
            mUserInfo.access_token = token;
            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof CommentsFragment) {
                ((CommentsFragment) getSupportFragmentManager().findFragmentById(R.id.container)).postComment(mUserInfo);
            }
            //To avoid splash ads
            updateCommentToken();
            mGpHelper.setOnSignedInListener(null);
        }
    };


    private void updateCommentToken() {
        if (SplashAdManager.getSplashAdMngrInstance(this) != null) {
            SplashAdManager.getSplashAdMngrInstance(this).signInBtnClicked(false);
        }
    }

    @Override
    protected void onStop() {
        LogUtils.LOGD(TAG, " I am onstop called baseactivity");
        super.onStop();
        UiUtility.activityStopped();
        BitmapCache.getInstance((this).getSupportFragmentManager()).clearCache();
        //disconnect the plus client
        if (mGpHelper != null)
            mGpHelper.onStop();
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                1200000, pi);
    }

    public void markNavigationItemSelected(int index) {
        mNavigationDrawerFragment.markNavigationItemSelected(index);
    }

    public void loadCommentCount() {
        if (comentProgressBar != null && comentCountTxtVw != null) {
            // comentProgressBar.setVisibility(View.VISIBLE);
            //comentCountTxtVw.setVisibility(View.GONE);
            if (!currentComentCount.equals(ApplicationUtils.DEFAULT_COMMENT)) {
                comentCountTxtVw.setText(currentComentCount);
            } else
                comentCountTxtVw.setText(ApplicationUtils.DEFAULT_COMMENT);
        }
    }

    public void setCommentCount(String count) {
        //comentProgressBar.setVisibility(View.GONE);
        //comentCountTxtVw.setVisibility(View.VISIBLE);
        if (comentCountTxtVw != null) {
            currentComentCount = count;
            comentCountTxtVw.setText(currentComentCount);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtils.LOGD(TAG, " I am onrestart called baseactivity");
        if ((NdtvApplication.getInstance().needsRefresh()) && (ApplicationUtils.isAppRefreshEnabled(this))) {
            //Added this to avoid launch count increment, when app is refreshed
            UiUtility.setAppREfresh(true);
            //Making interstitial previous ads instance null on background refresh
            InterstitialAdHelper.getInstance().stopInterstitialAd();

            ConfigUtils.refreshAppConfig(BaseActivity.this);

            new Handler().post((new Runnable() {

                @Override
                public void run() {
                    NdtvApplication.getInstance().setNeedsRefresh(false);
                    Intent intent = new Intent(BaseActivity.this, SplashActivity.class);
                    intent.putExtra(ApplicationConstants.BundleKeys.APP_REFRESH, true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    overridePendingTransition(0, 0);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                }
            }));
        }

    }

}