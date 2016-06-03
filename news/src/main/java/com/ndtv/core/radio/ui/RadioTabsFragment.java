package com.ndtv.core.radio.ui;

import android.os.Bundle;
import android.view.View;

import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.radio.LiveRadioManager;
import com.ndtv.core.ui.HomeFragment;

/**
 * Created by LENOVO on 07-02-2015.
 */
public class RadioTabsFragment extends HomeFragment implements LiveRadioFragment.LiveRadioConstants {

    private int mSectionPos;
    private boolean bisFromRadio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bisFromRadio = getArguments().getBoolean(LiveRadioFragment.LiveRadioConstants.FROM_NOTIFICATION);
            if (LiveRadioManager.getLiveRadioManagerInstance() != null)
                LiveRadioManager.getLiveRadioManagerInstance().setFromNotification(bisFromRadio);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mNavigation.type.equalsIgnoreCase(ApplicationConstants.SectionType.AUDIO)) {

            mSectionPos = PreferencesManager.getInstance(getActivity()).getSectionPositionLiveRadio();
            if (bisFromRadio)
                //set flag to indicate that its from notification
                mViewPager.setCurrentItem(mSectionPos);
            else

                mViewPager.setCurrentItem(0);

        }
    }
}
