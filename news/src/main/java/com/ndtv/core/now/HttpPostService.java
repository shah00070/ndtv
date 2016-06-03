package com.ndtv.core.now;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;


public class HttpPostService extends IntentService {
    private static final String TAG = "HttpPostService";

    public HttpPostService() {
        super("HttpPostService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        HttpTransport httpTransport = new NetHttpTransport();
        String url = workIntent.getStringExtra(Constants.METHOD_EXTRA);

        if (url == null) {
            return;
        }
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

        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        final HttpContent content = new UrlEncodedContent(params);

        try {
            final HttpRequest request = requestFactory.buildPostRequest(
                    new GenericUrl(url), content);
            HttpResponse response = request.execute();

            int statusCode = response.getStatusCode();
            responseIntent.putExtra(Constants.DATA_STATUS_EXTRA, statusCode);

            if (statusCode == HttpStatusCodes.STATUS_CODE_OK) {
                responseIntent.putExtra(Constants.DATA_RESPONSE_EXTRA,
                        response.parseAsString().trim());
            }
            response.disconnect();
        } catch (IOException e) {
            if (e.getMessage() != null)
                Log.e(TAG, e.getMessage());
        } finally {
            responseIntent.setPackage(getPackageName());
            sendBroadcast(responseIntent);
        }
    }
}
