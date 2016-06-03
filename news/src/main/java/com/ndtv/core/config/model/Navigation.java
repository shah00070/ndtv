package com.ndtv.core.config.model;

import android.text.TextUtils;

import com.ndtv.core.util.FlavourUtils.TitleAbstraction;

import java.util.List;

/**
 * Created by Srihari S Reddy on 12/11/14.
 */
public class Navigation extends TitleAbstraction {

    public List<Section> section;

    public String menu_icon;

    public String type;

    public String url;

    public String dfp_ad_site_id;

    private String divider;

    public List<Section> getSectionList() {
        return section;
    }

    public boolean showDivider() {
        if (TextUtils.isEmpty(divider)) return false;
        else return true;
    }
}
