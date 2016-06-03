package com.ndtv.core.config.model;

import com.ndtv.core.util.StringUtils;

/**
 * Created by Srihari S Reddy on 12/11/14.
 */
public class Api {

    public String type;
    public String url;
    public String options;
    public String location;
    public String frequency;
    public String status;

public String getUrl()
{
    return StringUtils.decodeString(url);
}
}
