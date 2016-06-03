/**
 Project      : Awaaz
 Filename     : VideoItem.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.video.dto;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.ndtv.core.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author nagaraj
 */
//@DatabaseTable
public class VideoItem {


    //@DatabaseField(id = true)
    public String id;
    //@DatabaseField(canBeNull = true)
    public String title;
    //@DatabaseField(canBeNull = true)
    public String link;
    //@DatabaseField(canBeNull = true)
    public String pubDate;

    @SerializedName("media:thumbnail")
    //DatabaseField(canBeNull = true)
    public String thumbnail;

    @SerializedName("media:duration")
    //@DatabaseField(canBeNull = true)
    public String duration;

    //@DatabaseField(canBeNull = true)
    public String description;
    //@DatabaseField(canBeNull = true)
    @SerializedName("media:filepath")
    public String url;
    //@DatabaseField(canBeNull = true)
    @SerializedName("media:keywords")
    public String keyword;

    //@DatabaseField(canBeNull = true)
    @SerializedName("media:fullimage")
    public String fullImage;

    public boolean isEmpty = false;

    private String formattedDate;

    public String getVideoDuration() {
        if (TextUtils.isEmpty(formattedDate)) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("s");
                Date date = format.parse(duration);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                StringBuilder builder = new StringBuilder();
                int value = calendar.get(Calendar.HOUR);
                if (value > 0) {
                    builder.append(value + ":");
                }
                value = calendar.get(Calendar.MINUTE);
                if (value < 10) {
                    builder.append("0");
                }
                builder.append(value + ":");
                value = calendar.get(Calendar.SECOND);
                if (value < 10) {
                    builder.append("0");
                }
                builder.append(value);
                formattedDate = builder.toString();
            } catch (ParseException e) {
                e.printStackTrace();
                formattedDate = "";
            }
        }

        return formattedDate;
    }

    public String getVideoDescription() {
        return StringUtils.decodeString(description);
    }

    public String getVideoTitle() {
        return StringUtils.decodeString(title);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
