package com.ndtv.core.config.model;

import android.content.Context;

import com.ndtv.core.util.StringUtils;

/**
 * Created by Srihari S Reddy on 17/02/15.
 */
public class Stype {

    public String t;
    public String tc;

    public Stype(String t, String tc) {
        this.t = t;
        this.tc = tc;
    }

    public String getStypeT(Context ctx) {
//        if (ctx != null) {
//            String Buildflavor = ctx.getPackageName();
//
//            if (Buildflavor.equalsIgnoreCase("com.ndtv.india")) {
//                return StringUtils.decodeString(t);
//            } else
//                return t;
//        }
        return t;
    }
}
