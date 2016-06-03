/**
 Project      : Awaaz
 Filename     : VideoItem.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.notifications.dto;

//import com.j256.ormlite.field.DatabaseField;
//import com.j256.ormlite.table.DatabaseTable;

import java.util.List;


//@DatabaseTable
public class NotificationItem {

    public static final String NDTV_SITE = "http://www.ndtv.com/";

    //@DatabaseField(id = true)
    public String id;

    //@DatabaseField(canBeNull = true)
    public String title;

    //@DatabaseField(canBeNull = true)
    public String pubdate;

    //@DatabaseField(canBeNull = true)
    public String applink;
    public String imageLink;
    public Metadata metadata;
    public List<String> category;

    public static final String link = NDTV_SITE;

}
