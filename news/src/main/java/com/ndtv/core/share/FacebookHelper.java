/**
 * Project      : Awaaz
 * Filename     : FacebookHelper.java
 * Author       : nagaraj
 * Comments     :
 * Copyright    : © Copyright NDTV Convergence Limited 2011
 * Developed under contract by Robosoft Technologies
 * History      : NA
 */

package com.ndtv.core.share;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.ndtv.core.R;
import com.ndtv.core.config.model.Section;
import com.ndtv.core.settings.ui.AccountsFragment;

public class FacebookHelper {
    private static final String PUBLISH_PERMISSION = "publish_actions";
    private UiLifecycleHelper uiHelper;
    private static FacebookHelper sInstance;
    private Activity mCtx;
    private ShareItem mShareItem;
    private OnFaceBookSignedInListener mFBlistener;

    private FacebookHelper(Activity ctx) {
        mCtx = ctx;
    }

    public synchronized static FacebookHelper getInstance(Activity ctx) {
        if (sInstance == null)
            sInstance = new FacebookHelper(ctx);
        return sInstance;
    }

    public static interface OnFaceBookSignedInListener {

        void onFBSignedIn(GraphUser response);

        void onFBSignedOut();
    }

    private Session.StatusCallback mCallback = new Session.StatusCallback() {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);

        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (exception != null && exception instanceof FacebookOperationCanceledException)
            return;
        if (state.isOpened()) {
            if (mShareItem != null) {
                shareOnFacebook(mShareItem, mCtx);
            }
            fetchUserInfo();
        }
    }

    public void oncreate(Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(mCtx, mCallback);
        uiHelper.onCreate(savedInstanceState);

    }

    public void onResume() {
        //added code as mentioned in the Developer site
       /*Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }*/
        //added code ends
        uiHelper.onResume();
    }

    public void onSaveInstanceState(Bundle outState) {
        uiHelper.onSaveInstanceState(outState);
    }

    public void onPause() {
        uiHelper.onPause();
    }


    public void onDestroy() {
        uiHelper.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                mShareItem = null;
                Toast.makeText(mCtx.getApplicationContext(), mCtx.getString(R.string.post_error), Toast.LENGTH_LONG)
                        .show();

            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                /**
                 *       For Signing in Accounts Fragment
                 */
                fetchUserInfo();

                mShareItem = null;
                if (FacebookDialog.getNativeDialogPostId(data) == null)
                    Toast.makeText(mCtx.getApplicationContext(), mCtx.getString(R.string.post_cancel),
                            Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(mCtx.getApplicationContext(), mCtx.getString(R.string.post_sucess),
                            Toast.LENGTH_LONG).show();
            }
        });

    }

    public boolean isLoggedIn() {
        if (isSessionOpened())
            return true;
        else
            return false;
    }

    public void shareOnFacebook(ShareItem item, Activity ctx) {
        mCtx = ctx;
        mShareItem = item;
        if (isSessionOpened()) {
            publish(item);
        } else {
            Login();
        }
    }

    public void getuserInfo() {
        if (isSessionOpened()) {
            fetchUserInfo();
        } else {
            Login();
        }
    }

    public void singOut() {
        Session session = Session.getActiveSession();
        if (session != null) {
            if (!session.isClosed())
                session.closeAndClearTokenInformation();
            mFBlistener.onFBSignedOut();
        }

    }

    /**
     *
     */
    private void fetchUserInfo() {
        Request request = Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {

            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    if (mFBlistener != null)
                        mFBlistener.onFBSignedIn(user);
                }
            }
        });
        Request.executeBatchAsync(request);
    }

    private void publish(ShareItem item) {
        Session session = Session.getActiveSession();
        if (hasPublishPermission()) {
            if (isFacebookAppInstatlled()) {
                FacebookDialog shareDialog = createShareDialogBuilder(item).build();
                uiHelper.trackPendingDialogCall(shareDialog.present());
                return;
            } else {
                publishFeedDialog(item);
            }
        } else if (session != null && session.isOpened()) {
            /**
             * We need to get new permissions, then complete the action when we
             * get called back.
             */
            session.requestNewPublishPermissions(new Session.NewPermissionsRequest(mCtx, PUBLISH_PERMISSION));
        }
    }

    private boolean isFacebookAppInstatlled() {
        return FacebookDialog.canPresentShareDialog(mCtx, FacebookDialog.ShareDialogFeature.SHARE_DIALOG);

    }

    public void signIn() {
        getuserInfo();
    }

    private void Login() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(mCtx).setCallback(mCallback));
        } else {
            Session.openActiveSession(mCtx, true, mCallback);
        }
    }

    private boolean isSessionOpened() {
        Session session = Session.getActiveSession();
        return session.isOpened();
    }

    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains(PUBLISH_PERMISSION);
    }

    public String getAccessTocken() {
        Session session = Session.getActiveSession();
        if (session.isOpened()) {
            return session.getAccessToken();
        } else {
            return null;
        }
    }

    private FacebookDialog.ShareDialogBuilder createShareDialogBuilder(ShareItem item) {
        return new FacebookDialog.ShareDialogBuilder(mCtx).setDescription(item.title).setLink(item.link);
    }

    private void publishFeedDialog(ShareItem item) {
        if (null != mCtx) {
            Bundle params = new Bundle();
            if (null != item) {
                if (!TextUtils.isEmpty(item.title))
                    params.putString("name", item.title);
                if (!TextUtils.isEmpty(item.link))
                    params.putString("link", item.link);
            }
            WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(mCtx, Session.getActiveSession(), params))
                    .setOnCompleteListener(new OnCompleteListener() {

                        @Override
                        public void onComplete(Bundle values, FacebookException error) {
                            mShareItem = null;
                            if (error == null) {
                                final String postId = values.getString("post_id");
                                if (postId != null) {
                                    Toast.makeText(mCtx.getApplicationContext(), mCtx.getString(R.string.post_sucess),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    /** User clicked the Cancel button */
                                    Toast.makeText(mCtx.getApplicationContext(), mCtx.getString(R.string.post_cancel),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else if (error instanceof FacebookOperationCanceledException) {
                                /** User clicked the "x" button */
                                Toast.makeText(mCtx.getApplicationContext(), mCtx.getString(R.string.post_cancel),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                /** Generic, ex: network error */
                                Toast.makeText(mCtx.getApplicationContext(), mCtx.getString(R.string.post_error),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                    }).build();
            feedDialog.show();
        }
    }

    public void setOnFBSignedInListener(OnFaceBookSignedInListener signedInListener) {
        mFBlistener = signedInListener;
    }

    public void clear() {
        sInstance = null;
    }

}
