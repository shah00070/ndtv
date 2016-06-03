/**
 Project      : Awaaz
 Filename     : Program.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.livetv.dto;

import android.text.SpannableString;
import android.text.TextUtils;

import com.ndtv.core.common.util.Utility;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author anudeep
 */
@Root(name = "program", strict = false)
public class Program {

    @Element(required = false)
    public String progid;
    @Element(data = true, required = false)
    public String name;
    @Element(data = true, required = false)
    public String desc;
    @Element(required = false)
    public String timestamp;
    @Element(data = true, required = false)
    public String image;
    @Element(data = true, required = false)
    public String thumb;

    public int fadeSate;
    public float fadeSize;

    public SpannableString topTimeString;
    public SpannableString bottomTimeString;

    private String showTime;

    private boolean isShowAvailable= true;

    public void setShowAvailabilit(boolean state){
        isShowAvailable = state;
    }

    public boolean isShowAvailable(){
        if(TextUtils.isEmpty(desc) && TextUtils.isEmpty(thumb))
            return false;
       return true;
    }

    public void setShowTime(String time) {
        showTime = time;
    }

    public String getShowTime() {
        return showTime;
    }

    public Date date;

    public void setProgramTime(Date showDate) {
        date = showDate;
    }

    public Date getProgramTime() {
        if (date == null) {
            date = Utility.getProgramTime(this);
        }
        return date;
    }

}
