package com.ndtv.core.common.util;


import com.ndtv.core.radio.dto.LiveRadioSchedules;

/**
 * Created by Harisha B on 3/2/15.
 */
/*
* TODO
* Use base managers in all modules to auto clean up
* commented the code not being used for time being
* please uncomment the code to use that
* */

public abstract class BaseManager {

    public interface ConfigDownloadListener {
        void onConfigDownloadComplete();

        void onConfigDownloadFailed();
    }

    public abstract void cleanUp();


    /*public interface SecondScreenDownloadListener {
        void onDownloadCompleted(SecondScreenSchedules secondScreenSchedules);

        void onDownloadFailed();
    }

    public interface CricketCalendarNativeDownloadListener {
        void onDownloadCompleted(CricketCalendar cricketCalendar);

        void onDownloadFailed();
    }*/

    public interface CricketCalendarRefreshListener {
        void onDownloadCompleted(boolean isSet);

        void onDownloadFailed();
    }

    /*public interface CricketScoreCardNativeDownloadListener {
        void onDownloadCompleted(CricketScoreCard cricketScoreCard);

        void onDownloadFailed();
    }

    public interface CricketCommentaryDownloadListener {
        void onDownloadCompleted(CricketCommentaryDTO cricketCommentaryDTO);

        void onDownloadFailed();
    }*/

    public interface SecondScreenPostQuestionListener {
        void onPostSuccess();

        void onPostFailed();
    }

    public interface DownloadListener {
        void onDownloadSucces();

        void onDownloadFailed();
    }

    public interface AddLikeDislikeListener {
        void onSuccess();

        void onFailed();
    }

    public interface LiveRadioScheduleListener {
        void onSuccess(LiveRadioSchedules liveRadioSchedules);

        void onFailed();
    }

    public interface VotesCountListener {
        void onSuccess(String response);

        void onFailed();
    }

    public interface SecondScreenPromoVideoListener {
        void onSuccess(String url);

        void onFailed();
    }

    public interface SecondScreenChannelDetailsListener {
        void onSuccess();

        void onFailed();
    }

    /*public interface SocialHubAlertsDownloadListener {
        void onSuccess(AlertsData alertsData);

        void onFailed();
    }

    public interface NewsroomLiveDownloadListener {
        void onSuccess(NewsroomLiveDTO newsroomLiveDTO);

        void onFailed();
    }*/

    public interface DeepLinkVideoDownloadListener {
        void onSuccess();

        void onFailed();
    }

    public interface NewsIn60SecondsListener {
        void onSuccess();

        void onFailed();
    }

    public static interface NewsIn60PostLikeShareInterface {
        void onSuccess();

        void onFailure();
    }

    public static interface NewsIn60VideoDownloadListener {
        void onVideoDownloaded();

        void onVideoDownloadFailed();
    }

}

