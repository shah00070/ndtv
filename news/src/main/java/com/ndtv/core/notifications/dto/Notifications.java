/**
 Project      : Awaaz
 Filename     : Videos.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.notifications.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Notifications {

    public String total;

    @SerializedName("results")
    public List<NotificationItem> notificationList;

}
