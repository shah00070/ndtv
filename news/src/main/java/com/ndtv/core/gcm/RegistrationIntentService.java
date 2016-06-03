package com.ndtv.core.gcm;

/**
 * Created by sridhard on 8/26/15.
 */

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import com.ndtv.core.common.util.PreferencesManager;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "GCM";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //SharedPreferences prefs = GcmUtility.getGcmPreferences(this);


        try {
            GcmUtility.registerInBackground(getApplicationContext());

            // Subscribe to topic channels
            //subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
           //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
          //  sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
      // Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
      // LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);

        //PreferencesManager.getInstance(this).setPushStatus(true);


    }




}