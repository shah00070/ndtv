/**
 Project      : Awaaz
 Filename     : PrimeVideos.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.shows.dto;

import com.google.gson.annotations.SerializedName;
import com.ndtv.core.video.dto.VideoItem;

import java.util.List;


/**
 * @author Chandan kumar
 */
public class PrimeVideos {

    public String total;

    @SerializedName("results")
    public List<VideoItem> videoList;

}
