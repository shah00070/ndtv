package com.ndtv.core.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ndtv.core.R;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.model.PhotoFeedItem;
import com.ndtv.core.io.VolleyRequestQueue;

import java.util.ArrayList;

public class PhotoListAdapter extends BaseAdapter {

    private ArrayList<PhotoFeedItem> photoItemList;
    private Activity activity;


    public PhotoListAdapter(Activity activity, ArrayList<PhotoFeedItem> photoItemList) {
        this.activity = activity;
        this.photoItemList = photoItemList;
    }

    @Override
    public int getCount() {
        return photoItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflator = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(R.layout.photo_list_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.photo_title);
            holder.noOfImage = (TextView) convertView.findViewById(R.id.no_of_photos_tv);
            holder.image = (NetworkImageView) convertView.findViewById(R.id.full_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PhotoFeedItem item = photoItemList.get(position);
        if (item != null)
            setItemData(holder, item);

        return convertView;
    }

    private void setItemData(ViewHolder holder, PhotoFeedItem item) {
        holder.title.setText(Html.fromHtml(Utility.decodeString(item.getTitle())).toString());
        if (Integer.parseInt(item.getCount().trim()) == 1) {
            holder.noOfImage.setText(item.getCount() + " " + activity.getString(R.string.total_image));
        } else {
            holder.noOfImage.setText(item.getCount() + " " + activity.getString(R.string.total_images));
        }
        holder.image.setDefaultImageResId(R.drawable.place_holder_black_new);
        ImageLoader imageLoader = VolleyRequestQueue.getInstance(activity).getImageLoader();
        NetworkImageView.class.cast(holder.image).setImageUrl(item.fullimage, imageLoader);
    }

    private static class ViewHolder {
        TextView title;
        TextView noOfImage;
        NetworkImageView image;
    }
}
