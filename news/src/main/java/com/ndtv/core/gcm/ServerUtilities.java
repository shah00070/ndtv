/**
 Project      : Awaaz
 Filename     : ServerUtilities.java
 Author       : Adithya A.M.
 Comments     :
 Copyright    : � Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */
package com.ndtv.core.gcm;

import android.content.Context;
import android.text.TextUtils;

import com.ndtv.core.R;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants.BundleKeys;
import com.ndtv.core.constants.ApplicationConstants.CustomApiType;
import com.ndtv.core.constants.ApplicationConstants.PreferenceKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static com.ndtv.core.util.LogUtils.LOGD;


/**
 * Helper class used to communicate with the server.
 */
public final class ServerUtilities implements BundleKeys, PreferenceKeys, CustomApiType {

    public static final String REGID_KEY = "regId";
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final String REGISTRATION_ID = "registration_id";
    private static final String PACKAGE_NAME = "packageName";
    private static final String ENVIRONMENT_KEY = "env";
    private static final String UNREGISTER = "unregister";
    private static final String UNREGISTER_VALUE = "1";
    private static final String REGISTER_VALE = "0";
    private static final Random random = new Random();

    private static final String NIGHT_TIME = "nightTime";
    private static final String SECTION = "section";
    private static final String NIGHT_NOTIFICATION = "nightNotification";
    private static final String DAY_NOTIFICATION = "dayNotification";
    private static final String DEFAULT_DAY_NOTIFICATION_VALUE = "1";
    /**
     * Live application data
     */
    public static final String SENDER_ID = "72129834489";
    public static final String SENDER_ID_HINDI = "550266002867";
    public static final String SENDER_ID_PRIME = "886688350916";
    // public static final String SERVER_URL =
    // "http://107.21.225.166/awaaz_pushnotification/";

    private static final String ENVIRONMENT = "PRODUCTION";
    //For GCM Test Purpose
    //private static final String ENVIRONMENT = "DEVELOPMENT";

    private static final String TAG = "GCM";

    /** Testing purpose(local) application data */

    /**
     * Register this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    public static boolean register(final Context context, final String regId) {
        if (Utility.isInternetOn(context)) {
            String serverUrl = ConfigManager.getInstance().getCustomApiUrl(DEVICE_TOKEN_API);
            //Hardcode for temp logic
            //String serverUrl = "http://ipush.apps.ndtv.com/subscribeAndroid.php";

            // String serverUrl = SERVER_URL;
            if (!TextUtils.isEmpty(serverUrl)) {
                Map<String, String> params = new HashMap<String, String>();
                params.put(REGISTRATION_ID, regId);
                params.put(PACKAGE_NAME, context.getPackageName());
                params.put(ENVIRONMENT_KEY, ENVIRONMENT);
                params.put(UNREGISTER, REGISTER_VALE);
                /** Additional params for custom push. */
                params.put(NIGHT_TIME, getTime());
                String sections = getSections(context);
                //Log.d(TAG, "sections: " + sections);
                params.put(SECTION, sections);
                params.put(NIGHT_NOTIFICATION, getNightNotification(context));
                params.put(DAY_NOTIFICATION, DEFAULT_DAY_NOTIFICATION_VALUE);
                storeCurrentTimeZone(context, Utility.getCurrentTimeZone().getID());
                long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
                /**
                 * Once GCM returns a registration id, we need to register it in
                 * the demo server. As the server might be down, we will retry
                 * it a couple times.
                 */
                for (int i = 1; i <= MAX_ATTEMPTS; i++) {
                    try {
                        post(serverUrl, params, context);
                        // GCMRegistrar.setRegisteredOnServer(context, true);
                        return true;
                    } catch (IOException e) {
                        /**
                         * Here we are simplifying and retrying on any error; in
                         * a real application, it should retry only on
                         * unrecoverable errors (like HTTP error code 503).
                         */
                        if (i == MAX_ATTEMPTS) {
                            break;
                        }
                        try {
                            Thread.sleep(backoff);
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                        /** increase back off exponentially */
                        backoff *= 2;
                    }
                }
            }
        } else {
            // Toast.makeText(context, "No Network Connection",
            // Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * @param context
     * @return
     */
    private static String getSections(Context context) {
        StringBuilder sb = new StringBuilder();
        List<String> sectionList = Arrays.asList(context.getResources()
                .getStringArray(R.array.custom_push_section_list));
        PreferencesManager preManager = PreferencesManager.getInstance(context.getApplicationContext());
        int size = sectionList.size();
        for (int i = 0; i < size; i++) {
            if (preManager.getNotificationSettings(sectionList.get(i))) {
                if (i != size - 1)
                    sb.append((i + 1) + ",");
                else
                    sb.append(i + 1);

            }

        }
        String string = sb.toString();
        /**
         * removing the trailing , which is not required
         */
        if (!TextUtils.isEmpty(string) && string.endsWith(",")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    /**
     * @param context
     * @return
     */
    private static String getNightNotification(Context context) {
        PreferencesManager prefManager = PreferencesManager.getInstance(context.getApplicationContext());
        if (prefManager.getNotificationSettings(NOTIFICATION_AT_NIGHT))
            return "1";
        return "0";
    }

    /**
     * @return
     */
    private static String getTime() {
        return Integer.toString((22 * 60 * 60) - Utility.getTimeZoneOffset());
    }

    private static void storeCurrentTimeZone(final Context context, final String id) {
        PreferencesManager.getInstance(context).setCurrentTimeZone(id);
    }

    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(final Context context, final String regId) {
        String serverUrl = ConfigManager.getInstance().getCustomApiUrl(DEVICE_TOKEN_API);
        //String serverUrl = "http://ipush.apps.ndtv.com/subscribeAndroid.php";
        // String serverUrl = SERVER_URL;

        if (!TextUtils.isEmpty(serverUrl)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(REGISTRATION_ID, regId);
            params.put(PACKAGE_NAME, context.getPackageName());
            params.put(ENVIRONMENT_KEY, ENVIRONMENT);
            params.put(UNREGISTER, UNREGISTER_VALUE);
            try {
                post(serverUrl, params, context);
                // GCMRegistrar.setRegisteredOnServer(context, false);
            } catch (IOException e) {
                /**
                 * At this point the device is unregistered from GCM, but still
                 * registered in the server. We could try to unregister again,
                 * but it is not necessary: if the server tries to send a
                 * message to the device, it will get a "NotRegistered" error
                 * message and should unregister the device.
                 */

            }
        }
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params   request parameters.
     * @param context
     * @throws java.io.IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params, Context context) throws IOException {
        URL url = null;
        try {
            url = new URL(endpoint.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        /** constructs the POST body using the parameters */
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        //Log.d(TAG, "body : " + body);
        LOGD(TAG, "body : " + body);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            /** post the request */
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            InputStream is = conn.getInputStream();
            if (endpoint.contains("unregisterGCM.php")) {
                String x = Utility.getString(is);
                if (x.contains("1")) {
                }
            } else {
                String x = Utility.getString(is);
                //Log.d(TAG, "response :" + x);
                if (x.contains("<registerStatus>1</registerStatus>")) {
                    // Toast.makeText(context.getApplicationContext(),
                    // "push notification registered", Toast.LENGTH_SHORT)
                    // .show();
                } else if (x.contains("<registerStatus>2</registerStatus>")) {
                    // Toast.makeText(context.getApplicationContext(),
                    // "push notification already registered",
                    // Toast.LENGTH_SHORT).show();
                }
            }
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
