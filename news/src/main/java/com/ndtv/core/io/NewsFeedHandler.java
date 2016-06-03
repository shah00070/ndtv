package com.ndtv.core.io;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.R;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.NewsFeed;
import com.ndtv.core.config.model.NewsItems;
import com.ndtv.core.config.model.Stype;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.newswidget.dto.NewsWidget;
import com.ndtv.core.provider.NewsContract;
import com.ndtv.core.util.ApplicationUtils;
import com.ndtv.core.util.GsonRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.ndtv.core.util.ApplicationUtils.date4Sql;
import static com.ndtv.core.util.LogUtils.LOGD;
import static com.ndtv.core.util.LogUtils.LOGV;
import static com.ndtv.core.util.LogUtils.makeLogTag;


/**
 * Created by Srihari S Reddy on 15/01/15.
 */
public class NewsFeedHandler {

    private static final String TAG = makeLogTag(NewsFeedHandler.class);

    String url;
    int pageNumber;
    boolean isCacheRequst;
    Context mContext;
    private ContentResolver mContentResolver;
    String section;
    ResultReceiver mResultreceiver;

    public NewsFeedHandler(ResultReceiver resultreceiver, Context ctx, String s, int i, boolean flag, String sec) {
        mContext = ctx;
        url = s;
        pageNumber = i;
        isCacheRequst = flag;
        mContentResolver = mContext.getContentResolver();
        section = sec;
        url = ApplicationUtils.buildUrl(url, pageNumber, ConfigManager.pageSize + "");
        mResultreceiver = resultreceiver;
    }

    public void downloadFeed() {
        LOGV(TAG, "Requesting content on " + url);

        GsonRequest<NewsFeed> gsonRequest = new GsonRequest<NewsFeed>(getWidgetUrl(url), NewsFeed.class, null, new Response.Listener<NewsFeed>() {
            @Override
            public void onResponse(NewsFeed newsFeed) {
                Log.d("DOWNLOAD", "DOWNLOADED:" + url + "\n\n\n");
            if(newsFeed!=null)
            {
                Bundle bundle = new Bundle();

                if (pageNumber * (ConfigManager.pageSize) < newsFeed.total)
                    bundle.putBoolean("more", true);
                else
                    bundle.putBoolean("more", false);

                mResultreceiver.send(1, bundle);
            }

                ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
                if (newsFeed.results != null) {
                    if((pageNumber==1)&&(((section.equalsIgnoreCase("ख़बरेबड़ी ख़बर"))||(section.equalsIgnoreCase("NewsTop Stories"))||(section.equalsIgnoreCase(ConfigManager.getInstance().getBreakingWidgetNavigationAndSectionName())))))
                    getNewsListingForBreakingWidget(newsFeed);
                    processData(newsFeed, batch);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                LOGD(TAG, "I am errored"+ volleyError);

            }
        });

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(gsonRequest);
    }
    public String getWidgetUrl(String url)
    {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return url+"&"+ts;
    }
    private void getNewsListingForBreakingWidget(NewsFeed newsFeed) {
        List<NewsItems> newsItems = new ArrayList<>(newsFeed.getResults());
        if (newsItems != null) {
            List<NewsWidget.NewsWidgetItem> breakingNews = new ArrayList<NewsWidget.NewsWidgetItem>();
            Iterator<NewsItems> it = newsItems.iterator();
            while (it.hasNext()) {
                NewsItems temp = it.next();
               /* if ((temp.nodes != null) && (temp.nodes.stype != null) && (temp.nodes.stype.t != null) && (temp.nodes.stype.t.toLowerCase().contains("breaking"))) {*/
                if((temp.type!=null)&&(temp.type.equalsIgnoreCase("collage"))){
                    NewsWidget.NewsWidgetItem nw = new NewsWidget.NewsWidgetItem();
                    nw.contentType = Constants.WIDGET_TYPE_TEXT;
                    nw.title = temp.getTitle();
                    nw.time = temp.updatedAt;
                    nw.link = temp.link;
                    NewsWidget.WidgetExtra extradata = new NewsWidget.WidgetExtra();
                    extradata.type=temp.type;
                    /*if((temp.type == null)||((temp.type!=null)&&(temp.type.equals("")))) {
                        extradata.type = Constants.WIDGET_TYPE_TEXT;
                    }
                    else
                    {
                        extradata.type = temp.type;
                    }*/
                    extradata.image = null;
                    extradata.description = null;

                    if (temp.applink == null) {
                        if(temp.device!=null)
                        {
                            String[] tokens = temp.device.split("/");
                            String applink_subcategories =tokens[5];
                            String applink_categories =tokens[3];
                            LOGD(TAG,"applink_subcategories "+applink_subcategories+ " applink_categories "+applink_categories);
                            extradata.applink = "ndtv://category="+applink_categories+"&subcategory="+applink_subcategories+"&id=" + temp.id;
                        }
                        else
                        {
                            extradata.applink = "ndtv://category=news&subcategory="+mContext.getResources().getString(R.string.subcategory_name)+"&id=" + temp.id;
                        }


                    } else
                        extradata.applink = temp.applink;
                    extradata.height = 300;
                    extradata.category = null;
                    if (temp.id == null) {
                        temp.id = "1111"; //dummy id to avoid null pointer
                    }
                    extradata.id = temp.id;
                    nw.extraData = extradata;
                    nw.description = "";
                    if ((temp.story_image == null) || ((temp.story_image != null) && (TextUtils.isEmpty(temp.story_image)))) {
                        nw.image = temp.thumb_image;
                    } else
                        nw.image = temp.story_image;

                    breakingNews.add(nw);

                }

            }
            if (breakingNews.size() > 0) {
                Collections.sort(breakingNews);
                NewsWidgetManager.getInstance().breakingNewsFromListing(breakingNews);
            }
            else
            {
                NewsWidgetManager.getInstance().breakingNewsFromListing(null);
                NewsWidgetManager.getInstance().setBreakingAvailable(false);
            }
        }

    }


    private void processData(NewsFeed newsFeed, ArrayList<ContentProviderOperation> batch) {
        int count = 1 + (ConfigManager.pageSize) * (pageNumber - 1);
        for (NewsItems ni : newsFeed.results) {

            Stype stype;
            try {
                stype = ni.nodes.stype;
            } catch (Exception e) {
                stype = null;
            }

           /* if ((stype == null) || ((stype != null) && (stype.getStypeT(mContext) != null)&&(!stype.getStypeT(mContext).equalsIgnoreCase("Breaking")))) {*/
            if((ni.type==null)||((ni.type!=null)&&(!ni.type.equalsIgnoreCase("collage")))){
                batch.add(ContentProviderOperation.newInsert(NewsContract.NewsItems.CONTENT_URI)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_ID, ni.id)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_TITLE, ni.getTitle())
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_LINK, ni.link)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_THUMB_IMAGE, ni.thumb_image)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_UPDATED_AT, date4Sql(ni.pubDate))
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_SECTION, section)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_STORY_IMAGE, ni.story_image)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_BY_LINE, ni.by_line)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_DEVICE, ni.device)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_TAG, stype == null ? "" : stype.getStypeT(mContext))
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_TAG_COLOR, stype == null ? "" : stype.tc)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_POSITION, count++)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_CATEGORY, ni.category)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_APPLINK, ni.getApplink())
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_IDENTIFIER, ni.identifier)
                        .withValue(NewsContract.NewsItems.NEWS_ITEM_TYPE, ni.type)
                        .withValue(NewsContract.SyncColumns.UPDATED, TextUtils.isEmpty(ni.pubDate) ? date4Sql(ni.pubDate) : date4Sql(ni.updatedAt))
                        .build());
            }
        }
        try {
            mContentResolver.applyBatch(NewsContract.CONTENT_AUTHORITY, batch);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        finally {
            mContentResolver.notifyChange(
                    NewsContract.NewsItems.CONTENT_URI, // URI where data was modified
                    null,                           // No local observer
                    false);

        }


    }
}
