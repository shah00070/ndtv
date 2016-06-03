package com.ndtv.core.share;

import android.content.Context;

import com.ndtv.core.constants.ApplicationConstants;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

/**
 * Created by sangeetha on 10/3/15.
 */
public class TwitterHelper implements ApplicationConstants.SocialShare {
    private static TwitterHelper sInstance;
    private Context mContext;
    //This is to avoid Splash Ads, after authorizing the user
    private boolean mIsFromTwitter = false;

    public synchronized static TwitterHelper getInstance(Context context) {
        if (sInstance == null)
            sInstance = new TwitterHelper(context);
        return sInstance;

    }

    private TwitterHelper(Context ctx) {
        mContext = ctx;
    }

    public static interface TwitterListeners {
        void onTwitterLogIn();

        void onTwitterLogOut();

        void OnUserInfoReceived(User user);

    }

    public TwitterAuthToken getTwitterAuthToken() {
        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();
        if (session != null)
            //user has logged in
            return session.getAuthToken();
        else
            //user is not logged in
            return null;

    }

    public boolean isLoggedIn() {
        TwitterAuthToken authToken = getTwitterAuthToken();
        if (authToken != null) {
            String token = authToken.token;
            String secret = authToken.secret;
            if (token != null && secret != null)
                return true;
        }
        return false;
    }

    public void signOutTwitter(TwitterListeners twitterListener) {
        if (isLoggedIn()) {
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
            twitterListener.onTwitterLogOut();
        }
    }


}
