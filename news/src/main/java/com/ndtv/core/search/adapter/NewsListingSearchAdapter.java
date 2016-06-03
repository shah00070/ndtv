package com.ndtv.core.search.adapter;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ndtv.core.R;
import com.ndtv.core.common.util.TimeUtils;
import com.ndtv.core.config.model.NewsItems;
import com.ndtv.core.io.VolleyRequestQueue;

import java.util.Date;
import java.util.List;

/**
 * Created by Ram Prakash on 10/4/15.
 */
public class NewsListingSearchAdapter extends BaseAdapter {
    private static final String OPINION = "opinion";
    private static final String BLOG = "blog";
    private List<NewsItems> mNewsListItems;
    // private View.OnClickListener mListener;
    private Activity mContext;

    public NewsListingSearchAdapter(List<NewsItems> newsList, Activity activity) {  //, View.OnClickListener listener
        mNewsListItems = newsList;
        //mListener = listener;
        mContext = activity;
    }

    @Override
    public int getCount() {
        return mNewsListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_news_list, parent, false);
            //LayoutInflater inflator = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //convertView=inflator.inflate(R.layout.item_search_news_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mNewsTitle = (TextView) convertView.findViewById(R.id.news_item_title);
            viewHolder.mNewsSubLine = (TextView) convertView.findViewById(R.id.news_item_subline);
            viewHolder.mNewsThumb = (NetworkImageView) convertView.findViewById(R.id.news_item_thumbnail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mNewsListItems.size() > 0) {
            NewsItems searchItem = mNewsListItems.get(position);
            if (searchItem != null) {
                setSearchedNewsItemData(viewHolder, searchItem);
            }
        }
        //convertView.setOnClickListener(mListener);
        return convertView;
    }

    private void setSearchedNewsItemData(ViewHolder holder, NewsItems items) {
        Date date;
        holder.mNewsTitle.setText(Html.fromHtml(items.getTitle()));
        if(null!=items.category){
            if(items.category.toLowerCase().equals(OPINION)||items.category.toLowerCase().equals(BLOG)) {
                holder.mNewsSubLine.setTextColor(holder.mNewsSubLine.getContext().getResources().getColor(R.color.news_digest_section_name_sports));
                holder.mNewsSubLine.setText(Html.fromHtml(items.category) +" "+ "|"+" "+ Html.fromHtml(items.by_line));
            }else{
                if ((date = items.getSearchPublishDate()) != null) {
                    holder.mNewsSubLine.setTextColor(holder.mNewsSubLine.getContext().getResources().getColor(R.color.body_text_3));
                    holder.mNewsSubLine.setText(TimeUtils.getRelativeTime(mContext, date.getTime()));
                }
            }
        }
       // Date date;
        /*if (null != items.by_line) {
            holder.mNewsSubLine.setText(items.by_line);

        } else {
            if ((date = items.getSearchPublishDate()) != null) {
                holder.mNewsSubLine.setText(TimeUtils.getRelativeTime(mContext, date.getTime()));
            }
        }*/
        holder.mNewsThumb.setDefaultImageResId(R.drawable.place_holder);
        holder.mNewsThumb.setImageUrl(items.thumb_image, VolleyRequestQueue.getInstance(holder.mNewsThumb.getContext()).getImageLoader());

    }

    private static class ViewHolder {
        public TextView mNewsTitle;
        public TextView mNewsSubLine;
        public NetworkImageView mNewsThumb;
    }
}
