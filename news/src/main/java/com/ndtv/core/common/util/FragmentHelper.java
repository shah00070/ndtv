/**
 Project      : Awaaz
 Filename     : FragmentHelper.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


/**
 * @author HArisha B
 */
public class FragmentHelper {
    /**
     * This removes current fragment and adds a new fragment.
     *
     * @param activity
     * @param frgmt
     */


    public static FragmentHelper sFragementHelper;

    public static FragmentHelper getInstance() {
        return sFragementHelper == null ? new FragmentHelper() : sFragementHelper;
    }

    private FragmentHelper() {
    }

    public static void replaceAndAddToBackStack(final FragmentActivity activity, final int containerId,
                                                final Fragment fragment, String tag) {
        try {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(containerId, fragment);
            transaction.addToBackStack(tag);
            transaction.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void replaceFragment(final FragmentActivity activity, final int containerId,
                                       final Fragment fragment) {
        try {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(containerId, fragment);
            transaction.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void replaceFragment(final FragmentActivity activity, final int containerId,
                                       final Fragment fragment, String tag) {
        try {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(containerId, fragment, tag);
            transaction.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void clearBackStack(final FragmentActivity activity) {
        try {
            if (null != activity)
                activity.getSupportFragmentManager().popBackStackImmediate(null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

}
