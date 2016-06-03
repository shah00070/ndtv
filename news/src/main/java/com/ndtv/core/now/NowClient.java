package com.ndtv.core.now;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.ndtv.core.R;

import java.util.HashMap;
import java.util.regex.Pattern;

import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Srihari S Reddy on 18/06/15.
 */
public class NowClient {

    private static final String TAG = makeLogTag(NowClient.class);
    private Context context;
    private static NowClient nowClient=null;

    private NowClient(Context ctx) {
        context = ctx;
    }

    public static NowClient getInstance(Context ctx) {
        if(nowClient==null) nowClient=new NowClient(ctx);
        return nowClient;
    }

    public void init(String nowID) {
        Constants.USER_ID=nowID;
        //addCredentails();
        forceCheckCredentails();
    }

    private void addCredentails() {
        Intent checkCredentialsIntent = createCheckUserCredentialsIntent(Constants.USER_ID);
        Resources res = context.getResources();
        String text = String.format(res.getString(R.string.signing_in), Constants.USER_ID);
        Log.d(TAG, text);
        createAlarm(checkCredentialsIntent);
    }

    private Intent createCheckUserCredentialsIntent(String user) {
        Bundle bundle = new Bundle();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.USER_PARAM, user);
        bundle.putSerializable(Constants.PARAMS_EXTRA, params);

        Intent checkCredentialsIntent = new Intent(context, HttpPostService.class);
        checkCredentialsIntent.putExtra(Constants.METHOD_EXTRA,
                Constants.CHECK_CREDENTIALS_URL);
        checkCredentialsIntent.putExtra(Constants.USER_PARAM, user);
        checkCredentialsIntent.putExtras(bundle);

        return checkCredentialsIntent;
    }

    private void createAlarm(Intent checkCredentialsIntent) {
        // Checks if we have already set up the alarm previously, using
        // PendingIntent.FLAG_NO_CREATE to ensure we don't create duplicates.
        PendingIntent pendingIntent = PendingIntent.getService(context,
                Constants.ALARM_SERVICE_REQUEST_CODE, checkCredentialsIntent,
                PendingIntent.FLAG_NO_CREATE);
        boolean isAlarmSet = pendingIntent != null;

        // Cancel the alarm if it already exists
        if (isAlarmSet) {
            pendingIntent.cancel();
            Log.d(TAG, "Removing alarm.");
           // Toast.makeText(context, "Alarm Removed", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "Setting new alarm.");
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        PendingIntent pendingAlarmIntent = PendingIntent.getService(context,
                Constants.ALARM_SERVICE_REQUEST_CODE, checkCredentialsIntent, 0);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                0, AlarmManager.INTERVAL_DAY, pendingAlarmIntent);
       // Toast.makeText(context, "An alarm is set to check credentials on a daily basis", Toast.LENGTH_SHORT).show();
    }

    private void forceCheckCredentails() {
        Intent checkCredentialsIntent = createCheckUserCredentialsIntent(Constants.USER_ID);
        ComponentName componentName = context.startService(checkCredentialsIntent);
        Log.d(TAG, "startService Component Name : " + componentName.flattenToString());
        // Toast.makeText(context, "Successfully started credential check service", Toast.LENGTH_SHORT).show();
    }

    private void removeAlarams() {
        Intent checkCredentialsIntent = createCheckUserCredentialsIntent(Constants.USER_ID);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                Constants.ALARM_SERVICE_REQUEST_CODE, checkCredentialsIntent,
                PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            pendingIntent.cancel();
            Log.d(TAG, "Removing alarm.");
          //  Toast.makeText(context,  "Alarm Removed", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "No alarm to remove");
           // Toast.makeText(context, "No alarm to remove.", Toast.LENGTH_SHORT).show();
        }
    }

    public void ForceCheckDelay(int interval){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                forceCheckCredentails();
            }
        }, interval);

    }

    public String getAccountID(){
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {

            if (emailPattern.matcher(account.name).matches()) {
                return account.name;

            }
        }
        return "";
    }

}
