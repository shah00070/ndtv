package com.ndtv.core.notifications.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ndtv.core.R;
import com.ndtv.core.common.util.BaseArrayAdapter;
import com.ndtv.core.common.util.TimeUtils;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.views.AspectRatioImageView;
import com.ndtv.core.notifications.dto.NotificationItem;

import org.jsoup.helper.StringUtil;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

public class NotificationsAdapter extends BaseArrayAdapter<NotificationItem> {
    private LayoutInflater mInflater;

    public NotificationsAdapter(Context ctx, List<NotificationItem> notifications) {
        super(ctx, 0, notifications);
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static class ViewHolder {
        TextView notificationTitle;
        TextView notificationTimeStamp;
        NetworkImageView notificationIcon;
        AspectRatioImageView notificationImage;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }


    @Override
    public int getItemViewType(int position) {
        if (getItem(position).imageLink != null)
            return 1;
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            switch (type) {
                case 0:
                    convertView = getArticleViewWithOutImage(position, convertView, parent); // get the video view
                    break;
                case 1:
                    convertView = getArticleViewWithImaage(position, convertView, parent); // get the ad view
                    break;
            }
        }
        viewHolder = (ViewHolder) convertView.getTag();

        NotificationItem item = getItem(position);
//        if (item.imageLink != null)
//            updateViewHeightToAspectRatio(viewHolder);

        switch (type) {
            case 0:
                setArticleDateWithoutImage(viewHolder, item);
                break;
            case 1:
                setArticleDataWithImage(viewHolder, item);
                break;
        }

        return convertView;
    }


    private View getArticleViewWithImaage(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.notification_alert_row_image_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.notificationTitle = (TextView) convertView.findViewById(R.id.notification_title_text);
            viewHolder.notificationTimeStamp = (TextView) convertView.findViewById(R.id.notification_timestamp_text);
            viewHolder.notificationIcon = (NetworkImageView) convertView.findViewById(R.id.icn_notification_iv);
            viewHolder.notificationImage = (AspectRatioImageView) convertView.findViewById(R.id.img_notification_hub);
            viewHolder.notificationImage.setDefaultImageResId(R.drawable.place_holder);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }


    private View getArticleViewWithOutImage(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.notification_alert_row_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.notificationTitle = (TextView) convertView.findViewById(R.id.notification_title_text);
            viewHolder.notificationTimeStamp = (TextView) convertView.findViewById(R.id.notification_timestamp_text);
            viewHolder.notificationIcon = (NetworkImageView) convertView.findViewById(R.id.icn_notification_iv);
            //viewHolder.notificationImage.setDefaultImageResId(R.drawable.place_holder);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }


    private void setArticleDataWithImage(ViewHolder viewHolder, NotificationItem item) {
        if (item != null) {
            setItemData(viewHolder, item);
            setArticleImage(viewHolder, item);
        }

    }


    private void setArticleDateWithoutImage(ViewHolder viewHolder, NotificationItem item) {
        if (item != null)
            setItemData(viewHolder, item);
    }

    private void setItemData(ViewHolder viewHolder, NotificationItem item) {
        Date programDate = null;
        programDate = TimeUtils.getNewsDate(item.pubdate);

        if (null != item.category && null != programDate) {

            String categoryText = "";
            categoryText = StringUtil.join(item.category, " , ");

            long time = programDate.getTime();

            if (!TextUtils.isEmpty(categoryText))
                viewHolder.notificationTimeStamp.setText(MessageFormat.format("{0} | {1}",
                        TimeUtils.getRelativeTime(getContext(), time), categoryText));
            else
                viewHolder.notificationTimeStamp.setText(TimeUtils.getRelativeTime(getContext(), time));
        }

        viewHolder.notificationTitle.setText(item.title);


        if (null != item.metadata) {
            viewHolder.notificationIcon.setImageUrl(item.metadata.icon, imageLoader);
        }


    }


    private void setArticleImage(ViewHolder viewHolder, NotificationItem item) {
        if (null != item.imageLink) {
            viewHolder.notificationImage.setImageUrl(item.imageLink, imageLoader);

        }
    }


}