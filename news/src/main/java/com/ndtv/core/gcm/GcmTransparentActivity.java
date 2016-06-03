package com.ndtv.core.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import com.ndtv.core.R;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.share.ShareItem;

import java.util.ArrayList;
import java.util.List;

public class GcmTransparentActivity extends Activity {

    private String mBreakingNews;
    private int RQ_GCM = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exractIntents();

        launchShareforForAlert();
    }

    private void exractIntents() {
        if (getIntent() != null) {
            mBreakingNews = getIntent().getStringExtra(ApplicationConstants.GCMKeys.BREAKING_NEWS);


            int notificationId = getIntent().getIntExtra(ApplicationConstants.GCMKeys.GCM_NOTIFICATION_ID, 0);
            cancelAndSaveNotificationId(notificationId);
            ConfigManager.getInstance().setPushNewsMessageText(mBreakingNews);
        }
    }


    private void cancelAndSaveNotificationId(int id) {
        Utility.cancelNotification(id, this);
        if (PreferencesManager.getInstance(this) != null)
            PreferencesManager.getInstance(this).saveCurrentNotificationId(id);
    }

    public void launchShareforForAlert() {
        String sharemsg = ConfigManager.getInstance().getPushNewsMessageText();
        ShareItem item = new ShareItem();
        item.title = sharemsg;
        item.link = "";
        startShareItem(item);
    }

    private void startShareItem(ShareItem item) {
        String shareViaNdtv = getResources().getString(R.string.shared_via_ndtv);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));

        emailIntent.putExtra(Intent.EXTRA_TEXT, item.link + "\n\n" + shareViaNdtv);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, item.title);

        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);

        sendIntent.setType("text/plain");

        Intent openInChooser = Intent.createChooser(emailIntent, "Complete action using");

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;

            if (packageName.contains("twitter") || packageName.contains("whatsapp") || packageName.contains("plus") || packageName.contains("bluetooth") || packageName.contains("talk") || packageName.contains("skype") || packageName.contains("mms")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);

                intent.setType("text/plain");

                if (packageName.contains("twitter")) {
                    intent.putExtra(Intent.EXTRA_TEXT, item.title);

                } else if (packageName.contains("facebook")) {
                    intent.putExtra(Intent.EXTRA_TEXT, item.title);

                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, item.title + ".\n\n" + shareViaNdtv);

                }


                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }


        }


        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivityForResult(openInChooser, RQ_GCM);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        this.finish();
    }
}