package com.ndtv.core.notifications;

import android.content.Context;
import android.util.SparseArray;

import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.ndtv.core.common.util.BaseManager;
import com.ndtv.core.notifications.dto.NotificationItem;
import com.ndtv.core.notifications.dto.Notifications;
import com.ndtv.core.notifications.io.NotificationConnectionManager;

/**
 * @author nagaraj
 */
public class NotificationsManager extends BaseManager {

    protected static final String LOG_TAG = NotificationsManager.class.getSimpleName();
    private static NotificationsManager sNotificationsManager;
    private SparseArray<Notifications> mNotificationSections = new SparseArray<Notifications>();
    private NotificationConnectionManager mConnectionMngr;


    public interface NotificationsDownloadListener {
        void onNotificationsDownloaded(Notifications notifications);

        void onNotificationsDownloadFailed();

    }

    private NotificationsManager() {
        mConnectionMngr = new NotificationConnectionManager();

    }

    public synchronized static NotificationsManager getInstance() {
        if (null == sNotificationsManager) {
            sNotificationsManager = new NotificationsManager();
        }
        return sNotificationsManager;
    }

    public void downloadNotifications(final int pos, final Context context, NotificationsDownloadListener listener,
                                      int navigationPos, int tabPosition, final int pageNum) {
        if (mConnectionMngr != null)
            mConnectionMngr.downloadNotifications(pos, context, NotificationDownloadListener(pos, listener),
                    NotificationsErrorListener(listener), navigationPos, tabPosition, pageNum);

    }

    private Listener<Notifications> NotificationDownloadListener(final int pos, final NotificationsDownloadListener listener) {
        return new Listener<Notifications>() {
            @Override
            public void onResponse(Notifications response) {
                if (mNotificationSections != null) {
                    mNotificationSections.put(pos, response);
                    if (listener != null)
                        listener.onNotificationsDownloaded(response);
                }
            }
        };
    }

    private Response.ErrorListener NotificationsErrorListener(final NotificationsDownloadListener listener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null)
                    listener.onNotificationsDownloadFailed();

            }
        };
    }

    public NotificationItem getNotification(final int sectionPos, final int pos) {
        if (mNotificationSections != null) {
            Notifications notifications = mNotificationSections.get(sectionPos);
            if (notifications != null && notifications.notificationList != null && notifications.notificationList.size() > pos)
                return notifications.notificationList.get(pos);
        }
        return null;

    }

    public Notifications getNotifications(final int sectionPos) {
        if (mNotificationSections != null) {
            Notifications notifications = mNotificationSections.get(sectionPos);
            return notifications;
        }
        return null;
    }


    @Override
    public void cleanUp() {
        if (null != mNotificationSections) {
            mNotificationSections.clear();
            mNotificationSections = null;
        }
        mConnectionMngr = null;
        sNotificationsManager = null;
    }


}
