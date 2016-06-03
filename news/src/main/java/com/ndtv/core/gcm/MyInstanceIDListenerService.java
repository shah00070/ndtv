package com.ndtv.core.gcm;

/**
 * Created by sridhard on 8/26/15.
 */

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;



public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "GCM";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */

    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

}