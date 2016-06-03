package com.ndtv.core.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.R;
import com.ndtv.core.ui.widgets.BaseFragment;

/**
 * Created by laveen on 25/2/15.
 */

/**
 * Etending from this fragment provides chromecast related functionality
 */
public abstract class ChromeCastFragment extends BaseFragment {
    protected VideoCastManager mCastManager;
    protected MenuItem mediaRouteMenuItem;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        VideoCastManager.checkGooglePlayServices(getActivity());
        mCastManager = NdtvApplication.getCastManager();
        mCastManager.reconnectSessionIfPossible();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d("OPTIONMENU", "REF:" + this);

        inflater.inflate(R.menu.menu_chrome_cast, menu);
        mediaRouteMenuItem = mCastManager.
                addMediaRouterButton(menu, R.id.media_route_menu_item);
        //super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }



    @Override
    public void onResume() {
        mCastManager = NdtvApplication.getCastManager();
        mCastManager.incrementUiCounter();
        super.onResume();
    }

    @Override
    public void onPause() {
        mCastManager.decrementUiCounter();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (null != mCastManager) {
            mCastManager.clearContext(getActivity());
        }
        super.onDestroy();
    }

    public boolean isAppConnectedToCastDevice() {
        if (mCastManager.isConnected())
            return true;
        return false;
    }

}
