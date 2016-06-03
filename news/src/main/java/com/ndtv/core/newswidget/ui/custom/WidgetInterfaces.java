package com.ndtv.core.newswidget.ui.custom;

import com.ndtv.core.video.dto.Videos;

/**
 * Created by Harisha B on 23/2/15.
 */
public class WidgetInterfaces {

    public static interface OnVideoItemAvailbleListener {
        public void onVideoAvailable(Videos videos);
    }
}
