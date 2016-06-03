package com.ndtv.core.video.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ndtv.core.R;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.video.dto.VideoItem;

import java.util.List;

/**
 * Created by laveen on 16/2/15.
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoItemHolder> {
    protected List<VideoItem> mVideosList;

    protected View.OnClickListener mListener;

    public VideoListAdapter(List<VideoItem> videoList, View.OnClickListener listener) {
        mVideosList = videoList;
        mListener = listener;
    }

    @Override
    public VideoItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_videos_list, viewGroup, false);
        return new VideoItemHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoItemHolder liveTVHolder, int i) {
        liveTVHolder.itemView.setTag(i);
        VideoItem item = mVideosList.get(i);
        setVideoImage(liveTVHolder, item);
        setVideoTile(liveTVHolder, item);
        setVideoDuration(liveTVHolder, item);
    }

    private void setVideoImage(VideoItemHolder holder, VideoItem item) {
        holder.mImageThumb.setImageUrl(item.fullImage, VolleyRequestQueue.getInstance(holder.mImageThumb.getContext()).getImageLoader());
    }

    private void setVideoTile(VideoItemHolder holder, VideoItem item) {
        holder.mVideoTitle.setText(Html.fromHtml(item.getVideoTitle()));
    }

    private void setVideoDuration(VideoItemHolder holder, VideoItem item) {
        holder.mVideoDuration.setText(item.getVideoDuration());
    }

    @Override
    public int getItemCount() {
        return mVideosList.size();
    }

    public class VideoItemHolder extends RecyclerView.ViewHolder {

        public NetworkImageView mImageThumb;
        public TextView mVideoTitle;
        public TextView mVideoDuration;

        public VideoItemHolder(View itemView) {
            super(itemView);
            mImageThumb = (NetworkImageView) itemView.findViewById(R.id.video_thumb);
            // mImageThumb.setDefaultImageResId(R.drawable.place_photos_large_black);
            mImageThumb.setDefaultImageResId(R.drawable.place_holder_black_new);
            mVideoTitle = (TextView) itemView.findViewById(R.id.video_title);
            mVideoDuration = (TextView) itemView.findViewById(R.id.video_duration);
            itemView.setOnClickListener(mListener);
            updateViewHeightToAspectRatio(mImageThumb);
        }
    }

    private void updateViewHeightToAspectRatio(final View view) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                //     Log.d("VIEWHEIGHT", "onPreDraw:" + view);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (int) (view.getWidth() * .75);
                view.setLayoutParams(params);
                //view.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }
}