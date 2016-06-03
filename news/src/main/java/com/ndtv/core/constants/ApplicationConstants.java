package com.ndtv.core.constants;

/**
 * Created by Srihari S Reddy on 12/11/14.
 */
public interface ApplicationConstants {

    public static final int SPLASH_SHOW_TIME = 1000;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String DEFAULT_LAUNCH = "News/Top Stories";
    public static final int TIMESTAMP_CAP = 120;
    public static final String HASH_SYMBOL = "#";

    public static interface SectionType {
        String NEWS = "news";
        String AUDIO = "audio";
        String PLAYER = "player";
        String MICRO = "micro";
        String PHOTO = "photo";
        String VIDEO = "video";
        String PAGE = "page";
        String NOTIFICATION = "notofication";
    }

    public static interface SocialShare {
        String MIME_DATA_TYPE = "text/plain";

        String FACEBOOK_PKG_NAME = "com.facebook.katana";
        String FB_MSNGR_PKG_NAME = "com.facebook.orca";
        String HANGOUTS_PKG_NAME = "com.google.android.talk";
        String WHATSAPP_PKG_NAME = "com.whatsapp";
        String MMS_PKG_NAME = "com.android.mms";
        String GMAIL_PKG_NAME = "com.google.android.gm";
        String E_MAIL_PKG_NAME = "com.android.email";
        String GOOGLE_PLUS_PKG_NAME = "com.google.android.apps.plus";
        String TWITTER_PKG_NAME = "com.twitter.android";
        int GP_RESOLVE_ERR_REQUEST_CODE = 200;

    }

    public static interface NavigationType {

        String AUDIO = "audio";
        String RADIO = "Radio";
        String NEWS_IN_60 = "News in 60 seconds";
        String NEWS_DIGEST = "newsdigest";
        String SETTINGS = "settings";
        String PAGE_TYPE = "page";
        String NEWSHOME = "newshome";
        String PHOTOHOME = "photohome";
        String VIDEOHOME = "videohome";
        String LIVETVHOME = "player";
        String EXT_URL = "browser";
        String SHOWS = "shows";

    }

    public static interface BuildType {

        String NDTVNEWS = "com.july.ndtv";
        String NDTVINDIA = "com.ndtv.india";
        String NDTVPRIME = "com.mm.ndtv";


    }

    public static interface CommentConstants {

        String GET_COMMENTS_API = "GetComments";

        String POST_COMMENTS = "PostComments";
        String POST_USERINFO = "LoginPostURL";
        String POST_COMMENTS_LIKE = "LikeComment";
        String POST_COMMENTS_DISLIKE = "UnLikeComment";

        String COMMENT_FIRTS_NAME = "first_name";
        String COMMENT_LAST_NAME = "last_name";
        String COMMENT_PROFILE_IMAGE = "profile_image";
        String COMMENT_SITE_NAME = "sitename";
        String COMMENT_SUBMIT = "submit";

        String COMMENT_METHOD = "method";
        String COMMENT_AUTH_ID = "user_id";
        String COMMENT_USERID = "uid";
        String COMMENT_PAGE_TITLE = "page_title";
        String COMMENT_PAGE_URL = "page_url";
        String COMMENT_CTYPE = "ctype";
        String COMMENT_IDENTIFIER = "identifier";
        String COMMENT_TEXT = "comment";
        String PARENT_ID = "parent_id";
        String COMMENT_KEY = "comment_id";
    }

    public static interface Status {
        String ENABLED = "1";
        String DISABLED = "0";
    }

    public static interface NewsDigestConstants {
        String NEWS_IN_60_AVAILABLE = "news_in_60_available";
    }


    public static interface FragmentType {

        int NEWS_DETAIL_FRAGMENT = 1;
        int COMMENT_FRAGMENT = 2;
        int PHOTO_DETAIL_FRAGMENT = 3;
        int DEEPLINKING_NEWS_DETAIL_FRAGMENT = 4;
        int DEEPLINKING_PHOTO_FRAGMENT = 5;
        int GCM_NEWS_DETAIL_FRAGMENT = 6;
        int HOME_FRAGMENT = 7;
        int NEWS_DETAIL_SEARCH_FRAGMENT = 8;
        int SEARCHING_FRAGMENT = 9;
        int PRIME_SHOWS_FRAGMENT = 10;
        int PRIME_VIDEOS_FRAGMENT = 11;
        int LIVE_RADIO_FRAGMENT = 12;

    }

    public static interface BundleKeys {

        String NORMAL_PUSH = "com.july.ndtv.push_notification";
        String NAVIGATION_POS = "navigation_position";
        String SECTION_POS = "section_position";

        /**
         * GCM Intent's extra that contains the message to be displayed.
         */
        String EXTRA_MESSAGE = "message";
        String DEFAULT_NOTI_NAV = "Notification Hub";
        String DEFAULT_NOTI_SEC = "All";
        String SECTION_POSITION = "category_pos";
        String SECTION_TITLE = "category_name";
        String URL_STRING = "url_string";
        String NEWSITEMID = "newsitem_id";
        String FROM_GCM = "from_gcm";
        String APP_REFRESH="app_refresh";

        /**
         * Deep Linking
         */
        String IS_DEEPLINK_URL = "is_deeplink_url";
        String DEEP_LINK_URL = "deep_link_url";
        String PRIME_VIDEOS_LINK = "prime videos link";
        String PRIME_SHOW_NAME = "prime_show_name";

        String LIST_ITEM_POSITION = "position";
        String IS_RELATED_VIDEO = "is_related_video";
        String IS_SEARCH_VIDEO = "is_search_video";
        String IS_PRIME_VIDEO = "is_prime_video";
        String IS_DETAIL_FRAGMENT = "is_detail_fragment";
        String IS_FAV = "is_fav";
        String NAVIGATION_TITLE = "navigation_title";
        String TV_SHOWS = "TV Shows";
    }

    public static interface PreferenceKeys {
        /**
         * custom push settings related *
         */
        String NOTIFICATION_SOUND = "enable_default_sound";
        String NOTIFICATION_VIBRATION = "enable_default_vibrate";
        String NOTIFICATION_AT_NIGHT = "notification_at_night";
        String NOTIFICATION_INVERSE = "notification_inverse";
        String APP_REFRESH_ENABLE = "app_refresh_enable";
        String BUILD_VERSION_NAME = "version_name";

        /* Caching related */
        String SETTINGS_FIRST_LAUNCH = "settings_first_launch";
        String PUSH_NOTIFICATION_STATUS = "push_notification_status_new";

        String BREAKING_NEWS = "Breaking_News";
        String BREAKING_NEWS_HINDI = "Breaking_Hindi";
        String BREAKING_NEWS_PRIME = "Breaking_Prime";
        String MESSAGE = "message";
        String APP_UPDATE_TIME = "app_update_time";
        String UPDATE_REMINDER_FLAG = "update_reminder";
        String CURRENT_TIME_ZONE = "current_time_zone";
        String CACHE_CLEAR_VERSION = "cache_clear_version";
        String CURRENT_IMAGE_INDEX = "image_index";
        String CURRENT_AD_COUNT = "ad_count";

        /**
         * Splash Ad related
         */
        String SPLASH_AD_STATUS = "ads_status";
        String SPLASH_AD_END_DATE = "ads_enddate";
        String SPLASH_AD_START_DATE = "ads_startdate";
        String SPLASH_AD_FREQUENCY = "ads_frequency_cap";
        String SPLASH_AD_DURATION = "ads_duration";
        String APP_LAUNCH_COUNT = "app_launch_count";
        String SPLASH_AD_IMAGE = "splash_Ad_Image";
        String SPLASH_AD_LOCATION = "location";
        String FIRST_LAUNCH = "app_first_launch";
        String SPLASH_AD = "splash_ad";

        String IS_PHOTO_FULL_SCREEN = "is_photo_full_screen";
        String CURRENT_NEWS_IDENTIFIER = "current_news_id";
        String IS_BACK_FROM_COMMENT = "back_from_comment";

        /**
         * GCM Related
         */
        String NOTIFICATION_ID = "notification_id";

        /**
         * INterstitial Ads
         */
        String PHOTO_INTERSTITIAL_COUNT = "photo_interstitial_count";
    }


    public static interface CustomApiType {
        /**
         * Settings page
         */

        String ENABLE_CUSTOMIZED_PUSH = "enableCustomizedPush";
        String FEEDBACK_EMAIL_API = "feedbackEmailAPI";
        //String NEWS_DETAIL_CUSTOM_API = "newsDetailsAPI";
        /**
         * Push Notification
         */
        String DEVICE_TOKEN_API = "deviceTokenAPI";

        /**
         * Splash Ad
         */
        String ADS_IMAGE_XHDPI = "ads_image_xhdpi";
        String ADS_IMAGE_HDPI = "ads_image_hdpi";
        String ADS_IMAGE_MDPI = "ads_image_mdpi";
        String ADS_IMAGE_LDPI = "ads_image_ldpi";
        String ADS_IMAGE_XXHDPI = "ads_image_xxhdpi";
        String ADS_IMAGE_XXXHDPI = "ads_image_xxxhdpi";

        /*
        *  for videos
        * */
        String VIDEO_DETAIL_API = "videoDetailsAPI";
        String PHOTO_DETAIL_API = "photoDetailsAPI";

        /**
         * *Deeplinking
         */
        String NEWS_DETAIL_CUSTOM_API = "newsDetailsAPI";

        // comment
        String POST_COMMENTS = "PostComments";
        String POST_USERINFO = "LoginPostURL";
        String POST_COMMENTS_LIKE = "LikeComment";
        String POST_COMMENTS_DISLIKE = "UnLikeComment";

        String CRICKET_CONTENT_DETAIL = "cricket_content_details";

        /**
         * *Search API
         */
        String STORY_SEARCH_API = "storySearchAPI";
        String VIDEO_SEARCH_API = "videoSearchAPI";
        String PHOTO_SEARCH_API = "photoSearchAPI";
    }


    public static interface DateKeys {
        String DATE_PATTERN = "MMMMM dd, yyyy hh:mm aaa";
        String NEWS_DATE_FORMAT = "EEE, dd MMM yy HH:mm:ss Z";
    }

    public static interface Tags {
        int NIGHT_TAG = 401;
        int SOUND_TAG = 501;
        int VIBRATION_TAG = 601;
        int ENABLE_NOTIFICATION = 602;
        int INVERT_NOTIFICATION = 301;
        int REFRESH_TAG = 701;
    }

    public interface Seperator {
        String DEFAULT_SECTION_SEPARATOR = "/";
    }

    public interface UrlKeys {
        String URL_LANGUAGE_ID = "@language_id";
        String PAGE_NUMBER = "@page";
        String PAGE_SIZE = "@size";
        String VIDEO_FORMAT = "@video_format";
        String URL_VIDEO_ID = "@video_id";
        String DEEPLINK_PHOTO_ALBUM_ID = "@albumid";
        String DEEPLINK_URL_TAG_ID = "@id";
        String DEEPLINK_CATEGORY_URL_TAG = "@category";


        String COMMENT_FIRTS_NAME = "first_name";
        String COMMENT_LAST_NAME = "last_name";
        String COMMENT_PROFILE_IMAGE = "profile_image";
        String COMMENT_SITE_NAME = "sitename";
        String COMMENT_SUBMIT = "submit";

        String COMMENT_METHOD = "method";
        String COMMENT_AUTH_ID = "user_id";
        String COMMENT_USERID = "uid";
        String COMMENT_PAGE_TITLE = "page_title";
        String COMMENT_PAGE_URL = "page_url";
        String COMMENT_CTYPE = "ctype";
        String COMMENT_IDENTIFIER = "identifier";
        String COMMENT_TEXT = "comment";
        String PARENT_ID = "parent_id";
        String POLL_ID = "@poll_id";
        String COMMENT_KEY = "comment_id";
        String TEAM_ID = "@teamid";
        String MATCH_FILE = "@matchfile";
        String CRICKET_INNINGS = "@innings";

    }

    /**
     * GCM  Notification
     */
    public static interface GCMKeys {
        String BREAKING_NEWS = "breaking_news";
        String GCM_NOTIFICATION_ID = "gcm_notificatiion_id";
        String GCM_STORY_IMAGE_URL = "gcm_story_image_url";
        int ICON_MDPI_SIZE = 40;
        int ICON_HDPI_SIZE = 60;
        int ICON_XHDPI_SIZE = 80;
        int ICON_XXHDPI_SIZE = 120;
        int ICON_XXXHDPI_SIZE = 160;
    }

}
