package com.ndtv.core.util;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ndtv.core.R;

import java.util.HashMap;

/**
 * Created by Srihari S Reddy on 15/02/15.
 */
public class GAHandler {

    private static GAHandler sInstance;
    private Tracker tracker;
    private GoogleAnalytics analytics;
    public HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    private GAHandler(Context applicationContext) {
        analytics = GoogleAnalytics.getInstance(applicationContext);
        analytics.setLocalDispatchPeriod(1800);
        tracker = getTracker(TrackerName.APP_TRACKER);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableExceptionReporting(true);
        tracker.enableAutoActivityTracking(true);
    }

    public synchronized static GAHandler getInstance(Context ctx) {
        if (sInstance == null)
            sInstance = new GAHandler(ctx.getApplicationContext());
        return sInstance;
    }

    public synchronized void SendScreenView(String screenName) {
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }

    public synchronized void SendEvent(String category, String action, String label, Long value) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());

    }


    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this
        // app.
        GLOBAL_TRACKER, // Tracker used by all the
        // apps from a company.
        // eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all
        // ecommerce
        // transactions from a
        // company.
    }


    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            if (trackerId == TrackerName.APP_TRACKER) {
                mTrackers.put(trackerId, analytics.newTracker(R.xml.ga_tracker));
            }
        }
        return mTrackers.get(trackerId);
    }
}
