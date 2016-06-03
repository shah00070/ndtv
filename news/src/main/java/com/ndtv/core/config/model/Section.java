package com.ndtv.core.config.model;

import com.ndtv.core.util.FlavourUtils.TitleAbstraction;
import com.ndtv.core.util.StringUtils;

import java.util.List;

/**
 * Created by Srihari S Reddy on 12/11/14.
 */
public class Section extends TitleAbstraction {

    public String type;
    public String url;
    public String schedule;
    public int order;
    public String ad_site_id;
    public List<TabItem> tab;

    public String dfp_ad_site_id;

    public String getScheduleUrl() {
        return schedule;
    }

    public String getPlayUrl() {
        return url;
    }


    public List<TabItem> getTabList() {
        return tab;
    }

    public String getSectionUrl() {
        return StringUtils.decodeString(url);
    }

}
