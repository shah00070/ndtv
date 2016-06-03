/**
 Project      : Awaaz
 Filename     : ShareAdapter.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ndtv.core.R;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.share.ShareApp;

import java.util.List;

/**
 * @author Harisha B
 */
public class ShareAdapter extends ArrayAdapter<ShareApp> {
    private final LayoutInflater mInflator;

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public ShareAdapter(Context context, List<ShareApp> objects) {
        super(context, 0, objects);
        mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    private static class ViewHolder {
        TextView title;
        ImageView icon;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflator.inflate(R.layout.share_pop_up_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.pop_up_item_title);
            holder.icon = (ImageView) convertView.findViewById(R.id.app_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ShareApp appInfo = getItem(position);
        convertView.setTag(R.id.pop_up_item_title,appInfo);
        if (appInfo != null)
            setItemData(holder, appInfo);

        return convertView;
    }

    /**
     * @param holder
     * @param item
     */
    @SuppressLint("NewApi")
    private void setItemData(ViewHolder holder, ShareApp info) {
        holder.title.setText(info.title);
        if (Utility.isJellyBeanAndAbove())
            holder.icon.setBackground(info.icon);
        else
            holder.icon.setBackgroundDrawable(info.icon);

    }
}
