/**
 Project      : Awaaz
 Filename     : Videos.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.video.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author nagaraj
 */
public class Videos {

    public int total;

    @SerializedName("results")
    public List<VideoItem> videoList;

}
