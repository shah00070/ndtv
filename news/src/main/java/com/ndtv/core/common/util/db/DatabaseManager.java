/**
 Project      : Awaaz
 Filename     : DatabaseManager.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ndtv.core.NdtvApplication;
import com.ndtv.core.config.BaseManager;
import com.ndtv.core.config.model.CommentsItem;
import com.ndtv.core.config.model.Reply;
import com.ndtv.core.ui.adapters.CommentsListAdapter;

import java.sql.SQLException;

/**
 * @author Chandan kumar
 */
public class DatabaseManager extends BaseManager {

    private String LOG_TAG = DatabaseManager.class.getSimpleName();

    private static DatabaseManager sDatabaseManager = null;


    public static interface IsCommentLiked {
        void onFetch(CommentsItem item, CommentsListAdapter.CommentGroupHolder commentGroupHolder);

        void onFetchChild(Reply item, CommentsListAdapter.CommentGroupHolder commentGroupHolder);
    }

    private DatabaseManager() {

    }

    public synchronized static DatabaseManager getInstance(Context ctx) {
        if (null == sDatabaseManager) {
            sDatabaseManager = new DatabaseManager(ctx);
        }
        return sDatabaseManager;
    }

    private DataBaseHelper mDatabaseHelper;


    private DatabaseManager(Context ctx) {
        mDatabaseHelper = new DataBaseHelper(ctx);
    }

    private DataBaseHelper getHelper() {
        return mDatabaseHelper;
    }

    public void setCommentLikeDislikeItem(final CommentsItem commentItem, Context context) {
        NdtvApplication.getApplication(context.getApplicationContext()).getDatabaseThreadExecutorService()
                .submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            getHelper().getCommentIdDao().createOrUpdate(commentItem);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                });
    }

    public void setCommentLikeDislikeItem(final Reply commentItem, Context context) {
        NdtvApplication.getApplication(context.getApplicationContext()).getDatabaseThreadExecutorService()
                .submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            getHelper().getReplyDao().createOrUpdate(commentItem);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }


    public void isCommentLiked(final String commentId, final CommentsListAdapter.CommentGroupHolder commentsAdapter,
                               final IsCommentLiked isCommentLiked) {

        new AsyncTask<Void, Void, CommentsItem>() {

            @Override
            protected CommentsItem doInBackground(Void... params) {
                // Boolean isExists = false;
                if (!isCancelled()) {
                    CommentsItem item = null;
                    try {
                        item = getHelper().getCommentIdDao().queryForId(commentId);
                        return item;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return item;
                }
                return null;
            }

            protected void onPostExecute(CommentsItem commentsItem) {
                isCommentLiked.onFetch(commentsItem, commentsAdapter);
            }

            ;

        }.execute();
    }

    public void isChildCommentLiked(final String commentId, final CommentsListAdapter.CommentGroupHolder commentsAdapter,
                                    final IsCommentLiked isCommentLiked) {

        new AsyncTask<Void, Void, Reply>() {

            @Override
            protected Reply doInBackground(Void... params) {
                // Boolean isExists = false;
                if (!isCancelled()) {
                    Reply item = null;
                    try {
                        item = getHelper().getReplyDao().queryForId(commentId);
                        return item;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return item;
                }
                return null;
            }

            protected void onPostExecute(Reply replyItem) {
                isCommentLiked.onFetchChild(replyItem, commentsAdapter);
            }

            ;

        }.execute();
    }

    @Override
    public void cleanUp() {

        if (mDatabaseHelper != null) {
            mDatabaseHelper.close();
            mDatabaseHelper = null;
        }
        sDatabaseManager = null;

    }
}
