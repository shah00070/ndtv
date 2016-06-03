/**
   Project      : Awaaz
   Filename     : GoogleSearchIndexingActivity.java
   Author       : praveenk
   Comments     : 
   Copyright    : © Copyright NDTV Convergence Limited 2011
					Developed under contract by Robosoft Technologies
   History      : NA
 */

package com.ndtv.core.deeplinking.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.AsyncTask;
import com.ndtv.core.common.util.FragmentHelper;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.cricket.ui.WebViewFragment;
import com.ndtv.core.deeplinking.Constants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author veena
 * 
 */
public class GoogleSearchIndexingActivity extends FragmentActivity implements Constants.BundleKeys, BannerAdFragment.AdListener{

    private static final String DEEPLINK_Type = "type";
	private static final String DEEPLINK_ID = "id";
	private static final String DEEPLINK_CATEGORY = "category";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
        setContentView(R.layout.deeplinking_layout);

		Intent intent = getIntent();

		if (null != intent) {
			final String data = intent.getDataString();
			if (null != data) {
                if(intent.getScheme().equalsIgnoreCase("ndtv")){
                    launchDeeplinking(data);
                }
			}
		}
	}

    private void launchDeeplinking(String deeplink) {
        if (deeplink.contains("/")) {
            String[] split = deeplink.split("/");

            Map<String, String> myMap = new HashMap<String, String>();

            for (int i = 0; i < split.length; i++) {
                if (!TextUtils.isEmpty(split[i])) {

                    if (split[i].contains("=")) {
                        String[] keyAndValue = split[i].split("=");
                        myMap.put(keyAndValue[0], keyAndValue[1]);
                    }
                }
            }

            String type = myMap.get(DEEPLINK_Type);
            String id = myMap.get(DEEPLINK_ID);
            String category = myMap.get(DEEPLINK_CATEGORY);

            if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(id)) {

                Intent deepLinkActivity = new Intent(this,
                        DeeplinkingActivity.class);
                deepLinkActivity.putExtra(IS_GOOGLE_SEARCH_INDEXING, true);
                deepLinkActivity.putExtra(DEEPLINK_GOOGLE_SEARCH_TYPE, type);
                deepLinkActivity.putExtra(DEEPLINK_GOOGLE_SEARCH_ID, id);
                deepLinkActivity.putExtra(DEEPLINK_GOOGLE_SEARCH_CATEGORY, category);
                startActivity(deepLinkActivity);
                finish();
            }
        }
    }

    private void showNewsWebPage(String newsDetailsAPI) {
        WebViewFragment newsDetail = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ApplicationConstants.BundleKeys.DEEP_LINK_URL, newsDetailsAPI);
        bundle.putBoolean(ApplicationConstants.BundleKeys.IS_DEEPLINK_URL, true);
        newsDetail.setArguments(bundle);
        FragmentHelper.replaceFragment(this, R.id.deep_link_frame_body, newsDetail);
    }

    //Cast ad listener when search results are redirected to WAP(WebViewFragment)
    @Override
    public void loadBannerAd(int navigationPos, int sectionPos, String contentUrl, boolean isPhotos, int phptoIndex, final boolean isLiveTv, final boolean isVideo) {

    }

    @Override
    public void hideIMBannerAd() {

    }

    @Override
    public void showIMBannerAd(boolean mIsPhotos, final boolean isLiveTv, final boolean isVideo) {

    }

    public interface GetDeeplinkListener{
        void onDeeplinkRetrieved(String deeplink);
    }

    /*public void getUrl(String path, final GetDeeplinkListener listener) {

        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    //Document doc = Jsoup.connect("http://www.ndtv.com/india-news/aaps-mayank-gandhi-questions-manner-of-removing-yogendra-yadav-prashant-bhushan-744468?pfrom=home-lateststories").userAgent(uaa).get();

                    String uaa = USER_AGENT_ANDROID;
                    String uam = USER_AGENT_GENERAL;
                    Document doc = Jsoup.connect(params[0]).get();
                    Elements metaElems = doc.head().getElementsByAttribute(PROPERTY);

                    for (Element metaElem : metaElems) {
                        String name = metaElem.attr(PROPERTY);
                        String content = metaElem.attr(CONTENT);
                        if(name.equalsIgnoreCase(AL_ANDROID_URL))
                            return content;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                Toast.makeText(GoogleSearchIndexingActivity.this, "Content:" + s, Toast.LENGTH_SHORT).show();
                listener.onDeeplinkRetrieved(s);
            }
        }.execute(path);


    }*/


    /*else {
                    getUrl(data, new GetDeeplinkListener() {
                        @Override
                        public void onDeeplinkRetrieved(String deeplink) {
                            if (!TextUtils.isEmpty(deeplink)) {
                                launchDeeplinking(deeplink);
                            } else {
                                showNewsWebPage(data);
                            }
                        }
                    });
                }*/


}
