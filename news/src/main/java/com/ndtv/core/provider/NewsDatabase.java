package com.ndtv.core.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.ndtv.core.BuildConfig;
import com.ndtv.core.provider.NewsContract.NewsItemColumns;
import com.ndtv.core.provider.NewsContract.ReadItemColumns;
import com.ndtv.core.provider.NewsContract.SyncColumns;

import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.LOGV;
import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Srihari S Reddy on 04/01/15.
 */
public class NewsDatabase extends SQLiteOpenHelper {

    private static final String TAG = makeLogTag(NewsDatabase.class);

    private static final String DATABASE_NAME = BuildConfig.APPLICATION_ID + "ndtvnews.db";

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String TYPE_DATETIME = " DATETIME";
    private static final String COMMA_SEP = ",";

    private final Context mContext;

    public NewsDatabase(Context context) {
        super(context, DATABASE_NAME, null, BuildConfig.VERSION_CODE);
        mContext = context;
    }

    interface Tables {
        String NEWS_ITEMS = "NewsItems";
        String READ_ITEMS = "ReadItems";
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        if (BuildConfig.DEBUG) {
            LOGV(TAG, "Deleting tables on debug");
            db.execSQL("DROP TABLE IF EXISTS " + Tables.NEWS_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.READ_ITEMS);
        }

        db.execSQL(
                "CREATE TABLE " + Tables.NEWS_ITEMS + "("
                        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + NewsItemColumns.NEWS_ITEM_ID + TYPE_INTEGER + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_TITLE + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_LINK + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_THUMB_IMAGE + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_UPDATED_AT + TYPE_DATETIME + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_SECTION + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_STORY_IMAGE + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_BY_LINE + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_DEVICE + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_POSITION + TYPE_INTEGER + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_TAG + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_TAG_COLOR + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_CATEGORY + TYPE_TEXT + COMMA_SEP
                        +NewsItemColumns.NEWS_ITEM_APPLINK + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_IDENTIFIER + TYPE_TEXT + COMMA_SEP
                        + NewsItemColumns.NEWS_ITEM_TYPE + TYPE_TEXT + COMMA_SEP
                        + SyncColumns.UPDATED + TYPE_DATETIME + COMMA_SEP
                        + "UNIQUE (" + NewsItemColumns.NEWS_ITEM_ID + COMMA_SEP + NewsItemColumns.NEWS_ITEM_SECTION + ") ON CONFLICT REPLACE)"

        );

        db.execSQL(
                "CREATE TABLE " + Tables.READ_ITEMS + "("
                        + ReadItemColumns.READ_ITEM_ID + TYPE_INTEGER + COMMA_SEP
                        + SyncColumns.UPDATED + TYPE_DATETIME + COMMA_SEP
                        + "UNIQUE (" + ReadItemColumns.READ_ITEM_ID + ") ON CONFLICT REPLACE)"
        );


        db.execSQL("CREATE INDEX IF NOT EXISTS idx_id_" + Tables.READ_ITEMS + " ON " + Tables.READ_ITEMS + " (" + ReadItemColumns.READ_ITEM_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_id_" + Tables.NEWS_ITEMS + " ON " + Tables.NEWS_ITEMS + " (" + NewsItemColumns.NEWS_ITEM_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ct_" + Tables.NEWS_ITEMS + " ON " + Tables.NEWS_ITEMS + " (" + NewsItemColumns.NEWS_ITEM_CATEGORY + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ua_" + Tables.NEWS_ITEMS + " ON " + Tables.NEWS_ITEMS + " (" + SyncColumns.UPDATED + ")");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGD(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.NEWS_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.READ_ITEMS);
        onCreate(db);
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
