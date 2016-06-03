package com.ndtv.core.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.ndtv.core.R;
import com.ndtv.core.common.util.ui.ShareAdapterNew;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.share.ShareApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by laveen on 3/3/15.
 */
public class UiUtil implements ApplicationConstants.SocialShare {

    //This method only creates the  popup window with list of sharing app.Showing this popupwindow at any location is depends on the caller.
    //don't put any showing popup related code here i.e popupwindow.showAtLocation or popupwindow.showAsDropDown
    public static PopupWindow createSharePopup(Context context, AdapterView.OnItemClickListener listener, boolean isFullList) {
        PopupWindow popupWindow;

        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.layout_share_popup, null);

        GridView listView = (GridView) popupView.findViewById(R.id.options_list);
        // ShareAdapter adapter = new ShareAdapter(context, getResolveInfoList(context));
        ShareAdapterNew adapter;
        if (isFullList) {
            adapter = new ShareAdapterNew(context, getResolveInfoList(context));
        } else {
            adapter = new ShareAdapterNew(context, getCustomShareList(context));
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);

        final int width = context.getResources().getDimensionPixelSize(R.dimen.attachment_pop_up_width);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // mShareBtn.setOnClickListener(BaseActivity.this);
            }
        });

        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources()));
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        popupWindow.showAtLocation(popupWindow.getContentView(), Gravity.CENTER, 0, 0);

        return popupWindow;
    }


    public static List<ShareApp> getResolveInfoList(Context context) {
        PackageManager packageMngr = context.getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(ApplicationConstants.SocialShare.MIME_DATA_TYPE);
        // NOTE: Provide some data to help the Intent resolver
        List<ResolveInfo> resolveInfoList = packageMngr.queryIntentActivities(intent, 0);
        Collections.sort(resolveInfoList, new Comparator<ResolveInfo>() {

            @Override
            public int compare(ResolveInfo lhs, ResolveInfo rhs) {
                if (lhs.activityInfo.packageName.equalsIgnoreCase(ApplicationConstants.SocialShare.FACEBOOK_PKG_NAME))
                    return -1;
                else if (rhs.activityInfo.packageName.equalsIgnoreCase(ApplicationConstants.SocialShare.FACEBOOK_PKG_NAME))
                    return 1;
                else if (lhs.activityInfo.packageName.equalsIgnoreCase(ApplicationConstants.SocialShare.TWITTER_PKG_NAME))
                    return -1;
                else if (rhs.activityInfo.packageName.equalsIgnoreCase(ApplicationConstants.SocialShare.TWITTER_PKG_NAME))
                    return 1;
                else if (lhs.activityInfo.packageName.equalsIgnoreCase(ApplicationConstants.SocialShare.GOOGLE_PLUS_PKG_NAME))
                    return -1;
                else if (rhs.activityInfo.packageName.equalsIgnoreCase(ApplicationConstants.SocialShare.GOOGLE_PLUS_PKG_NAME))
                    return 1;
                return 0;
            }
        });

        List<ShareApp> list = new ArrayList<ShareApp>();
        for (ResolveInfo info : resolveInfoList) {
            ShareApp app = new ShareApp();
            app.title = info.loadLabel(packageMngr).toString();
            app.icon = info.loadIcon(context.getPackageManager());
            app.packageName = info.activityInfo.packageName;
            app.activityName = info.activityInfo.name;
            list.add(app);
        }
        return list;
    }

    public static List<ShareApp> getCustomShareList(Context context) {
        List<ShareApp> list = new ArrayList<ShareApp>();
        ShareApp facebook = new ShareApp();
        facebook.title = context.getResources().getString(R.string.facebook);
        facebook.icon = context.getResources().getDrawable(R.drawable.icn_fb);
        facebook.isSdkPresent = true;
        facebook.packageName = FACEBOOK_PKG_NAME;
        list.add(facebook);

        ShareApp twitter = new ShareApp();
        twitter.title = context.getResources().getString(R.string.twitter);
        twitter.icon = context.getResources().getDrawable(R.drawable.icn_twitter);
        twitter.isSdkPresent = true;
        twitter.packageName = TWITTER_PKG_NAME;
        list.add(twitter);

        ShareApp googlePlus = new ShareApp();
        googlePlus.title = context.getResources().getString(R.string.g_plus);
        googlePlus.icon = context.getResources().getDrawable(R.drawable.btn_gplus);
        googlePlus.isSdkPresent = true;
        googlePlus.packageName = GOOGLE_PLUS_PKG_NAME;
        list.add(googlePlus);
        return list;
    }
}
