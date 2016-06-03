/**
 Project      : Awaaz
 Filename     : CommentConnestionManager.java
 Author       : adithya
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author adithya
 */
public class CommentConnectionManager {

    private final String SUBMIT_POST = "Post";
    private final String SUBMIT_SAVE = "Save";
    private String key_id;
    private static String TAG = CommentConnectionManager.class.getSimpleName();

    public CommentConnectionManager(String key_id) {
        this.key_id = key_id;
    }

    public String HttpPostRequest(String url, Bundle params, String Submit_method) {
        // random string as boundary for multi-part http post
        String strBoundary = Long.toHexString(System.currentTimeMillis());
        String endLine = "\r\n";
        OutputStream os;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + strBoundary);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();

            os = new BufferedOutputStream(conn.getOutputStream());
            os.write((endLine + endLine).getBytes());
            os.write(("--" + strBoundary + endLine).getBytes());
            os.write((encodePostBody(params, strBoundary)).getBytes());
            os.write((endLine + "--" + strBoundary + endLine).getBytes());
            if (Submit_method.equals(SUBMIT_POST)) {
                os.write(("Content-Disposition: form-data; name=\"key\"" + endLine + endLine + key_id).getBytes());
                os.write((endLine + "--" + strBoundary + endLine).getBytes());
            }
            os.write(("Content-Disposition: form-data; name=\"submit\"" + endLine + endLine + Submit_method).getBytes());
            os.write((endLine + "--" + strBoundary + endLine).getBytes());
            os.flush();

            String response = "";
            try {
                response = read(conn.getInputStream(), Submit_method, true);
            } catch (FileNotFoundException e) {
                response = read(conn.getErrorStream(), Submit_method, false);
            }
            conn.disconnect();
            return response;
        } catch (MalformedURLException e) {
            Log.d(TAG, " MalformedURLException Message: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, " IOException Message: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generate the multi-part post body providing the parameters and boundary
     * string
     *
     * @param parameters the parameters need to be posted
     * @param boundary   the random string as boundary
     * @return a string of the post body
     */
    public static String encodePostBody(Bundle parameters, String boundary) {
        String endLine = "\r\n";
        if (parameters == null)
            return "";
        StringBuilder sb = new StringBuilder();

        for (String key : parameters.keySet()) {
            if (parameters.getByteArray(key) != null) {
                continue;
            }

            sb.append("Content-Disposition: form-data; name=\"" + key + "\"" + endLine + endLine
                    + parameters.getString(key));
            sb.append(endLine + "--" + boundary + endLine);
        }
        return sb.toString();
    }

    private String read(InputStream in, String submit_method, boolean isSuccess) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        JSONObject responseObj = null;
        try {
            responseObj = new JSONObject(sb.toString());
            if (isSuccess) {
                android.util.Log.d("PostCommentTag", " " + responseObj.toString());
                if (submit_method.equals(SUBMIT_SAVE)) {
                    return responseObj.getString("uid");
                } else {
                    return responseObj.getJSONObject("messages").getJSONArray("success").getString(0);
                }
            } else {
                return ("Error" + responseObj.getString("error"));
            }
        } catch (JSONException e) {
            Log.d(TAG, " JSONException message: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
