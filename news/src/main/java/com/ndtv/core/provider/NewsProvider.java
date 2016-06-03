package com.ndtv.core.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.ndtv.core.provider.NewsContract.NewsItems;
import com.ndtv.core.util.SelectionBuilder;

import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.makeLogTag;

public class NewsProvider extends ContentProvider {

    private static final String TAG = makeLogTag(NewsProvider.class);

    NewsDatabase mDatabaseHelper;

    private static final String AUTHORITY = NewsContract.CONTENT_AUTHORITY;

    private static final int NEWS_ITEMS = 100;
    private static final int NEWS_ITEMS_ID = 102;
    private static final int READ_ITEMS = 200;
    private static final int READ_ITEMS_ID = 202;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "NewsItems", NEWS_ITEMS);
        sUriMatcher.addURI(AUTHORITY, "NewsItems/*", NEWS_ITEMS_ID);
        sUriMatcher.addURI(AUTHORITY, "ReadItems", READ_ITEMS);
        sUriMatcher.addURI(AUTHORITY, "ReadItems/*", READ_ITEMS_ID);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new NewsDatabase(getContext());
        LOGD(TAG, getContext().getDatabasePath(mDatabaseHelper.getDatabaseName()) + "");
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NEWS_ITEMS:
                return NewsItems.CONTENT_TYPE;
            case NEWS_ITEMS_ID:
                return NewsItems.CONTENT_ITEM_TYPE;
            case READ_ITEMS:
                return NewsContract.ReadItems.CONTENT_TYPE;
            case READ_ITEMS_ID:
                return NewsContract.ReadItems.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {

            case NEWS_ITEMS_ID:
                String id = uri.getLastPathSegment();
                builder.where(NewsItems._ID + "=?", id);

            case NEWS_ITEMS:
                builder.table(NewsItems.TABLE_NAME).where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;

            case READ_ITEMS_ID:
                builder.where(NewsItems._ID + "=?", uri.getLastPathSegment());

            case READ_ITEMS:
                builder.table(NewsContract.ReadItems.TABLE_NAME).where(selection, selectionArgs);
                Cursor c1 = builder.query(db, projection, sortOrder);
                Context ctx1 = getContext();
                assert ctx1 != null;
                c1.setNotificationUri(ctx1.getContentResolver(), uri);
                return c1;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case NEWS_ITEMS:
                long id = db.insertOrThrow(NewsItems.TABLE_NAME, null, values);
                result = Uri.parse(NewsItems.TABLE_NAME + "/" + id);
                break;

            case READ_ITEMS:
                long id1 = db.insertOrThrow(NewsContract.ReadItems.TABLE_NAME, null, values);
                result = Uri.parse(NewsContract.ReadItems.TABLE_NAME + "/" + id1);
                break;
            case NEWS_ITEMS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case NEWS_ITEMS:
                count = builder.table(NewsItems.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case NEWS_ITEMS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(NewsItems.TABLE_NAME)
                        .where(NewsItems._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case READ_ITEMS:
                count = builder.table(NewsContract.ReadItems.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case READ_ITEMS_ID:
                String id1 = uri.getLastPathSegment();
                count = builder.table(NewsContract.ReadItems.TABLE_NAME)
                        .where(NewsContract.ReadItems.READ_ITEM_ID + "=?", id1)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an etry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case NEWS_ITEMS:
                count = builder.table(NewsItems.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case NEWS_ITEMS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(NewsItems.TABLE_NAME)
                        .where(NewsItems._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }
}
