package com.ndtv.core.util;

import android.net.Uri;
import android.text.TextUtils;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

/**
 * Created by laveen on 27/2/15.
 */
public class ChromecastUtil {

    public static MediaInfo createMediaInfo(String url, String title, String subTitle, String smallImage, String largeImage) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);

        if (!TextUtils.isEmpty(smallImage))
            movieMetadata.addImage(new WebImage(Uri.parse(smallImage)));
        if (!TextUtils.isEmpty(largeImage))
            movieMetadata.addImage(new WebImage(Uri.parse(largeImage)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4")
                .setMetadata(movieMetadata)
                .build();
    }
}
