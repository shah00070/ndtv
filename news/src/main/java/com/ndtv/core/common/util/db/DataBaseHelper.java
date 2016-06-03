/**
 Project      : Awaaz
 Filename     : DataBaseHelper.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ndtv.core.config.model.CommentsItem;
import com.ndtv.core.config.model.Reply;

/**
 * @author anudeep
 */
public class DataBaseHelper extends OrmLiteSqliteOpenHelper {
    private static String DATABASE_NAME = "NDTV_Database_New";
    private static int DATABASE_VERSION = 7;

    private Dao<CommentsItem, String> mCommentItemDao = null;
    private Dao<Reply, String> mReplyDao = null;

    /**
     * @param context
     * @param databaseName
     * @param factory
     * @param databaseVersion
     */
    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
        try {
            TableUtils.createTable(getConnectionSource(), CommentsItem.class);
            TableUtils.createTable(getConnectionSource(), Reply.class);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(DataBaseHelper.class.getName(), "Can't create database");
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int arg2, int arg3) {

    }

    public Dao<CommentsItem, String> getCommentIdDao() {
        if (null == mCommentItemDao) {
            try {
                mCommentItemDao = getDao(CommentsItem.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }catch(Exception ex){
               ex.printStackTrace();
            }
        }
        return mCommentItemDao;
    }

    public Dao<Reply, String> getReplyDao() {
        if (null == mReplyDao) {
            try {
                mReplyDao = getDao(Reply.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return mReplyDao;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#close()
     */
    @Override
    public void close() {
        mCommentItemDao = null;
        mReplyDao = null;
        super.close();

    }
}
