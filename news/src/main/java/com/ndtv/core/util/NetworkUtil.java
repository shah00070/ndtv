package com.ndtv.core.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Srihari S Reddy on 25/01/15.
 */
public final class NetworkUtil {

    public static final int MOBILE_NETWORK = 0;
    public static final int WIFI = 1;

    public NetworkUtil() {
    }

    public static int getConnectionType(Context context) {
        ConnectivityManager connectivitymanager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivitymanager != null) {
            NetworkInfo networkinfo = connectivitymanager.getActiveNetworkInfo();
            if (networkinfo != null && networkinfo.isConnected()) {
                if (networkinfo.getType() == 0) {
                    return NetworkUtil.MOBILE_NETWORK;
                }
                if (networkinfo.getType() == 1) {
                    return NetworkUtil.WIFI;
                }
            }
        }
        return -1;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivitymanager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivitymanager != null) {
            NetworkInfo networkinfo = connectivitymanager.getActiveNetworkInfo();
            if (networkinfo != null) {
                return networkinfo.isConnected();
            }
        }
        return false;
    }
}
