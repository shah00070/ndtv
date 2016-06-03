package com.ndtv.core.now;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.search.GoogleNowAuthState;
import com.google.android.gms.search.SearchAuth;
import com.google.android.gms.search.SearchAuthApi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;

public class GetAuthCodeService extends IntentService {
    private static final String TAG = "GetAuthCodeService";

    public GetAuthCodeService() {
        super("GetAuthCodeService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        Intent responseIntent = new Intent(Constants.BROADCAST_ACTION);
        Bundle extras = workIntent.getExtras();
        HashMap<String, String> params = new HashMap<String, String>();
        if (extras != null) {
             responseIntent.putExtras(extras);
            Serializable inputParams = extras.getSerializable(Constants.PARAMS_EXTRA);
            if (inputParams != null) {
                params.putAll((HashMap<String, String>) inputParams);
            }
        }


        SearchAuthApi api = SearchAuth.SearchAuthApi;
        GoogleApiClient client = new GoogleApiClient.Builder(GetAuthCodeService.this)
                .addApi(SearchAuth.API)
                .build();
        client.connect();

        SearchAuthApi.GoogleNowAuthResult authResult;
        try {
            authResult = api.getGoogleNowAuth(client, Constants.SERVER_CLIENT_ID).await();
        } finally {
            client.disconnect();
        }

        com.google.android.gms.common.api.Status status = authResult.getStatus();

        if (status != null && status.isSuccess()) {
            GoogleNowAuthState googleNowAuthState = authResult.getGoogleNowAuthState();
            if (googleNowAuthState != null) {
                if (!TextUtils.isEmpty(googleNowAuthState.getAuthCode())) {

                    Log.v(TAG, "Got auth code");
                    responseIntent.putExtra(Constants.AUTH_CODE_EXTRA,
                            googleNowAuthState.getAuthCode());
                } else if (!TextUtils.isEmpty(googleNowAuthState.getAccessToken())) {

                    Log.v(TAG, "Got access token");
                    responseIntent.putExtra(Constants.ACCESS_TOKEN_EXTRA,
                            googleNowAuthState.getAccessToken());
                }

                responseIntent.setPackage(getPackageName());
                sendBroadcast(responseIntent);
            }
        } else {
            Log.e(TAG, "Failure status: " + status.getStatusMessage());
        }
    }
}
