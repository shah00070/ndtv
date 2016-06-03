package com.ndtv.core.common.util.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.ndtv.core.constants.ApplicationConstants;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by root on 20/2/15.
 */
public class URLUtility implements ApplicationConstants.UrlKeys {

    /**
     * @returns URL with launguage_id,pageNum and size
     */
    public static String getFinalUrl(String url, Context context) {
//        PreferencesManager prefMngr = PreferencesManager.getInstance(context);
        String keys[] = new String[]{URL_LANGUAGE_ID, PAGE_NUMBER, PAGE_SIZE};
        String values[] = new String[]{ /*prefMngr.getLanguageId()*/"1", /*PAGE_NUM*/"", PAGE_SIZE};
        String finalUrl = url;
        int size = keys.length;
        if (null != finalUrl) {
            if (size > 0 && size == values.length) {
                for (int i = 0; i < size; i++) {
                    if (finalUrl.contains(keys[i])) {
                        finalUrl = finalUrl.replace(keys[i], values[i]);
                    }
                }
            }
            return getEncodedUrl(finalUrl);
        } else {
            return "";
        }

    }

    public static String getEncodedUrl(String url) {
        try {
            URL urlTemp = new URL(url);
            URI uri = new URI(urlTemp.getProtocol(), urlTemp.getUserInfo(), urlTemp.getHost(), urlTemp.getPort(),
                    urlTemp.getPath(), urlTemp.getQuery(), urlTemp.getRef());
            urlTemp = uri.toURL();
            url = urlTemp.toString();
            url = getVideoEncodedUrl(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getVideoEncodedUrl(String url) {
        if (!TextUtils.isEmpty(url) && url.contains(VIDEO_FORMAT))
            return url.replace(VIDEO_FORMAT, Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? "mp4" : "rtsp");
        return url;
    }

    /**
     * @returns URL with launguage_id,pageNum,size and extra params.
     */
    public static String getFinalUrl(String oldStrings[], String newStrings[], String url, Context ctx) {
        String newUrl = getFinalUrl(url, ctx);
        int size = oldStrings.length;
        if (size == newStrings.length) {
            for (int i = 0; i < size; i++) {
                if (oldStrings[i] != null && newStrings[i] != null) {
                    if (newUrl.contains(oldStrings[i])) {
                        newUrl = newUrl.replace(oldStrings[i], newStrings[i]);
                    }
                }
            }
        }
        return getEncodedUrl(newUrl);
    }
    /**
     * @returns URL with launguage_id,pageNum,size and extra params.
     */
    public static String getFinalUrl(String oldStrings[], String newStrings[], String url, Context ctx,int pageNum) {
        String newUrl = getFinalUrl(url, ctx,pageNum);
        int size = oldStrings.length;
        if (size == newStrings.length) {
            for (int i = 0; i < size; i++) {
                if (oldStrings[i] != null && newStrings[i] != null) {
                    if (newUrl.contains(oldStrings[i])) {
                        newUrl = newUrl.replace(oldStrings[i], newStrings[i]);
                    }
                }
            }
        }
        return getEncodedUrl(newUrl);
    }

    public static String getFinalUrl(String url, Context context,int pageNumber) {
//        PreferencesManager prefMngr = PreferencesManager.getInstance(context);
        String keys[] = new String[]{URL_LANGUAGE_ID, PAGE_NUMBER, PAGE_SIZE};
        String values[] = new String[]{ /*prefMngr.getLanguageId()*/"1", /*PAGE_NUM*/""+pageNumber, PAGE_SIZE};
        String finalUrl = url;
        int size = keys.length;
        if (null != finalUrl) {
            if (size > 0 && size == values.length) {
                for (int i = 0; i < size; i++) {
                    if (finalUrl.contains(keys[i])) {
                        finalUrl = finalUrl.replace(keys[i], values[i]);
                    }
                }
            }
            return getEncodedUrl(finalUrl);
        } else {
            return "";
        }

    }
}
