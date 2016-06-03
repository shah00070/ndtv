package com.ndtv.core.ui;

public class NavigationItem {
    private String mText;
    private String mIconUrl;
    private boolean mdivider;

    public NavigationItem(String text, String icon,boolean div) {
        mText = text;
        mIconUrl = icon;
        mdivider=div;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String mIconUrl) {
        this.mIconUrl = mIconUrl;
    }

    public boolean isDivider(){
        return mdivider;
    }
}
