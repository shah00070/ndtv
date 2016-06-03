package com.ndtv.core.deeplinking.io;

/**
 * Created by MY on 12/15/2014.
 */
public interface OnDeepLinkingInterface {
    public void onHandleDeepLink(String url);
    public String DEEP_LINK_NEWS ="news";
    public String DEEP_LINK_PHOTO="photo";
    public String DEEP_LINK_VIDEO="video";
    public String DEEP_LINK_PHOTOS="photos";
    public String DEEP_LINK_VIDEOS="videos";
    public String DEEP_LINK_LIVETV="Live TV";
    public String DEEP_LINK_STORY="story";
    public String DEEP_LINK_LIVETV_TYPE = "livetv";
    public String DEEP_LINK_CRICKET_SCORECARD = "cricket-scorecard";
}
