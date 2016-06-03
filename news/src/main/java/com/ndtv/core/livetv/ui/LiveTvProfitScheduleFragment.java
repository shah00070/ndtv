package com.ndtv.core.livetv.ui;

import android.text.TextUtils;

import com.ndtv.core.livetv.adapter.LiveTVPrimeScheduleAdapter;
import com.ndtv.core.livetv.adapter.LiveTvProfitScheduleAdapter;
import com.ndtv.core.livetv.dto.LiveTvSchedule;
import com.ndtv.core.livetv.dto.Program;

import java.util.List;

/**
 * Created by laveen on 13/3/15.
 */

@Deprecated
public class LiveTvProfitScheduleFragment extends LiveTvScheduleFragment {

    /* protected void setLiveTvListAdapter(List<Program> programs) {
         mScheduleList.setAdapter(new LiveTvProfitScheduleAdapter(programs, mLiveTvName, mPlayUrl, mNavigationPosition, mSectionPosition));
     }*/

    @Override
    public void onResponse(LiveTvSchedule liveTvSchedule) {
        // liveTvSchedule.schedule.programeList = shuffleProgram(liveTvSchedule.schedule.programeList);
        updateShowName(liveTvSchedule.schedule.programeList);
        super.onResponse(liveTvSchedule);
    }

    private void updateShowName(List<Program> programs) {
        for (int index = 0; index < programs.size(); index++) {
            if (TextUtils.isEmpty(programs.get(index).name)) {
                programs.get(index).name = "No shows available at the moment";
            }
        }
    }

    protected void setLiveTvListAdapter(List<Program> programs) {
        mScheduleList.setAdapter(new LiveTVPrimeScheduleAdapter(programs, mLiveTvName, mPlayUrl,mNavigationPosition,mSectionPosition));
    }

}
