package com.ndtv.core.now;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.http.HttpStatusCodes;

import java.util.HashMap;


public class ResponseHandler extends BroadcastReceiver {
    private static final String TAG = "ResponseHandler";

    @Override
    public void onReceive(Context context, Intent intent) {
        String method = intent.getStringExtra(Constants.METHOD_EXTRA);
        String user = intent.getStringExtra(Constants.USER_PARAM);

        if (method.equals(Constants.CHECK_CREDENTIALS_URL)) {
             String response = intent.getStringExtra(Constants.DATA_RESPONSE_EXTRA);
            Log.d(TAG, "Response "+ response);


            if (intent.getIntExtra(Constants.DATA_STATUS_EXTRA, 0)
                    == HttpStatusCodes.STATUS_CODE_OK) {
                if (response.equals(Constants.VALID_CREDENTIALS_SERVER_RESPONSE)) {
                   Log.d(TAG, "Server has valid credentials.");
                } else if (response.equals(Constants.INVALID_CREDENTIALS_SERVER_RESPONSE)) {
                    Log.e(TAG, "Server credentials invalid. Getting new auth code...");
                    getAuthCode(context, user);
                } else {
                   Log.e(TAG, "Unable to parse server response.");
                }
            } else {
                Log.e(TAG, "Error occurred while checking credentials.");
            }
        } else if (method.equals(Constants.GET_AUTH_CODE_METHOD)) {
            String authCode = intent.getStringExtra(Constants.AUTH_CODE_EXTRA);
            String accessToken = intent.getStringExtra(Constants.ACCESS_TOKEN_EXTRA);

            if (accessToken != null) {
                Log.d(TAG, "Already have existing token. Revoking existing access token: " +
                        accessToken);
                revokeAndRetry(context, accessToken, user);
            } else if (authCode != null) {
                sendAuthCodeToServer(context, authCode, user);
            } else {
                Log.e(TAG, "Unexpected error occurred while getting the auth code.");
            }
        } else if (method.equals(Constants.ADD_CREDENTIALS_URL)) {
            if (intent.getIntExtra(Constants.DATA_STATUS_EXTRA, 0)
                    == HttpStatusCodes.STATUS_CODE_OK) {
                Log.d(TAG, "Successfully posted credentials to the server.");
            } else {
                Log.e(TAG, "An error occurred posting user credentials.");
            }
        } else if (method.startsWith(Constants.REVOKE_CREDENTIALS_URL)) {
            if (intent.getIntExtra(Constants.DATA_STATUS_EXTRA, 0)
                    == HttpStatusCodes.STATUS_CODE_OK) {
               Log.d(TAG, "Token revoked successfully. Getting new auth code...");
                getAuthCode(context, user);
            } else {
                Log.e(TAG, "There was an error revoking the token.");
            }
        }
    }


    private void getAuthCode(Context context, String user) {
        Intent getAuthCodeIntent = new Intent(context, GetAuthCodeService.class);
        getAuthCodeIntent.putExtra(Constants.METHOD_EXTRA, Constants.GET_AUTH_CODE_METHOD);
        // Put the user in the intent, so we know the user when we handle the response.
        getAuthCodeIntent.putExtra(Constants.USER_PARAM, user);
        context.startService(getAuthCodeIntent);
    }


    private void sendAuthCodeToServer(Context context, String authCode, String user) {
        Bundle paramsBundle = new Bundle();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.USER_PARAM, user);
        params.put(Constants.AUTH_CODE_PARAM, authCode);
        paramsBundle.putSerializable(Constants.PARAMS_EXTRA, params);

        Intent postCredentialsIntent = new Intent(context, HttpPostService.class);
        postCredentialsIntent.putExtra(Constants.METHOD_EXTRA, Constants.ADD_CREDENTIALS_URL);
        postCredentialsIntent.putExtras(paramsBundle);

        context.startService(postCredentialsIntent);
    }


    private void revokeAndRetry(Context context, String accessToken, String user) {
        Intent revokeCredentialsIntent = new Intent(context, HttpPostService.class);
        revokeCredentialsIntent.putExtra(Constants.METHOD_EXTRA,
            Constants.REVOKE_CREDENTIALS_URL + "?token=" + accessToken);
        revokeCredentialsIntent.putExtra(Constants.USER_PARAM, user);
        context.startService(revokeCredentialsIntent);
    }
}
