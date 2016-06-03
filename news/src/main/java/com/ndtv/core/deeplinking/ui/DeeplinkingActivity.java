package com.ndtv.core.deeplinking.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.BuildConfig;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.FragmentHelper;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.util.URLUtility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Configuration;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.cricket.ui.WebViewFragment;
import com.ndtv.core.deeplinking.io.DeepLinkingManager;
import com.ndtv.core.newswidget.ui.DeeoLinkingPhotoFragment;
import com.ndtv.core.newswidget.ui.custom.DeeplinkingVideoDetailFragment;
import com.ndtv.core.ui.widgets.BaseFragment;

import java.text.MessageFormat;

import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by veena on 23/2/15.
 *
 * @modified Sangeetha
 */
public class DeeplinkingActivity extends FragmentActivity implements ApplicationConstants.SectionType, ApplicationConstants.BundleKeys, ApplicationConstants.CustomApiType, ApplicationConstants.UrlKeys, BaseFragment.OnAddDetailFragmentListener, BannerAdFragment.AdListener {

    private static final String DEEPLINK_ID = "deep_link_id";
    private static final String DEEPLINK_TYPE = "type";
    private static final String DEEP_LINK_ID_VAL = "id";
    private static final String DEEP_LINK_CATEGORY = "category";
    private static final String NEWS_TYPE_TEXT = "News";
    private static final String NDTV_TWITTER_SCHEME = "android-app";
    private static final String FACEBOOK_SCHEME = "ndtv";

    public static final String FACEBOOK_ACTION = "com.facebook.application.202684249798785";

    public static final String IS_GOOGLE_SEARCH_INDEXING = "is_google_search_indexing";
    public static final String DEEPLINK_GOOGLE_SEARCH_TYPE = "type";
    public static final String DEEPLINK_GOOGLE_SEARCH_ID = "id";
    public static final String DEEPLINK_GOOGLE_SEARCH_CATEGORY = "category";

    private static final String TAG = makeLogTag(DeeplinkingActivity.class);

    private String mType;
    private String mId;
    private String mCategory;

    private TextView mCategoryTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deeplinking_layout);
        extractIntentData(getIntent());
        initViews();
        getConfigData();
    }


    private void extractIntentData(Intent intent) {
        //  Intent intent = getIntent();
        if(null != intent && null != intent.getExtras() && intent.getExtras().getBoolean(IS_GOOGLE_SEARCH_INDEXING, false))
            getIntentParameters(intent);
        if (intent != null && intent.getAction() != null) {
            //if it is from google plus deep link
            if (intent.getAction().equalsIgnoreCase("com.google.android.apps.plus.VIEW_DEEP_LINK")) {
                getIntentParameters(intent);
            }//if it is from facebook deeplinking
            else if (intent.getAction().equalsIgnoreCase(FACEBOOK_ACTION)) {
                showNewsWebPage(intent.getData().toString());
            } else {//for twitter deep link
                Uri uri = intent.getData();
                if (null != uri) {
                    String scheme = uri.getScheme();
                    if (scheme != null && (scheme.equalsIgnoreCase(NDTV_TWITTER_SCHEME) || scheme.equalsIgnoreCase(FACEBOOK_SCHEME))) {
                        extractTwitterOrFacebookIntentData(uri.getPath());
                    }
                }
            }
        }
    }

    private void extractTwitterOrFacebookIntentData(String data) {
        String[] urlData = data.split("type=");
        if (urlData.length > 1) {
            data = urlData[1];
            urlData = data.split("/");
            mType = urlData[0];
            mId = urlData[1].split("id=")[1];
            if (urlData.length > 2)
                mCategory = urlData[2].split("category=")[1];
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        extractIntentData(getIntent());
        initViews();
        getConfigData();
    }

    private void getConfigData() {
        if (PreferencesManager.getInstance(DeeplinkingActivity.this) != null) {
            if (PreferencesManager.getInstance(DeeplinkingActivity.this).getConfig() != null) {
                handleCategoryLaunch();
            } else
                downlaodConfig();
        }

    }

    private void downlaodConfig() {
        DeepLinkingManager.getInstance().downloadConfig(DeeplinkingActivity.this, getConfigUrl(), new Response.Listener<Configuration>() {
            @Override
            public void onResponse(Configuration configuration) {
                ConfigManager.getInstance().setConfiguration(configuration);
                saveConfig(configuration);
                handleCategoryLaunch();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Config file Download Failed " + volleyError.getMessage());
            }
        });
    }

    private void saveConfig(Configuration c) {
        if (PreferencesManager.getInstance(this) != null)
            PreferencesManager.getInstance(this).saveConfig(c);
    }

    private String getConfigUrl() {
        if (BuildConfig.DEBUG) {
            return getString(R.string.config_url_debug);
        } else {
            return getString(R.string.config_url);
        }
    }

    private void initViews() {
//        mNavigationLayout = (RelativeLayout) findViewById(R.id.navigation_layout);
//        mHeaderLayout = (RelativeLayout) findViewById(R.id.header_layout);
//        mArrowLinearLayout = (LinearLayout) findViewById(R.id.arrow_lin_lay);

//        mHeaderRightArrow = (ImageView) findViewById(R.id.header_right_arrow);
//        mHeaderLeftArrow = (ImageView) findViewById(R.id.header_left_arrow);
        mCategoryTV = (TextView) findViewById(R.id.category_tv);
    }


    private void getIntentParameters(Intent intent) {
        if (null != intent.getExtras()) {
            boolean isGoogleSearchIndexing = intent.getExtras().getBoolean(IS_GOOGLE_SEARCH_INDEXING, false);
            if (isGoogleSearchIndexing) {
                mType = intent.getExtras().getString(DEEPLINK_GOOGLE_SEARCH_TYPE);
                mId = intent.getExtras().getString(DEEPLINK_GOOGLE_SEARCH_ID);
                mCategory = intent.getExtras().getString(DEEPLINK_GOOGLE_SEARCH_CATEGORY);
            } else {
                Uri data = intent.getData();
                String query = data.getQueryParameter(DEEPLINK_ID);
                Uri deepLinkQueryURI = Uri.parse(query);
                mType = deepLinkQueryURI.getQueryParameter(DEEPLINK_TYPE);
                mId = deepLinkQueryURI.getQueryParameter(DEEP_LINK_ID_VAL);
                mCategory = deepLinkQueryURI.getQueryParameter(DEEP_LINK_CATEGORY);
            }
        }
    }


    private void handleCategoryLaunch() {
        if (!TextUtils.isEmpty(mId) && !TextUtils.isEmpty(mType)) {
            if (NEWS.equalsIgnoreCase(mType)) {
                mCategoryTV.setVisibility(View.VISIBLE);
                mCategoryTV.setText(MessageFormat.format("{0} - {1}", NEWS_TYPE_TEXT, getAlteredCategoryName()));
                launchNewsDetailFragment();
            } else if (PHOTO.equalsIgnoreCase(mType)) {
                mCategoryTV.setVisibility(View.VISIBLE);
                launchPhotosDetailFragment();
            } else if (VIDEO.equalsIgnoreCase(mType)) {
                mCategoryTV.setVisibility(View.VISIBLE);
                if(!TextUtils.isEmpty(mCategory))
                    mCategoryTV.setText(mCategory);
                else
                    mCategoryTV.setText("Videos");
                launchVideoDetailFragment();
            }
        }
    }


    public String getAlteredCategoryName() {

        // This is done as per request by client
        // Mail details - Wed, 4 Jun 2014

        String formattedString = "";
        String categoryText = mCategory;
        boolean isStringFormatted = true;

        if (!TextUtils.isEmpty(categoryText)) {

            if (categoryText.contains("_")) {
                categoryText = categoryText.replace("_", " ");
                isStringFormatted = false;
            }

            if (categoryText.contains("-")) {
                categoryText = categoryText.replace("-", " ");
                isStringFormatted = false;
            }

            if (!isStringFormatted) {
                String splitString[] = categoryText.split(" ");

                for (int i = 0; i < splitString.length; i++) {

                    if (!TextUtils.isEmpty(splitString[i])) {
                        char capLetter = Character.toUpperCase(splitString[i].charAt(0));

                        if (splitString[i].length() > 1) {
                            formattedString += " " + capLetter + splitString[i].substring(1, splitString[i].length());
                        }
                    }
                }

                if (!TextUtils.isEmpty(formattedString)) {
                    return formattedString;
                } else {
                    return "";
                }
            } else {

                String catString = "";
                // Just to make sure string is not of length 0
                if (categoryText.length() != 0) {
                    catString = categoryText.substring(0, 1).toUpperCase();
                }
                // Make sure that string is more than a single character
                if (categoryText.length() > 1) {
                    catString = catString + categoryText.substring(1, categoryText.length());
                }

                return catString;
            }

        } else {
            return " ";
        }

    }

    /**
     * ****************************************News*****************************************
     */
    private void launchNewsDetailFragment() {
        String newsDetailsAPI = null;
        Configuration config = PreferencesManager.getInstance(DeeplinkingActivity.this).getConfig();
        if (config != null) {
            ConfigManager cm = ConfigManager.getInstance();
            cm.setConfiguration(config);
            newsDetailsAPI = cm.getCustomApiUrl(NEWS_DETAIL_CUSTOM_API);
        }
        if (newsDetailsAPI != null) {
            // Category is replaced by type (i.e news) the category is not
            // considered in url , any character will do
            String strToReplaceEpisode[] = new String[]{DEEPLINK_CATEGORY_URL_TAG, DEEPLINK_URL_TAG_ID};
            String replacementEpisode[] = new String[]{mCategory, mId};

            newsDetailsAPI = URLUtility.getFinalUrl(strToReplaceEpisode, replacementEpisode, newsDetailsAPI, this);
            showNewsWebPage(newsDetailsAPI);
        }
    }

    private void showNewsWebPage(String newsDetailsAPI) {
        WebViewFragment newsDetail = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DEEP_LINK_URL, newsDetailsAPI);
        bundle.putBoolean(IS_DEEPLINK_URL, true);
        newsDetail.setArguments(bundle);
        FragmentHelper.replaceFragment(this, R.id.deep_link_frame_body, newsDetail);
    }

    /**
     * ****************************************News End*****************************************
     */

    /**
     * ****************************************Photos*****************************************
     */
    private void launchPhotosDetailFragment() {
        String photoDetailApi = null;
        Configuration config = PreferencesManager.getInstance(DeeplinkingActivity.this).getConfig();
        if (config != null) {
            ConfigManager cm = ConfigManager.getInstance();
            cm.setConfiguration(config);
            photoDetailApi = cm.getCustomApiUrl(PHOTO_DETAIL_API);
        }
        if (photoDetailApi != null) {
            String strToReplaceEpisode[] = new String[]{DEEPLINK_PHOTO_ALBUM_ID};
            String replacementEpisode[] = new String[]{mId};

            photoDetailApi = URLUtility.getFinalUrl(strToReplaceEpisode, replacementEpisode, photoDetailApi, this);

            DeeoLinkingPhotoFragment photoDetail = new DeeoLinkingPhotoFragment();
            Bundle bundle = new Bundle();
            bundle.putString(DEEP_LINK_URL, photoDetailApi);
            bundle.putBoolean(IS_DEEPLINK_URL, true);
            photoDetail.setArguments(bundle);
            FragmentHelper.replaceFragment(this, R.id.deep_link_frame_body, photoDetail);
            mCategoryTV.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(mCategory))
                mCategoryTV.setText(mCategory);
            else
                mCategoryTV.setText("Photos");
            if (DeepLinkingManager.getInstance() != null)
                DeepLinkingManager.getInstance().setFromDeepLink(true);

        }
    }

    /**
     * ****************************************Photos End*****************************************
     */


    /**
     * ****************************************Videos*****************************************
     */
    private void launchVideoDetailFragment() {
        String videoDetailAPI = null;
        Configuration config = PreferencesManager.getInstance(DeeplinkingActivity.this).getConfig();
        if (config != null) {
            ConfigManager cm = ConfigManager.getInstance();
            cm.setConfiguration(config);
            videoDetailAPI = cm.getCustomApiUrl(VIDEO_DETAIL_API);
        }
        if (videoDetailAPI != null) {
            String strToReplaceEpisode[] = new String[]{URL_VIDEO_ID};
            String replacementEpisode[] = new String[]{mId};
            videoDetailAPI = URLUtility.getFinalUrl(strToReplaceEpisode, replacementEpisode, videoDetailAPI, this);
            videoDetailAPI = URLUtility.getVideoEncodedUrl(videoDetailAPI);
            replaceVideoDetailFragment(videoDetailAPI);
        }

    }

    private void replaceVideoDetailFragment(String videoDetailAPI) {
        DeeplinkingVideoDetailFragment videoDetailFragment = new DeeplinkingVideoDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_DEEPLINK_URL, true);
        bundle.putString(DEEP_LINK_URL, videoDetailAPI);
        videoDetailFragment.setArguments(bundle);
        FragmentHelper.replaceFragment(this, R.id.deep_link_frame_body, videoDetailFragment);
    }

    /**
     * ****************************************Videos End*****************************************
     */


    //Implemented to avoid the null pointer exception caused by the listeners
    @Override
    public void onAddDetailFragment(Fragment fragment, String tag) {

    }

    @Override
    public void loadBannerAd(int navigationPos, int sectionPos, String contentUrl,
                             boolean isPhotos, int photoIndex, final boolean isLiveTv, final boolean isVideo) {

    }

    @Override
    public void hideIMBannerAd() {

    }

    @Override
    public void showIMBannerAd(boolean mIsPhotos, final boolean isLiveTv, final boolean isVideo) {

    }

    public void hideCategoryView(){
        if(mCategoryTV != null)
            mCategoryTV.setVisibility(View.INVISIBLE);
    }

}
