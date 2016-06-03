package com.ndtv.core.livetv.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ndtv.core.R;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.livetv.dto.Program;
import com.ndtv.core.livetv.ui.LiveTVPlayActivity;
import com.ndtv.core.livetv.ui.LiveTvPlayFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by laveen on 5/2/15.
 */
public class LiveTvScheduleAdapter extends RecyclerView.Adapter<LiveTvScheduleAdapter.LiveTVHolder> {

    public final int SHOW_DURATION= 60000*30;//30 mins in miliseconds
    protected List<Program> mPrograms;
    protected String mPlayUrl;
    protected String mLiveTvName;
    private int mNavigationPosition;
    private int mSectionPosition;

    public LiveTvScheduleAdapter(List<Program> programs, String liveTvName, String playUrl, int navigationIndex, int secionIndex) {
        mPrograms = programs;
        mPlayUrl = playUrl;
        mLiveTvName = liveTvName;
        mNavigationPosition = navigationIndex;
        mSectionPosition = secionIndex;
    }

    @Override
    public LiveTVHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_livetv_schedule, viewGroup, false);
        return new LiveTVHolder(view);
    }

    @Override
    public void onBindViewHolder(LiveTVHolder liveTVHolder, int i) {
        liveTVHolder.itemView.setTag(i);
        liveTVHolder.mShowNowPlaying.setTag(i);
        setShowName(liveTVHolder, i);
        setShowTime(liveTVHolder, i);
        setShowImage(liveTVHolder, i);
        updateNowPlayingShow(liveTVHolder, i);
    }

    protected void updateNowPlayingShow(LiveTVHolder holder, int index) {
        Program currentProgram = mPrograms.get(index);
        if (isCurrentPlayingShow(index)) {
            currentPlayingUI(holder);
        } else {
            completelyPlayedUI(holder);
        }
    }

    protected void currentPlayingUI(LiveTVHolder holder) {
        yetToPlayUI(holder);
        holder.mShowNowPlaying.setVisibility(View.VISIBLE);
        holder.setCurrentPlaying(true);
    }

    protected void yetToPlayUI(LiveTVHolder holder) {
        holder.mShowNowPlaying.setVisibility(View.GONE);
        holder.mShowIndicator.setBackgroundResource(R.drawable.icn_actv);
        holder.mShowDummyView.setBackgroundResource(R.drawable.schedule_image_background_playing);
    }

    protected void completelyPlayedUI(LiveTVHolder holder) {
        holder.mShowNowPlaying.setVisibility(View.GONE);
        holder.mShowIndicator.setBackgroundResource(R.drawable.icn_inactv);
        holder.setCurrentPlaying(false);
        holder.mShowDummyView.setBackgroundResource(R.drawable.schedule_image_background_not_playing);
    }

    protected void setShowName(LiveTVHolder holder, int index) {
        holder.mShowName.setText(mPrograms.get(index).name);
        holder.mShowName.bringToFront();
    }

    protected void setShowTime(LiveTVHolder holder, int index) {
        Program program = mPrograms.get(index);
        if (TextUtils.isEmpty(program.getShowTime())) {
            String time = program.timestamp;
            if (!TextUtils.isEmpty(time)) {
                final String[] hoursMinutes = time.split(":");
                final String hours = hoursMinutes[0];

                try {
                    int hour = Integer.parseInt(hours);
                    String meridiem = "\nAM";
                    if (hour >= 12) {
                        if (hour != 12)
                            hour = hour % 12;
                        meridiem = "\nPM";
                    }

                    //just to diplay it in the format 01 hours
                    if (hour < 10) {
                        hoursMinutes[0] = "0" + hour;
                    } else {
                        hoursMinutes[0] = "" + hour;
                    }

                    if (hoursMinutes.length > 1)
                        program.setShowTime(hoursMinutes[0] + ":" + hoursMinutes[1] + meridiem);
                    else
                        program.setShowTime(hoursMinutes[0] + meridiem);
                } catch (NumberFormatException ex) {

                }
            }
        }

        if (TextUtils.isEmpty(program.getShowTime()))
            holder.mShowTime.setText("");
        else
            holder.mShowTime.setText(program.getShowTime());
    }

    private void setShowImage(LiveTVHolder holder, int index) {
        holder.mShowImage.setImageUrl(mPrograms.get(index).image, VolleyRequestQueue.getInstance(holder.mShowImage.getContext()).getImageLoader());
    }

    @Override
    public int getItemCount() {
        return mPrograms.size();
    }

    protected boolean isCurrentPlayingShow(int index) {
        Program currentProgram = mPrograms.get(index);
        Calendar deviceCalendar = Calendar.getInstance();
        Date deviceDate = deviceCalendar.getTime();
        Date programDate = currentProgram.getProgramTime();

        if (programDate != null) {
            if (deviceDate.getTime() >= programDate.getTime()) {
                if (mPrograms.size() > index + 1) {
                    //  Date nextShowDate = Utility.getProgramTime(mPrograms.get(index + 1));
                    Date nextShowDate = mPrograms.get(index + 1).getProgramTime();
                    if (nextShowDate != null)
                        if (deviceDate.getTime() < nextShowDate.getTime()) {

                            return true;
                        }
                } else {
                    Log.d("TIME", "DEVICE TIME:" + deviceDate.getTime());
                    Log.d("TIME", "TIME:" + programDate.getTime());
                    Date date = new Date();
                    date.setTime(programDate.getTime() + (SHOW_DURATION));
                   // programDate.setTime(programDate.getTime() + (60000 * 30));
                    Log.d("TIME", "TIME:" + programDate.getTime());

                    if (deviceDate.getTime() > date.getTime())
                        return false;
                      else
                        return true;
                }
            }
        }
        return false;
    }

    public class LiveTVHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mShowName;
        public TextView mShowTime;
        public ImageButton mShowIndicator;
        public ImageView mShowNowPlaying;
        public NetworkImageView mShowImage;
        public View mShowDummyView;
        public boolean mPlayingShow;

        public LiveTVHolder(View itemView) {
            super(itemView);
            mShowIndicator = (ImageButton) itemView.findViewById(R.id.show_indicator);
            mShowImage = (NetworkImageView) itemView.findViewById(R.id.show_image);
            mShowName = (TextView) itemView.findViewById(R.id.show_name);
            mShowTime = (TextView) itemView.findViewById(R.id.show_time);
            mShowNowPlaying = (ImageView) itemView.findViewById(R.id.show_now_playing);
            mShowDummyView = itemView.findViewById(R.id.show_image_top_layer);

            mShowImage.setDefaultImageResId(R.drawable.place_holder_livetv);

            updateViewHeightToAspectRatio(mShowImage);
            updateViewHeightToAspectRatio(mShowDummyView);

            itemView.setOnClickListener(this);
            mShowNowPlaying.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isCurrentPlaying()) {
                int index = (int) v.getTag();
                Program program = mPrograms.get(index);
                Intent intent = new Intent(v.getContext(), LiveTVPlayActivity.class);
                intent.putExtra(LiveTvPlayFragment.LIVETV_NAME, mLiveTvName);
                intent.putExtra(LiveTvPlayFragment.LIVETV_URL, mPlayUrl);
                intent.putExtra(LiveTvPlayFragment.LIVETV_SHOW_NAME, program.name);
                intent.putExtra(LiveTvPlayFragment.LIVETV_SHOW_IMAGE_URL, program.image);

                intent.putExtra(ApplicationConstants.BundleKeys.NAVIGATION_POS, mNavigationPosition);
                intent.putExtra(ApplicationConstants.BundleKeys.SECTION_POS, mSectionPosition);

                v.getContext().startActivity(intent);
            }
        }

        public void setCurrentPlaying(boolean state) {
            mPlayingShow = state;
        }

        public boolean isCurrentPlaying() {
            return mPlayingShow;
        }
    }

    private void updateViewHeightToAspectRatio(final View view) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                //     Log.d("VIEWHEIGHT", "onPreDraw:" + view);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (int) (view.getWidth() * .62);
                view.setLayoutParams(params);
                //view.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }
}
