

package com.ndtv.core.gcm;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.ndtv.core.constants.ApplicationConstants;


public class MyGcmListenerService extends GcmListenerService implements ApplicationConstants.BundleKeys {

    public static final String SECTION = "section";
    public static final String IMAGE_NODE = "story_image";
    private static final String TAG = "GCM";

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String section = null;
        //section=extras.getString(EXTRA_SECTION);
        section = data.getString(SECTION);
        String imagePath = null;
        imagePath = data.getString(IMAGE_NODE);

        if (!TextUtils.isEmpty(section))
            sendNotification(data.getString(EXTRA_MESSAGE), section, imagePath);
        else
            sendNotification(data.getString(EXTRA_MESSAGE), DEFAULT_NOTI_NAV + ApplicationConstants.Seperator.DEFAULT_SECTION_SEPARATOR + DEFAULT_NOTI_SEC, imagePath);

        Log.i(TAG, "Received: " + data.toString() + section);


    }

    private void sendNotification(String msg, String section, String imageUrl) {

        if (msg != null) {
            //check if image url is present
            if (imageUrl != null) {
                downloadImage(imageUrl, msg, section);

            } else
                GcmUtility.generateNotification(getApplicationContext(), msg, section);
        }
    }


    private void downloadImage(final String imageUrl, String msg, final String section) {

        Bitmap bitmap = GcmUtility.getBitmapFromURL(imageUrl);
        if (bitmap != null)
            GcmUtility.generateImageNotification(getApplicationContext(), msg, section, bitmap, imageUrl);
    }


}
