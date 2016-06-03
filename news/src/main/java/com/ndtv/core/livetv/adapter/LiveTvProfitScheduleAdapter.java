package com.ndtv.core.livetv.adapter;

import com.ndtv.core.livetv.dto.Program;

import java.util.List;

/**
 * Created by laveen on 16/3/15.
 */
public class LiveTvProfitScheduleAdapter extends LiveTvScheduleAdapter {

    public LiveTvProfitScheduleAdapter(List<Program> programs, String liveTvName, String playUrl,int navigationIndex,int sectionIndex) {
        super(programs, liveTvName, playUrl,navigationIndex,sectionIndex);
    }

    protected void updateNowPlayingShow(LiveTvScheduleAdapter.LiveTVHolder holder, int index) {
        Program currentProgram = mPrograms.get(index);
        if (isCurrentPlayingShow(index) && currentProgram.isShowAvailable() && index < getItemCount() - 1) {
            currentPlayingUI(holder);
        } else {
            completelyPlayedUI(holder);
        }
    }
}
