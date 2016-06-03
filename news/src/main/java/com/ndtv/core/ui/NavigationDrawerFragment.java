package com.ndtv.core.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.ndtv.core.R;
import com.ndtv.core.common.util.FragmentHelper;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.radio.ui.LiveRadioFragment;

import java.util.List;
import java.util.Random;
import java.util.Stack;

public class NavigationDrawerFragment extends android.support.v4.app.Fragment implements NavigationDrawerCallbacks, LiveRadioFragment.LiveRadioConstants, ApplicationConstants.SectionType {
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREFERENCES_FILE = "my_app_settings";
    private static final String USER_FIRSTTIME_LAUNCH = "navigationdrawer_is_open";

    private NavigationDrawerCallbacks mCallbacks;

    private RecyclerView mDrawerList;

    private View mFragmentContainerView;

    private DrawerLayout mDrawerLayout;

    private Toolbar mToolBar;

    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private boolean mUserFirsttimeLaunched;

    private int mCurrentSelectedPosition;
    private int mCurrentSelectedSection;
    private String mDetailUrl;
    private String mNewsItemID;

    private List<NavigationItem> mNavigationItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerList = (RecyclerView) view.findViewById(R.id.drawerList);
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerList.setLayoutManager(layoutManager);
        mDrawerList.setHasFixedSize(true);

        mNavigationItem = ConfigManager.getInstance().getNavigationItems();
        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(mNavigationItem, getActivity());
        adapter.setNavigationDrawerCallbacks(this);
        mDrawerList.setAdapter(adapter);
        selectItem(mCurrentSelectedPosition, mCurrentSelectedSection, mDetailUrl, mNewsItemID);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "false"));
        extractBundleData();
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
        setDrawerListner(getActivity());
    }

    private void extractBundleData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurrentSelectedPosition = bundle.getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS);
            mCurrentSelectedSection = bundle.getInt(ApplicationConstants.BundleKeys.SECTION_POS, 0);
            mDetailUrl = bundle.getString(ApplicationConstants.BundleKeys.URL_STRING);
            mNewsItemID = bundle.getString(ApplicationConstants.BundleKeys.NEWSITEMID);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
        //TODO handle external changes ex.font changes on the device settings
        //setDrawerListner(activity);
    }


    public ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mActionBarDrawerToggle;
    }

    public void setActionBarDrawerToggle(ActionBarDrawerToggle actionBarDrawerToggle) {
        mActionBarDrawerToggle = actionBarDrawerToggle;
    }

    public void setup(View containerView, Toolbar toolbar) {
        mFragmentContainerView = containerView;
        //   mDrawerLayout = drawerLayout;
        mToolBar = toolbar;
    }

    private void setDrawerListner(Activity activity) {

        if (mDrawerLayout == null)
            mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer);

        if (mFragmentContainerView == null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.navigation_container, this)
                    .commit();
            mFragmentContainerView = getActivity().findViewById(R.id.navigation_container);
        }

        if (mToolBar == null) {
            mToolBar = ((BaseActivity) getActivity()).getActionBarToolbar();
        }


        if (mDrawerLayout == null || mToolBar == null || mFragmentContainerView == null) {
            Crashlytics.log("NavigationDrawerFragment::setDrawerListner " + mDrawerLayout + "  " + mToolBar + " " + mFragmentContainerView);
          //  throw new IllegalStateException("Call setup method after creating NavigationDrawerFragment instance");

//            Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName() );
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(i);

            Intent mStartActivity = new Intent(getActivity(), SplashActivity.class);
            PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), new Random().nextInt(100), mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, mPendingIntent);
            System.exit(0);

        }

        mActionBarDrawerToggle = new ActionBarDrawerToggle(activity, mDrawerLayout, mToolBar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    saveSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "true");
                }
                getActivity().invalidateOptionsMenu();
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState)
            mDrawerLayout.openDrawer(mFragmentContainerView);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void openDrawerForFirsttime() {
        mDrawerLayout.openDrawer(mFragmentContainerView);

        saveSharedSetting(getActivity(), USER_FIRSTTIME_LAUNCH, "false");

    }

    public void closeDrawer() {
        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    // private ArrayList<Integer> mNavigationStack = new ArrayList<>();
    private Stack<Integer> mNavigationStack = new Stack<>();
    //private int indexPointer = -1;

    void selectItem(int position, int section, String url, String itemid) {
        addNavigationToStack(position);
        setNavigationTitle(position);
        mCurrentSelectedPosition = position;
        mUserFirsttimeLaunched = Boolean.valueOf(readSharedSetting(getActivity(), USER_FIRSTTIME_LAUNCH, "true"));

        if (mUserFirsttimeLaunched)
            openDrawerForFirsttime();
        else {
            closeDrawer();
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(mCurrentSelectedPosition, section, url, itemid);
        }
        markNavigationItemSelected(mCurrentSelectedPosition);
    }

    public void addNavigationToStack(int navigationIndex) {
        if (mNavigationStack.size() == 0)
            mNavigationStack.push(navigationIndex);
    }

    private void setNavigationTitle(int index) {
        if(null!=mNavigationItem && mNavigationItem.size()>index) {
            getActivity().setTitle(mNavigationItem.get(index).getText());
        }
    }

    public void popNavigationFromStack() {
        if (!mNavigationStack.isEmpty())
            mNavigationStack.pop();
        if (!mNavigationStack.isEmpty() && mNavigationStack.lastElement() != -1) {
            markNavigationItemSelected(mNavigationStack.lastElement());
            setNavigationTitle(mNavigationStack.lastElement());
        }
    }

    public int getLastElementIndex() {
        return mNavigationStack.lastElement();
    }

    public int stackCount() {
        return mNavigationStack.size();
    }

    public boolean isStackEmpty() {
        return mNavigationStack.isEmpty();
    }

    public void markNavigationItemSelected(int index) {
        ((NavigationDrawerAdapter) mDrawerList.getAdapter()).selectPosition(index);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, int section, String url, String itemid) {
        FragmentHelper.clearBackStack(getActivity());
        selectItem(position, section, url, itemid);
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }
}
