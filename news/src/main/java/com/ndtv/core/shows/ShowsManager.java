/**
 * @author Chandan kumar
 *
 */

package com.ndtv.core.shows;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ndtv.core.common.util.BaseManager;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Navigation;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.shows.dto.PrimeShows;
import com.ndtv.core.shows.dto.PrimeVideos;
import com.ndtv.core.shows.io.ShowsConnectionManager;
import com.ndtv.core.video.dto.VideoItem;


/**
 * @author nagaraj
 */
public class ShowsManager extends BaseManager implements ApplicationConstants.NavigationType {

    private static ShowsManager sInstance;
    private ShowsConnectionManager mShowsConnectionMngr;
    private PrimeVideos mPrimeVideos;
    private PrimeShows mPrimeShows;

    public interface ShowsDownloadListener {
        void onShowsDownloaded(PrimeShows shows);

        void onShowsDownloadFailed();

    }

    public interface PrimeVideosDownloadListener {
        void onPrimeVideosDownloaded(PrimeVideos primeVideos);

        void onPrimVideosDownloadFailed();
    }

    private ShowsManager() {
        mShowsConnectionMngr = new ShowsConnectionManager();

    }

    public synchronized static ShowsManager getInstance() {
        if (sInstance == null)
            return sInstance = new ShowsManager();
        return sInstance;
    }

    public void parseShows(final Context context, ShowsDownloadListener listener, int navigationPos) {
        String url = getShowsUrl(context.getApplicationContext(), navigationPos);
        if (mShowsConnectionMngr != null && url != null && !TextUtils.isEmpty(url))
            mShowsConnectionMngr.downloadShows(url, context, showsDownloadListener(listener),
                    showsErrorListener(listener), context);
    }

    /**
     * @return shows URL.
     */
    private String getShowsUrl(Context context, int navigationPos) {
        Navigation navigation = ConfigManager.getInstance().getNavigation(navigationPos, context);
        if (navigation != null)
            return navigation.url;
        return null;
    }

    private Response.Listener<PrimeShows> showsDownloadListener(final ShowsDownloadListener listener) {
        return new Response.Listener<PrimeShows>() {
            @Override
            public void onResponse(PrimeShows response) {
                mPrimeShows = response;
                if (listener != null)
                    listener.onShowsDownloaded(response);
            }
        };
    }

    private Response.ErrorListener showsErrorListener(final ShowsDownloadListener listener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    if (mPrimeShows != null) {
                        listener.onShowsDownloaded(mPrimeShows);
                    } else {
                        listener.onShowsDownloadFailed();
                    }
                }

            }
        };
    }

    public void parsePrimeVideos(final String mLink, final Context ctx, final PrimeVideosDownloadListener listener) {
        if (mShowsConnectionMngr != null && mLink != null && !TextUtils.isEmpty(mLink))
            mShowsConnectionMngr.downloadPrimeVideos(mLink, ctx, PrimeVideosDownloadListener(listener),
                    PrimeVideosErrorListener(listener));
    }

    private Response.Listener<PrimeVideos> PrimeVideosDownloadListener(final PrimeVideosDownloadListener listener) {
        return new Response.Listener<PrimeVideos>() {
            @Override
            public void onResponse(PrimeVideos response) {
                mPrimeVideos = response;
                if (listener != null)
                    listener.onPrimeVideosDownloaded(response);
            }
        };
    }

    private Response.ErrorListener PrimeVideosErrorListener(final PrimeVideosDownloadListener listener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    if (mPrimeVideos != null) {
                        listener.onPrimeVideosDownloaded(mPrimeVideos);
                    } else {
                        listener.onPrimVideosDownloadFailed();
                    }
                }
            }
        };
    }

    public VideoItem getPrimeVideo(int pos) {
        if (mPrimeVideos != null && mPrimeVideos.videoList != null && mPrimeVideos.videoList.size() > pos) {
            return mPrimeVideos.videoList.get(pos);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.july.ndtv.common.BaseManager#cleanUp()
     */
    @Override
    public void cleanUp() {
        sInstance = null;

    }

}
