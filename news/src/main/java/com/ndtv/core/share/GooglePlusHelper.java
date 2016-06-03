/**
 Project      : Awaaz
 Filename     : GooglePlusManager.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.share;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.ndtv.core.R;
import com.ndtv.core.common.util.AsyncTask;

import java.io.IOException;

/**
 * @author veena
 */
public class GooglePlusHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final static String G_PLUS_SCOPE = "oauth2:https://www.googleapis.com/auth/plus.me";
    private final static String USERINFO_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private final static String SCOPES = G_PLUS_SCOPE + " " + USERINFO_SCOPE;
    private static final int GP_RESOLVE_ERR_REQUEST_CODE = 200;

    private ProgressDialog mConnectionProgressDialog;
    private GoogleApiClient mPlusClient;
    private ConnectionResult mConnectionResult;
    private static GooglePlusHelper sInstance;
    private Context mContext;
    public int GOOGLE_PLUS_INTENT = 9999;
    public int GOOGLE_PLUS_ACCOUNT = 8888;
    private boolean isBasicShare = false;

    private static final ConnectionResult CONNECTION_RESULT_SUCCESS = new ConnectionResult(ConnectionResult.SUCCESS,
            null);
    private ShareItem mGpShareItem;
    private GooglePlusListeners mGooglePlusListener;

    /* A flag indicating that a PendingIntent is in progress and prevents
   * us from starting further intents.
   */
    private boolean mIntentInProgress;
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    public static interface GooglePlusListeners {

        void onSignedIn(Person person);

        void onSignedOut();

        void OnTokenAccessed(String token);
    }

    public synchronized static GooglePlusHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new GooglePlusHelper(context);
        }
        return sInstance;
    }

    private GooglePlusHelper(Context context) {
        mContext = context;
        initialise(context);

    }

    /**
     * Initialize mPlusClient with the requested visible activities
     */
    private void initialise(Context context) {
        /*if (mPlusClient == null) {
            mPlusClient = nef(w GoogleA.Builder(context.getApplicationContext(), this, this).setActions(
                    "http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity").build();
		}*/

        if (mPlusClient == null) {
            mPlusClient = new GoogleApiClient.Builder(context.getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();
        }
    }

    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        /*if (requestCode == GP_RESOLVE_ERR_REQUEST_CODE && responseCode == Activity.RESULT_OK) {
            mConnectionResult = null;
			mPlusClient.connect();
		}*/

        if ((requestCode == GP_RESOLVE_ERR_REQUEST_CODE || requestCode == RC_SIGN_IN) && responseCode == Activity.RESULT_OK) {
            mIntentInProgress = false;
            mConnectionResult = null;

            if (!mPlusClient.isConnecting()) {
                mPlusClient.connect();
            }
        }
    }


    public void onStart() {
        if (mPlusClient != null)
            mPlusClient.connect();
    }

    public void onStop() {
        if (mPlusClient != null)
            mPlusClient.disconnect();
    }

    private void connect() {
        if (mPlusClient != null && !mPlusClient.isConnected())
            mPlusClient.connect();
    }

    public void disconnect() {
        if (mPlusClient != null)
            mPlusClient.disconnect();
        mConnectionResult = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mConnectionProgressDialog != null && mConnectionProgressDialog.isShowing()) {
            /**
             * The user clicked the sign-in button already. Start to resolve
             * connection errors. Wait until onConnected() to dismiss the
             * connection dialog.
             */
            if (!mIntentInProgress && result.hasResolution()) {
                try {
//					result.startResolutionForResult((Activity) mContext, GP_RESOLVE_ERR_REQUEST_CODE);
                    mIntentInProgress = true;
                    ((Activity) mContext).startIntentSenderForResult(result.getResolution().getIntentSender(),
                            RC_SIGN_IN, null, 0, 0, 0);
                } catch (SendIntentException e) {
                    mIntentInProgress = false;
                    mPlusClient.connect();
                }
            }
        }
        /**
         * Save the result and resolve the connection failure upon a user click.
         */
        mConnectionResult = result;

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (null != mContext) {
            if (isBasicShare) {
                basicShare((Activity) mContext, mGpShareItem);
            }
            if (mGooglePlusListener != null) {
                mGooglePlusListener.onSignedIn(Plus.PeopleApi.getCurrentPerson(mPlusClient));
            }
            mConnectionResult = CONNECTION_RESULT_SUCCESS;
            if (null != mConnectionProgressDialog)
                mConnectionProgressDialog.dismiss();
//            Toast.makeText(mContext.getApplicationContext(), "User is connected!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(mContext.getApplicationContext(), " is disconnected.", Toast.LENGTH_LONG).show();
    }

   /* @Override
    public void onDisconnected() {
		Toast.makeText(mContext.getApplicationContext(), " is disconnected.", Toast.LENGTH_LONG).show();

	}*/

    /**
     * To comply with the terms of the Google+ developer policies, you must
     * offer users that signed in with Google the ability to disconnect from
     * your application. If the user deletes their account, you must delete the
     * information that your application obtained from the Google APIs.
     */
    private void revokeAndDissConnectApp(final GooglePlusListeners listener) {
        /**
         * Prior to disconnecting, run clearDefaultAccount().
         */
        /*mPlusClient.clearDefaultAccount();
		mPlusClient.revokeAccessAndDisconnect(new OnAccessRevokedListener() {
			@Override
			public void onAccessRevoked(ConnectionResult status) {
				*//**
         * mPlusClient is now disconnected and access has been revoked.
         * Trigger application logic to comply with the developer
         * policies
         *//*
				mConnectionResult = null;
				if (null != listener)
					listener.onSignedOut();

			}
		});*/

        if (mPlusClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mPlusClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mPlusClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    mPlusClient.disconnect();
                    mConnectionResult = null;
                    if (null != listener)
                        listener.onSignedOut();
                }
            });
        }

    }

    /**
     * @return True if connected, otherwise false
     */
    public boolean isConncted() {
        if (null != mPlusClient && mPlusClient.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * Sign out of the application.
     */
    public void signOut(GooglePlusListeners listener) {
        if (mPlusClient.isConnected()) {
            revokeAndDissConnectApp(listener);
        }

    }

    private Intent getBasicShareIntent(Activity activity, ShareItem item) {
        if (!mPlusClient.isConnected() || activity == null)
            return null;
        PlusShare.Builder builder = new PlusShare.Builder(activity);
        if (null != item) {
            Uri parse = null;
            if (!TextUtils.isEmpty(item.contentType))
                builder.setType(item.contentType);
            if (!TextUtils.isEmpty(item.title))
                builder.setText(item.title);
            if (!TextUtils.isEmpty(item.link)) {
                parse = Uri.parse(item.link);
                builder.setContentUrl(parse);
            }

            if (!TextUtils.isEmpty(item.itemID) && !TextUtils.isEmpty(item.itemType)
                    && !TextUtils.isEmpty(item.category)) {

                if (item.link.contains("?")) {

                    builder.setContentDeepLinkId(item.link + "&type=" + item.itemType + "&id=" + item.itemID
                            + "&category=" + Uri.encode(item.category));
                } else {
                    builder.setContentDeepLinkId(item.link + "?type=" + item.itemType + "&id=" + item.itemID
                            + "&category=" + Uri.encode(item.category));
                }
            }

        }

        Intent intent = builder.getIntent();
        return intent;
    }

    public void basicShare(Activity activity, ShareItem item) {
        if (activity != null) {
            isBasicShare = true;
            mContext = activity;
            if (mPlusClient.isConnected()) {
                Intent intent = getBasicShareIntent(activity, item);
                /**
                 *
                 * If the application is not installed, you can prompt the user
                 * to install the application with the
                 * GooglePlayServicesUtil#getErrorDialog utility method.
                 */
                int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
                if (errorCode == ConnectionResult.SUCCESS) {
                    try {
                        activity.startActivityForResult(intent, GOOGLE_PLUS_INTENT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    GooglePlayServicesUtil.getErrorDialog(errorCode, activity, GOOGLE_PLUS_INTENT).show();
                }

            } else {
                mGpShareItem = item;
                signIn(activity);
            }
        }

    }

    public void googlePlusSignIn(Activity activity) {
        isBasicShare = false;
        signIn(activity);
    }

    public void getUserId(Activity activity) {
        Person person;
        if (activity != null) {
            isBasicShare = false;
            if (mPlusClient.isConnected()) {
                person = Plus.PeopleApi.getCurrentPerson(mPlusClient);
                if (mGooglePlusListener != null) {
                    mGooglePlusListener.onSignedIn(person);
                }
                /**
                 *
                 * If the application is not installed, you can prompt the user
                 * to install the application with the
                 * GooglePlayServicesUtil#getErrorDialog utility method.
                 */
                int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
                if (errorCode == ConnectionResult.SUCCESS) {
                    try {
                        activity.startActivityForResult(new Intent(), GOOGLE_PLUS_ACCOUNT);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }

                } else {
                    GooglePlayServicesUtil.getErrorDialog(errorCode, activity, GOOGLE_PLUS_ACCOUNT).show();
                }
            } else {
                signIn(activity);
            }
        }
    }

    private void signIn(Context context) {
        if (mPlusClient != null) {
            if (mConnectionResult == null) {
                mConnectionProgressDialog = new ProgressDialog(context);
                mConnectionProgressDialog.setMessage(mContext.getResources().getString(R.string.signing_in));
                mConnectionProgressDialog.show();
                connect();
            } else {
                try {
                    mConnectionResult.startResolutionForResult((Activity) context, GP_RESOLVE_ERR_REQUEST_CODE);
                } catch (SendIntentException e) {
                    /** Try connecting again. */
                    mConnectionResult = null;
                    connect();
                }
            }
        }

    }

    public void setOnSignedInListener(GooglePlusListeners signedInListener) {
        mGooglePlusListener = signedInListener;
    }

    public void getGplusToken() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String token = null;
                try {
                    token = GoogleAuthUtil.getToken(mContext, Plus.AccountApi.getAccountName(mPlusClient), SCOPES);
                } catch (GooglePlayServicesAvailabilityException playEx) {
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(playEx.getConnectionStatusCode(),
                            (Activity) mContext, 101);
                    /** Use the dialog to present to the user. */
                }
                /**
                 * catch (UserRecoverableAutException recoverableException) {
                 * Intent recoveryIntent = recoverableException.getIntent(); //
                 * Use the intent in a custom dialog or just
                 * startActivityForResult. }
                 */ catch (GoogleAuthException authEx) {
                    /** This is likely unrecoverable. */
                    Log.d("PLUS", "Unrecoverable authentication exception: " + authEx.getMessage().toString());
                } catch (IOException ioEx) {
                    Log.i("PLUS", "transient error encountered: " + ioEx.getMessage());
                    /** doExponentialBackoff(); */
                }
                return token;
            }

            protected void onPostExecute(String accessToken) {
                if (null != mGooglePlusListener) {
                    mGooglePlusListener.OnTokenAccessed(accessToken);
                }
            }

            ;

        }.execute();

    }

    public void clear() {
        sInstance = null;
    }
}
