/**
 Project      : Awaaz
 Filename     : NewsManager.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.ndtv.core.config.model.Comments;
import com.ndtv.core.config.model.CommentsItem;
import com.ndtv.core.config.model.NewsItems;
import com.ndtv.core.ui.widgets.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author anudeep
 */
public class NewsManager extends BaseManager {

    protected static final String LOG_TAG = NewsManager.class.getSimpleName();
    private static NewsManager sNewsManager;
    private BaseFragment.CommentsDownloadListener mCommentsDownloadListener;
    private List<CommentsItem> mCommentItems;
    private BaseFragment.PostCommentsListener mPostCommentsListener;
    private BaseFragment.PostCommentLikeListener mPostCommentLikeListener;
    private BaseFragment.PostCommentDisLikeListener mPostCommentDisLikeListener;
    public List<NewsItems> mNewsSearchItems= new ArrayList<>();


    private NewsManager() {

    }

    public synchronized static NewsManager getNewsInstance() {

        if (null == sNewsManager) {
            sNewsManager = new NewsManager();
        }
        return sNewsManager;
    }

    public void downloadComments(Context ctx, String url, BaseFragment.CommentsDownloadListener listener) {
        mCommentsDownloadListener = listener;
        NewsConnectionManger connectionManger = new NewsConnectionManger();
        connectionManger.downloadComments(ctx, url, commentsDownloadListner(), commentsErrorListener());
    }


    private ErrorListener commentsErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mCommentsDownloadListener != null)
                    mCommentsDownloadListener.onDownloadFailed();
            }
        };
    }

    private Listener<Comments> commentsDownloadListner() {
        return new Response.Listener<Comments>() {

            @Override
            public void onResponse(Comments response) {
                if (mCommentsDownloadListener != null && response != null && response.commentsItemsList != null) {
                    mCommentItems = response.commentsItemsList;
                    mCommentsDownloadListener.onDownloadComplete(response);
                }
            }
        };
    }


    public void postComment(final Context context, final Map<String, String> commentParams,
                            BaseFragment.PostCommentsListener listener) {
        mPostCommentsListener = listener;
        NewsConnectionManger connectionManger = new NewsConnectionManger();
        connectionManger.postComment(context, commentParams, createSuccessListener(listener),
                createErrorListener(listener), false);
    }

    public void postUserInfo(final Context context, final Map<String, String> commentParams,
                             BaseFragment.PostCommentsListener listener) {
        mPostCommentsListener = listener;
        NewsConnectionManger connectionManger = new NewsConnectionManger();
        connectionManger.postComment(context, commentParams, createSuccessListener(listener),
                createErrorListener(listener), true);
    }

    public void likeComment(final Context context, final Map<String, String> questionParams,
                            BaseFragment.PostCommentLikeListener listener) {
        mPostCommentLikeListener = listener;
        NewsConnectionManger connectionManger = new NewsConnectionManger();
        connectionManger.commentLike(context, questionParams, createSuccessListener(listener),
                createErrorListener(listener));
    }

    public void disLikeComment(final Context context, final Map<String, String> questionParams,
                               BaseFragment.PostCommentDisLikeListener listener) {
        mPostCommentDisLikeListener = listener;
        NewsConnectionManger connectionManger = new NewsConnectionManger();
        connectionManger.commentDisLike(context, questionParams, createSuccessListener(listener),
                createErrorListener(listener));
    }

    /**
     * @param listener
     * @return
     */
    private Response.Listener<String> createSuccessListener(final BaseFragment.PostCommentsListener listener) {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (listener != null)
                    listener.onPostCommentComplete(response);
            }

        };
    }

    /**
     * @param listener
     * @return
     */
    private Response.Listener<String> createSuccessListener(final BaseFragment.PostCommentLikeListener listener) {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (listener != null)
                    listener.onPostCommentLikeComplete();
            }
        };
    }

    /**
     * @param listener
     * @return
     */
    private Response.Listener<String> createSuccessListener(final BaseFragment.PostCommentDisLikeListener listener) {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (listener != null)
                    listener.onPostCommentDisLikeComplete();
            }
        };
    }

    /**
     * @param listener
     * @return
     */
    private Response.ErrorListener createErrorListener(final BaseFragment.PostCommentLikeListener listener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null)
                    listener.onPostCommentLikeFailed();

            }
        };
    }

    /**
     * @param listener
     * @return
     */
    private Response.ErrorListener createErrorListener(final BaseFragment.PostCommentsListener listener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null)
                    listener.onPostCommentFailed();

            }
        };
    }

    /**
     * @param listener
     * @return
     */
    private Response.ErrorListener createErrorListener(final BaseFragment.PostCommentDisLikeListener listener) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null)
                    listener.onPostCommentDisLikeFailed();

            }
        };
    }

    public void removeCallBacks() {
        mCommentsDownloadListener = null;
    }

    @Override
    public void cleanUp() {
        sNewsManager = null;
    }

}
