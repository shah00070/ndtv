package com.ndtv.core.livetv.adapter;

import com.ndtv.core.livetv.dto.Program;

import java.util.List;

/**
 * Created by laveen on 13/3/15.
 */
public class LiveTVPrimeScheduleAdapter extends LiveTvScheduleAdapter {

    public LiveTVPrimeScheduleAdapter(List<Program> programs, String liveTvName, String playUrl, int navigationIndex, int sectionIndex) {
        super(programs, liveTvName, playUrl, navigationIndex, sectionIndex);
    }

    protected void updateNowPlayingShow(LiveTVHolder holder, int index) {
        Program currentProgram = mPrograms.get(index);
        if (isCurrentPlayingShow(index) && currentProgram.isShowAvailable()) {
            currentPlayingUI(holder);
        } else {
            completelyPlayedUI(holder);
        }

    }

}
