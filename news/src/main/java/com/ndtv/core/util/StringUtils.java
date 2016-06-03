package com.ndtv.core.util;

import android.text.TextUtils;

import java.nio.charset.Charset;

/**
 * Created by Srihari S Reddy on 04/02/15.
 */
public class StringUtils {

    public static String decodeString(String encoded) {
        if (TextUtils.isEmpty(encoded)) {
            return null;
        }
        try {
            return new String(encoded.getBytes(Charset.forName("ISO-8859-1")), "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            return encoded;
        }
    }

    public static String decodeStringWithoutCharset(String encoded) {
        if (TextUtils.isEmpty(encoded)) {
            return null;
        }
        try {
            return new String(encoded.getBytes(), "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            return encoded;
        }
    }

    public static String encodeString(String encoded) {
        if (TextUtils.isEmpty(encoded)) {
            return null;
        }
        try {
            return new String(encoded.getBytes(Charset.forName("UTF-8")), "ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
            return encoded;
        }
    }


}
