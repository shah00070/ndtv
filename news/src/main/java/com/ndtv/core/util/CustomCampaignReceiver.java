package com.ndtv.core.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.analytics.CampaignTrackingReceiver;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.LOGE;

/**
 * Created by Chandan kumar on 01-07-2015.
 */
public class CustomCampaignReceiver extends BroadcastReceiver {

    private static final String TAG = "CustomCampaignReceiver";

    public static final String REFERRER = "REF";

    public static final String UTM_CAMPAIGN = "utm_campaign";
    public static final String UTM_SOURCE = "utm_source";
    public static final String UTM_MEDIUM = "utm_medium";
    public static final String UTM_TERM = "utm_term";
    public static final String UTM_CONTENT = "utm_content";
    public static final String POSTBACK_URL = "postBackUrl";
    public static final String POSTBACK_ID = "postBackID";

    public SharedPreferences preferences;

    private final String[] sources = {
            UTM_CAMPAIGN, UTM_SOURCE, UTM_MEDIUM, UTM_TERM, UTM_CONTENT, POSTBACK_URL, POSTBACK_ID
    };

    private String postBackUrl;
    private String postBackID;


    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        String referrerString = null;
        try {
            referrerString = URLDecoder.decode(extras.getString("referrer"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            Map<String, String> getParams = getHashMapFromQuery(referrerString);

            preferences = context.getSharedPreferences(REFERRER, Context.MODE_PRIVATE);

            SharedPreferences.Editor preferencesEditor = preferences.edit();

            for (String sourceType : sources) {
                String source = getParams.get(sourceType);

                if (source != null) {
                    preferencesEditor.putString(sourceType, source);
                }
            }
            preferencesEditor.commit();

            postBackUrl = preferences.getString(POSTBACK_URL, null);
            postBackID = preferences.getString(POSTBACK_ID, null);

            if (!(TextUtils.isEmpty(postBackID))) {
                postBackUrl = postBackUrl + postBackID;
            }

            if (!(TextUtils.isEmpty(postBackUrl))) {
                new PostBackAsyncTask().execute(postBackUrl);
            }

        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
        } finally {

            CampaignTrackingReceiver receiver = new CampaignTrackingReceiver();
            receiver.onReceive(context, intent);
        }
    }

    public static Map<String, String> getHashMapFromQuery(String query)
            throws UnsupportedEncodingException {

        Map<String, String> query_pairs = new LinkedHashMap<String, String>();

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    public class PostBackAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            postData(params[0]);
            return null;
        }

        public void postData(String url) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            try {
                LOGD(TAG, url);
                httpclient.execute(httppost);
            } catch (Exception ex) {
                LOGE(TAG, ex.getMessage());
            }
        }
    }
}