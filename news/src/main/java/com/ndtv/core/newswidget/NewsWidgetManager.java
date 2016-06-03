package com.ndtv.core.newswidget;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.common.util.BaseManager;
import com.ndtv.core.newswidget.dto.NewsWidget;
import com.ndtv.core.newswidget.dto.NewsWidget.NewsWidgetItem;
import com.ndtv.core.newswidget.io.NewsWidgetConnectionManager;
import com.ndtv.core.newswidget.ui.custom.WidgetInterfaces;
import com.ndtv.core.util.LogUtils;
import com.ndtv.core.video.dto.Videos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Harisha B on 18/2/15.
 */
public class NewsWidgetManager extends BaseManager {

    private NewsWidget mWidget = new NewsWidget();
    private Videos mVideos;
    private boolean mIsFromNewsWidget;
    private int mFullPhotoClickPosition;
    boolean mWidgetNotInitialised = false;
    private static final String TAG = LogUtils.makeLogTag(NewsWidgetManager.class);

    private static NewsWidgetManager sNewsWidgetManager;
    private List<NewsWidgetItem> savedWidgetitemsFromListing;
    private boolean isErrored;
    private List<NewsWidgetItem> lastResponse;
    //private List<NewsWidgetItem> lastmWidgetItemForListing;
    //private List<NewsWidgetItem> mWidgetitemCopy;
    private boolean breakingAvailable = false;

    public void setWidgetUpdateListener(WidgetItemsFromBreakingUpdateListener widgetUpdateListener) {
        this.widgetUpdateListener = widgetUpdateListener;
    }

    private NewsWidgetManager.WidgetItemsFromBreakingUpdateListener widgetUpdateListener;
    //public  boolean modified = false;

    public static NewsWidgetManager getInstance() {

        if (sNewsWidgetManager == null) {
            sNewsWidgetManager = new NewsWidgetManager();

        }

        return sNewsWidgetManager;
    }

    private NewsWidgetManager() {
    }

    public void downloadWidgetData(Context context, String url, final Response.Listener<NewsWidget> listener, final Response.ErrorListener errorListener) {
        NewsWidgetConnectionManager connectionManger = NewsWidgetConnectionManager.getInstance();
        connectionManger.downloadNewsWidget(context, url, getWidgetSucessListner(listener), new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                isErrored = true;
                saveResponseForNextCycle(null);
                //setmWidget(null);
                errorListener.onErrorResponse(error);
            }
        });
    }

    private Response.Listener<NewsWidget> getWidgetSucessListner(final Response.Listener<NewsWidget> listener) {
        return new Response.Listener<NewsWidget>() {

            @Override
            public void onResponse(NewsWidget response) {
                isErrored = false;
                if(lastResponse==null || !lastResponse.equals(response.item)) {
                    saveResponseForNextCycle(response.item);
                    setmWidget(response);

                }
                listener.onResponse(response);
            }
        };
    }


    private void saveResponseForNextCycle(List<NewsWidgetItem> responseItem) {
        if ((responseItem)==null)
        {
            lastResponse=null;
        }
        else
            lastResponse = new ArrayList<NewsWidgetItem>(responseItem);
    }


    public void downloadVideoItem(Context context, String URL, final WidgetInterfaces.OnVideoItemAvailbleListener mOnVideoItemAvailbleListener, Response.ErrorListener errorListener) {
        NewsWidgetConnectionManager newsWidgetConnectionManager = NewsWidgetConnectionManager.getInstance();
        newsWidgetConnectionManager.downloadVideoItem(context, URL, getVideoItemSuccessListener(mOnVideoItemAvailbleListener), errorListener);
    }

    public void downloadAlbum(Context context, String url, Response.Listener listener, Response.ErrorListener errorListener) {
        NewsWidgetConnectionManager newsWidgetConnectionManager = NewsWidgetConnectionManager.getInstance();
        newsWidgetConnectionManager.downloadAlbum(context, url, listener, errorListener);
    }

    private Response.Listener getVideoItemSuccessListener(final WidgetInterfaces.OnVideoItemAvailbleListener mOnVideoItemAvailbleListener) {
        return new Response.Listener() {
            @Override
            public void onResponse(Object o) {
                setmVideos((Videos) o);
                mOnVideoItemAvailbleListener.onVideoAvailable((Videos) o);
            }
        };
    }

    @Override
    public void cleanUp() {

    }

    public NewsWidget getmWidget() {
        return mWidget;
    }

    public void setmWidget(NewsWidget mWidget) {

            this.mWidget = mWidget;

        if (savedWidgetitemsFromListing!=null)
        {
            addMWidgetitem(savedWidgetitemsFromListing);
        }

    }

    public void addMWidgetitem(List<NewsWidgetItem> mWidgetItems) {
        if (mWidgetItems==null)
        {
            if (lastResponse!=null)
            {
                retainLastWidgetFromNewsWidget(lastResponse);
                widgetUpdateListener.onWidgetItemsUpdated(mWidget);

            }
            else
            {
                if(mWidget.item!=null) {
                    mWidget.item.clear();
                    widgetUpdateListener.onWidgetItemsUpdated(mWidget);
                }
            }
            return;
        }
        //mWidgetitemCopy = new ArrayList<>(mWidgetItems);
        savedWidgetitemsFromListing= null;
        if (lastResponse!=null) {
            retainLastWidgetFromNewsWidget(lastResponse);
        }
        if(mWidget.item==null && !isErrored)
        {
            saveBreakingItemsFromListing(mWidgetItems);
            return;
        }
        if (isErrored)
        {
            this.mWidget = new NewsWidget();
            List<NewsWidget.NewsWidgetItem> breakingNews = new ArrayList<NewsWidgetItem>();
            mWidget.item = breakingNews;
            Iterator<NewsWidgetItem> its = mWidgetItems.iterator();
            while (its.hasNext()) {
                final NewsWidget.NewsWidgetItem temp = its.next();

                    mWidget.item.add(temp);
            }
            setmWidget(mWidget);
            //savemWidgetItemoFListingForNextCycle(mWidgetitemCopy);
            widgetUpdateListener.onWidgetItemsUpdated(mWidget);
            return;
        }
        LogUtils.LOGD(TAG, "i am size of mWidget.item " + mWidget.item.size());
        Iterator<NewsWidgetItem> it = mWidgetItems.iterator();

        while (it.hasNext()) {
            final NewsWidget.NewsWidgetItem temp = it.next();

            if (mWidget.item.contains(temp)) {
                it.remove();
            } else
                mWidget.item.add(temp);
        }
        //savemWidgetItemoFListingForNextCycle(mWidgetitemCopy);
        widgetUpdateListener.onWidgetItemsUpdated(mWidget);

    }

    private void retainLastWidgetFromNewsWidget(List<NewsWidgetItem> lastmWidgetItemForListing) {
        if(mWidget.item != null) {
            mWidget.item.retainAll(lastmWidgetItemForListing);
            //widgetUpdateListener.onWidgetItemsUpdated(mWidget);
        }


    }

    private void saveBreakingItemsFromListing(List<NewsWidgetItem> mWidgetItems) {

        savedWidgetitemsFromListing = new ArrayList<NewsWidgetItem>(mWidgetItems);
    }

    public int getWidgetCount() {
        if (mWidget != null) {
            if (null != mWidget.item) {
                return mWidget.item.size();
            }
        }
        return 0;
    }


    public NewsWidgetItem getWidgetData(int widgetIndex) {
        if (mWidget != null) {
            if (widgetIndex < mWidget.item.size()) {
                return mWidget.item.get(widgetIndex);
            }
        }
        return null;
    }

    public String getWidgetType(int index) {
        if (mWidget != null) {
            if (index < mWidget.item.size()) {
                NewsWidgetItem widget = mWidget.item.get(index);
                if (widget.extraData != null)
                    return widget.extraData.type;
            }
        }
        return null;
    }

    public String getWidgetAppLink(int index) {
        if (mWidget != null) {
            if (index < mWidget.item.size()) {
                NewsWidgetItem widget = mWidget.item.get(index);
                if (widget.extraData != null)
                    return widget.extraData.applink;
            }
        }
        return null;
    }

    public static String getDeeplinkCategory(String deeplinkUrl) {
        if (!TextUtils.isEmpty(deeplinkUrl)) {
            //"ndtv://category=news&subcategory=world&id=641669"
            String[] array = deeplinkUrl.split("&subcategory=");
            array = array[0].split("category=");
            if (array.length > 1) {
                String category = array[1];
                if (!TextUtils.isEmpty(category))
                    return category;
            }
        }
        return null;
    }

    public static String getDeeplinkSubcategory(String deeplinkUrl) {
        if (!TextUtils.isEmpty(deeplinkUrl)) {
            String[] array = deeplinkUrl.split("&subcategory=");
            if (array.length > 1) {
                String subCategory = array[1];
                //If subcategory value is missing from config
                if (subCategory.equalsIgnoreCase("&id=")) return null;
                array = array[1].split("&id=");
                String category = array[0];
                if (!TextUtils.isEmpty(category))
                    return category;
            }
        }
        return null;
    }

    public static String getDeeplinkingId(String deeplinkUrl) {
        if (!TextUtils.isEmpty(deeplinkUrl)) {
            String[] array = deeplinkUrl.split("&id=");
            if (array.length > 1) {
                String category = array[1];
                if (!TextUtils.isEmpty(category))
                    return category;
            }
        }
        return null;
    }


    public Videos getmVideos() {
        return mVideos;
    }

    public void setmVideos(Videos mVideos) {
        this.mVideos = mVideos;
    }

    public boolean getMIsFromNewsWidget() {
        return mIsFromNewsWidget;
    }

    public void setMIsFromNewsWidget(boolean fromNewsWidget) {
        mIsFromNewsWidget = fromNewsWidget;
    }

    public void setmFullPhotoClickPosition(int fullPhotoClickPosition) {
        mFullPhotoClickPosition = fullPhotoClickPosition;
    }

    public int getmFullPhotoClickPosition() {
        return mFullPhotoClickPosition;
    }

    public void breakingNewsFromListing(List<NewsWidgetItem> item) {
        if (item!=null)
        breakingAvailable = true;
        else
        breakingAvailable =false;
        addMWidgetitem(item);
    }
    public boolean isBreakingStoriesAvailable()
    {
        return breakingAvailable;
    }
    public void setBreakingAvailable(boolean yes)
    {
        breakingAvailable = yes;
    }
    public interface WidgetItemsFromBreakingUpdateListener {
        void onWidgetItemsUpdated(NewsWidget nw);


    }


/*    private void savemWidgetItemoFListingForNextCycle(List<NewsWidgetItem> responseItem) {
        lastmWidgetItemForListing = new ArrayList<NewsWidgetItem>(responseItem);
    }*/

}
