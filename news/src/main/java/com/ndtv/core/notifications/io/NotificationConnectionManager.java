package com.ndtv.core.notifications.io;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.common.util.GsonObjectRequest;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Section;
import com.ndtv.core.constants.ApplicationConstants.CustomApiType;
import com.ndtv.core.constants.ApplicationConstants.NavigationType;
import com.ndtv.core.notifications.dto.Notifications;

//import com.ndtv.core.constants.ApplicationConstants.UrlKeys;

/**
 *
 *
 */
public class NotificationConnectionManager implements NavigationType, CustomApiType {


    public void downloadNotifications(final int pos, final Context ctx, Listener<Notifications> listener,
                                      ErrorListener errorListener, int navigationPos, int tabPosition, final int pageNum) {
        final Section notificationSection = ConfigManager.getInstance().getSection(pos, navigationPos);
        final String url;
        if (notificationSection != null && !TextUtils.isEmpty(notificationSection.url)) {
            url = notificationSection.url;

            if (!TextUtils.isEmpty(url)) {
                final NdtvApplication application = NdtvApplication.getApplication(ctx);
                final GsonObjectRequest<Notifications> jsObjRequest = new GsonObjectRequest<Notifications>(Method.GET, url,
                        Notifications.class, null, listener, errorListener, ctx.getApplicationContext());
                application.mRequestQueue.add(jsObjRequest);
            }
        }
    }
}
