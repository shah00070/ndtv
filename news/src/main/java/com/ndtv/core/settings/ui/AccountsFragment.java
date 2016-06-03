package com.ndtv.core.settings.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.model.GraphUser;
import com.google.android.gms.plus.model.people.Person;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.SplashAdManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.share.FacebookHelper;
import com.ndtv.core.share.GooglePlusHelper;
import com.ndtv.core.share.TwitterHelper;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import java.text.MessageFormat;

import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by sangeetha on 9/3/15.
 */
public class AccountsFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "SettingsFragment " + makeLogTag(AccountsFragment.class);
    private ImageView mGooglePlusBtn, mTwitterBtn, mFacebookBtn;
    private TextView mAppVersionText;
    private GooglePlusHelper.GooglePlusListeners mGPListener;
    private TwitterHelper.TwitterListeners mTwitterListener;
    private FacebookHelper.OnFaceBookSignedInListener mFacebookListener;
    private TwitterAuthClient mAuthClient;
    private String mSectionTitle;
    private int mNavigationPosition;
    private String navigation;
    private BannerAdFragment.AdListener mAdUpdateListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mAdUpdateListener != null)
            mAdUpdateListener.hideIMBannerAd();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.settings_sign_in, container, false);
        initViews(view);
        extractArguments();
        initGooglePlusListener();
        initFacebookListener();
        initTwitterListener();
        return view;
    }

    private void initViews(View view) {
        mGooglePlusBtn = (ImageView) view.findViewById(R.id.google_plus_btn);
        mGooglePlusBtn.setOnClickListener(this);
        mTwitterBtn = (ImageView) view.findViewById(R.id.twitter_btn);
        mTwitterBtn.setOnClickListener(this);
        mFacebookBtn = (ImageView) view.findViewById(R.id.facebook_btn);
        mFacebookBtn.setOnClickListener(this);
        mAppVersionText = (TextView) view.findViewById(R.id.app_version_text);

        setGooglePlusBtn();
        setFacebookBtn();
        setTwitterBtn();
        setAppVersionName();
    }

    private void extractArguments() {
        if (null != getArguments()) {
            mSectionTitle = getArguments().getString(SettingsFragment.SettingsConstants.SECTION_TITLE);
            mNavigationPosition = getArguments().getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS);
        }
        navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPosition);

    }


    public void setAppVersionName() {

        String versionName = Utility.getVersionName(getActivity().getApplicationContext());
        mAppVersionText.setText(MessageFormat.format(getActivity().getString(R.string.app_version_text), versionName));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthClient != null) {
            mAuthClient.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void setGooglePlusBtn() {
        GooglePlusHelper gPlusHelper = GooglePlusHelper.getInstance(getActivity());
        if (gPlusHelper.isConncted()) {
            mGooglePlusBtn.setBackgroundResource(R.drawable.btn_google_signout);
        } else {
            mGooglePlusBtn.setBackgroundResource(R.drawable.btn_google);
        }
    }


    private void setFacebookBtn() {
        FacebookHelper facebookHelper = FacebookHelper.getInstance(getActivity());
        if (facebookHelper.isLoggedIn()) {
            mFacebookBtn.setBackgroundResource(R.drawable.btn_facebook_signout);
        } else {
            mFacebookBtn.setBackgroundResource(R.drawable.btn_facebook);
        }
    }

    /**
     *
     */
    public void setTwitterBtn() {
        TwitterHelper twitterHelper = TwitterHelper.getInstance(getActivity());
        if (twitterHelper.isLoggedIn()) {
            mTwitterBtn.setBackgroundResource(R.drawable.btn_twitter_signout);
        } else {
            mTwitterBtn.setBackgroundResource(R.drawable.btn_twitter);
        }
    }


    private void initGooglePlusListener() {
        mGPListener = new GooglePlusHelper.GooglePlusListeners() {
            @Override
            public void onSignedIn(Person person) {
                setGooglePlusBtn();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.login_success_msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSignedOut() {
                setGooglePlusBtn();
                GooglePlusHelper gPlusHelper = GooglePlusHelper.getInstance(getActivity());
                gPlusHelper.setOnSignedInListener(null);

            }

            @Override
            public void OnTokenAccessed(String token) {

            }
        };
    }


    private void initFacebookListener() {
        mFacebookListener = new FacebookHelper.OnFaceBookSignedInListener() {
            @Override
            public void onFBSignedIn(GraphUser response) {
                setFacebookBtn();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.login_success_msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFBSignedOut() {
                setFacebookBtn();
                FacebookHelper facebookHelper = FacebookHelper.getInstance(getActivity());
                facebookHelper.setOnFBSignedInListener(null);

            }
        };
    }

    private void initTwitterListener() {
        mTwitterListener = new TwitterHelper.TwitterListeners() {
            @Override
            public void onTwitterLogIn() {
                setTwitterBtn();
            }

            @Override
            public void onTwitterLogOut() {
                setTwitterBtn();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.twitter_logout_msg, Toast.LENGTH_SHORT).show();


                }

            }

            @Override
            public void OnUserInfoReceived(User user) {

            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_plus_btn:
                onGooglePlusClick();
                break;
            case R.id.twitter_btn:
                onTwitterBtnClick();
                break;
            case R.id.facebook_btn:
                onFacebookBtnClick();
                break;
            default:
                break;
        }
    }

    /**
     *
     */
    private void onGooglePlusClick() {
        GooglePlusHelper gPlusHelper = GooglePlusHelper.getInstance(getActivity());
        if (gPlusHelper.isConncted()) {
            gPlusHelper.signOut(mGPListener);
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.google_logout_msg, Toast.LENGTH_SHORT).show();
        } else {
            gPlusHelper.setOnSignedInListener(mGPListener);
            gPlusHelper.googlePlusSignIn(getActivity());
            //to avoid splash ads
            if (SplashAdManager.getSplashAdMngrInstance(getActivity()) != null)
                SplashAdManager.getSplashAdMngrInstance(getActivity()).signInBtnClicked(true);
        }
    }


    private void onFacebookBtnClick() {
        FacebookHelper facebookHelper = FacebookHelper.getInstance(getActivity());
        if (facebookHelper.isLoggedIn()) {
            facebookHelper.setOnFBSignedInListener(mFacebookListener);
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.facebook_logout_msg, Toast.LENGTH_SHORT).show();
            facebookHelper.singOut();
        } else {
            facebookHelper.setOnFBSignedInListener(mFacebookListener);
            if (getActivity() != null)
                Toast.makeText(getActivity(), getActivity().getString(R.string.signing_in), Toast.LENGTH_SHORT).show();
            facebookHelper.signIn();
            //to avoid splash ads
            if (SplashAdManager.getSplashAdMngrInstance(getActivity()) != null)
                SplashAdManager.getSplashAdMngrInstance(getActivity()).signInBtnClicked(true);
        }
    }

    private void onTwitterBtnClick() {
        TwitterHelper twitterHelper = TwitterHelper.getInstance(getActivity());
        if (twitterHelper.isLoggedIn())
            twitterHelper.signOutTwitter(mTwitterListener);
        else {

            login();
            //to avoid splash ads
            if (SplashAdManager.getSplashAdMngrInstance(getActivity()) != null) {
                SplashAdManager.getSplashAdMngrInstance(getActivity()).signInBtnClicked(true);
            }
        }

    }


    public void login() {
        mAuthClient = new TwitterAuthClient();
        mAuthClient.authorize(getActivity(), new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {

                setTwitterBtn();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.login_success_msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException e) {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.login_failure_msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void setScreenName() {
        setScreenName(navigation + " - " + mSectionTitle);
    }
}