/**
 Project      : Awaaz
 Filename     : BaseManager.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.config;

/**
 * @author anudeep
 */
public abstract class BaseManager {

    public interface ConfigDownloadListener {
        void onConfigDownloadComplete();

        void onConfigDownloadFailed();
    }


    public interface DownloadListener {
        void onDownloadSucces();

        void onDownloadFailed();
    }

    public abstract void cleanUp();

    public interface AddLikeDislikeListener {
        void onSuccess();

        void onFailed();
    }

}
