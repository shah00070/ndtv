package com.ndtv.core.settings.adapter; /**
 Project      : Awaaz
 Filename     : NotificationAdapter.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */


import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.ndtv.core.R;
import com.ndtv.core.common.util.PreferencesManager;

import java.util.Arrays;
import java.util.List;

public class NotificationAdapter extends ArrayAdapter<String> implements OnCheckedChangeListener {
    private LayoutInflater mInflater;
    private SparseBooleanArray mcachedArray = new SparseBooleanArray();
    private boolean mIsChecked = false;


    private static class ViewHolder {
        TextView title;
        CheckBox checkBox;
    }

    /**
     * @param context
     * @param stringArray
     */
    public NotificationAdapter(Context context, String[] stringArray) {
        super(context, 0, stringArray);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setSelectedSections(context, stringArray);
    }

    /**
     * @param context
     * @param stringArray
     */
    public void setSelectedSections(Context context, String[] stringArray) {
        List<String> sectionList = Arrays.asList(stringArray);
        PreferencesManager preManager = PreferencesManager.getInstance(context.getApplicationContext());
        for (String section : sectionList) {
            if (preManager.getNotificationSettings(section))
                mcachedArray.put(sectionList.indexOf(section), true);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.notification_list_item, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.section_title);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(getItem(position));
        holder.checkBox.setTag(position);
        holder.checkBox.setChecked(mcachedArray.get(position));
        holder.checkBox.setOnCheckedChangeListener(this);
        if (mIsChecked) {
            convertView.setPadding(15,15,15,15);
            holder.title.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            convertView.setPadding(0,0,0,0);
            holder.title.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.GONE);
        }

        return convertView;

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (null != mcachedArray)
            mcachedArray.put((Integer) buttonView.getTag(), isChecked);

    }

    public void saveSelectedSections(Context context) {
        List<String> sectionList = Arrays.asList(context.getResources()
                .getStringArray(R.array.custom_push_section_list));
        if (null != sectionList) {
            PreferencesManager preManager = PreferencesManager.getInstance(context.getApplicationContext());
            for (int i = 0; i < sectionList.size(); i++)
                preManager.setNotificationSettings(sectionList.get(i), mcachedArray.get(i));
        }
    }

    public void resetSelections(Context context, String[] stringArray, boolean isEnabled) {
        mcachedArray.clear();
        List<String> sectionList = Arrays.asList(stringArray);
        PreferencesManager preManager = PreferencesManager.getInstance(context.getApplicationContext());
        for (String section : sectionList) {
            if (isEnabled || preManager.getNotificationSettings(section))
                mcachedArray.put(sectionList.indexOf(section), isEnabled);
        }
        saveSelectedSections(context);
    }

    public void enableNotification(boolean enable) {
        mIsChecked = enable;
        notifyDataSetChanged();
    }

    public void disableNotification(boolean disable) {
        mIsChecked = disable;
        notifyDataSetChanged();
    }
}
