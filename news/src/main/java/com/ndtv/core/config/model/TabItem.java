package com.ndtv.core.config.model;

import com.ndtv.core.util.FlavourUtils.TitleAbstraction;

/*
@author Chandan kumar
 */
public class TabItem extends TitleAbstraction {

    public String type;
    public String url;

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

}
