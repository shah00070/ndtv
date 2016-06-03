package com.ndtv.core.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.Date;

import static com.ndtv.core.util.ApplicationUtils.date4Sql;
import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Srihari S Reddy on 23/02/15.
 */
public class ContentProviderUtils {

    private static final String TAG = makeLogTag(ContentProviderUtils.class);

    private static final String[] READ_PROJECTION = new String[]{
            NewsContract.ReadItems.READ_ITEM_ID,
    };

    private static final String READ_SELECTION = new StringBuffer(NewsContract.ReadItems.READ_ITEM_ID).append("=?").toString();


    public static void updateReadStatus(Context ctx, String id) {
        ContentValues values = new ContentValues();
        values.put(NewsContract.ReadItems.READ_ITEM_ID, id);
        values.put(NewsContract.ReadItems.UPDATED, date4Sql(new Date()));
        ctx.getContentResolver().insert(NewsContract.ReadItems.CONTENT_URI, values);
    }

    public static boolean IsItemRead(Context ctx, String id) {

        Cursor cursor = ctx.getContentResolver().query(NewsContract.ReadItems.CONTENT_URI,
                READ_PROJECTION,
                READ_SELECTION,
                new String[]{id},
                null);

        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public static void CleanDB(Context ctx, int ageInDays) {
        int count = ctx.getContentResolver().delete(NewsContract.ReadItems.CONTENT_URI,
                ("julianday('now') - julianday(" + NewsContract.ReadItems.UPDATED + ") >= " + ageInDays + " "), new String[]{});
        LOGD(TAG, count + " from ReadItems");

        count = ctx.getContentResolver().delete(NewsContract.NewsItems.CONTENT_URI,
                ("julianday('now') - julianday(" + NewsContract.NewsItems.NEWS_ITEM_UPDATED_AT + ") >= " + ageInDays + " "), new String[]{});

        LOGD(TAG, count + " from NewsItems");

    }
}
