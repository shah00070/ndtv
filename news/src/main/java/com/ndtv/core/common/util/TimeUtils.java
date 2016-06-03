/**
 Project      : Awaaz
 Filename     : TimeUtils.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util;

import android.content.Context;
import android.text.TextUtils;

import com.ndtv.core.R;
import com.ndtv.core.constants.ApplicationConstants.DateKeys;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author anudeep
 */
public class TimeUtils implements DateKeys {
    private static final String TIME_FORMAT = "HH:mm";

    public static String getRelativeTime(Context mContext, long milliseconds2) {
        String time = "";
        long milliseconds1 = Calendar.getInstance().getTimeInMillis();

        long diff, minutes, hours, days;
        diff = milliseconds1 - milliseconds2;

        days = diff / (24 * 60 * 60 * 1000);

        if (days > 1) {
            time = days + " " + mContext.getString(R.string.days_ago);
        } else if (days == 1) {
            time = mContext.getString(R.string.one_day_ago);
        } else {
            hours = diff / (60 * 60 * 1000);
            if (hours > 1) {
                time = hours + " " + mContext.getString(R.string.hours_ago);
            } else if (hours == 1) {
                time = mContext.getString(R.string.one_hour_ago);
            } else {
                minutes = diff / (60 * 1000);
                if (minutes > 1) {
                    time = minutes + " " + mContext.getString(R.string.minutes_ago);
                } else
                    time = mContext.getString(R.string.one_minute_ago);
            }
        }

        return time;
    }

    public static Date getSearchNewsDate(String pubDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(NEWS_DATE_FORMAT, Locale.US);
        Date date = null;
        try {
            date = dateFormat.parse(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return date;
    }

    public static Date getNewsDate(String pubDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        Date date = null;
        try {
            date = dateFormat.parse(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return date;
    }

    public static String getWeekDay(String pubDate, Context ctx) {
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        String day = "";
        try {
            Date date = dateFormat.parse(pubDate);
            dateFormat = new SimpleDateFormat("EEEE");
            day = dateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return day;

    }


    public static String getLastUpdatedTime(String pubDate) {
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        String time = "";
        try {
            Date date = dateFormat.parse(pubDate);
            dateFormat = new SimpleDateFormat("MMMM dd, yyy hh:mm a");
            time = dateFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();

        }
        return time;

    }

    public static String getHoursAndMinutes(int minutes) {
        int min = minutes % 60;
        int hour = minutes / 60;
        return (MessageFormat.format("{0}{1}:{2}{3}", (hour < 10 ? "0" : ""), hour, (min < 10 ? "0" : ""), min));
    }

//	public static String getVideoTime(int minutes, Context ctx) {
//		int min = minutes % 60;
//		int hour = minutes / 60;
//		return MessageFormat.format("{0} {1}, {2} {3}", hour, ctx.getString(R.string.minutes), min,
//                ctx.getString(R.string.seconds));
//	}

    public static Date getDate(String pubDate) {
        Date date = null;
        SimpleDateFormat simpleDate = new SimpleDateFormat(NEWS_DATE_FORMAT, Locale.US);
        try {
            date = simpleDate.parse(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getPublishDate(String pubDate, Context ctx) {
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat(NEWS_DATE_FORMAT, Locale.US);
        String time = null;
        try {
            Date date = dateFormat.parse(pubDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            dateFormat = new SimpleDateFormat("MMMM dd, yyy ");
            time = dateFormat.format(calendar.getTime());
            if (!TextUtils.isEmpty(time)) {
                return MessageFormat.format("{0} {1}  |", ctx.getString(R.string.published_on), time);

            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";

        }

        return "";
    }

    public static String getShowTime(String time) {
        Date date = getShowDate(time, NEWS_DATE_FORMAT);
        DateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(date);
    }

    public static Date getShowDate(String pubDate, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
        Date date = null;
        try {
            date = dateFormat.parse(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return date;
    }

    public static synchronized boolean isValidTime(String time) {
        Calendar currentCalender = Calendar.getInstance(Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.US);
        try {
            Date date = dateFormat.parse(time);
            Calendar calender = Calendar.getInstance();
            calender.setTime(date);

            int hour = calender.get(Calendar.HOUR_OF_DAY);
            int min = calender.get(Calendar.MINUTE);
            int currentHour = currentCalender.get(Calendar.HOUR_OF_DAY);
            int currentMin = currentCalender.get(Calendar.MINUTE);
            if (hour == currentHour)
                if (min == 0) {
                    if (currentMin >= 0 && currentMin < 30) {
                        return true;
                    } else
                        return false;
                } else {
                    if (currentMin >= 30 && currentMin <= 59) {
                        return true;
                    } else
                        return false;
                }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;

    }


    public static boolean isValidDate(Context ctx) {
        //TODO remove comments later
        PreferencesManager prefMngr = PreferencesManager.getInstance(ctx);
        Date startDate = TimeUtils.getDate(prefMngr.getAdStratDate());
//        Date startDate = TimeUtils.getDate("Mon, 23 Feb 2015 00:00:00 +0530");
        Date endDate = TimeUtils.getDate(prefMngr.getAdEndDate());
//        Date endDate = TimeUtils.getDate("Tue, 24 Mar 2015 23:59:59 +0530");

        if (startDate != null && endDate != null && Calendar.getInstance().getTime().after(startDate)
                && Calendar.getInstance().getTime().before(endDate)) {
            return true;
        }
        return false;
    }

    public static String getCurrentDate() {
        long date = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateString = sdf.format(date);
        return dateString;
    }

    public static int getCurrentHour() {
        long time = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return  hour;
    }

}
