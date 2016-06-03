package com.ndtv.core.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import com.ndtv.core.BuildConfig;

/**
 * Created by Srihari S Reddy on 04/01/15.
 */
public class NewsContract {

    public static final long UPDATED_NEVER = -2;
    public static final long UPDATED_UNKNOWN = -1;

    public interface SyncColumns {
        String UPDATED = "updated";
    }

    interface NewsItemColumns {
        String NEWS_ITEM_ID = "id";
        String NEWS_ITEM_TITLE = "title";
        String NEWS_ITEM_THUMB_IMAGE = "thumb_image";
        String NEWS_ITEM_UPDATED_AT = "updated_at";
        String NEWS_ITEM_LINK = "link";
        String NEWS_ITEM_SECTION = "section";
        String NEWS_ITEM_STORY_IMAGE = "story_image";
        String NEWS_ITEM_BY_LINE = "by_line";
        String NEWS_ITEM_DEVICE = "device";
        String NEWS_ITEM_POSITION = "position";
        String NEWS_ITEM_TAG = "tag";
        String NEWS_ITEM_TAG_COLOR = "tag_color";
        String NEWS_ITEM_CATEGORY = "category";
        String NEWS_ITEM_APPLINK = "applink";
        String NEWS_ITEM_IDENTIFIER = "identifier";
        String NEWS_ITEM_TYPE = "type";
    }

    interface ReadItemColumns {
        String READ_ITEM_ID = "id";
    }


    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String NEWS_ITEMS = "NewsItems";
    private static final String READ_ITEMS = "ReadItems";


    public static class NewsItems implements NewsItemColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(NEWS_ITEMS).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.news.NewsItem";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.news.NewsItem";
        public static final String TABLE_NAME = NewsDatabase.Tables.NEWS_ITEMS;
    }

    public static class ReadItems implements ReadItemColumns, BaseColumns, SyncColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(READ_ITEMS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.news.ReadItem";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.news.ReadItem";

        public static final String TABLE_NAME = NewsDatabase.Tables.READ_ITEMS;

    }

}