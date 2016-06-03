package com.ndtv.core.config.model;


import com.google.android.gms.ads.formats.NativeContentAd;

public class Photos {

    public String description;
    public String fullimage;
    public String id;
    public String link;
    public String reporter;
    public String source;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullimage() {
        return fullimage;
    }

    public void setFullimage(String fullimage) {
        this.fullimage = fullimage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getThumbimage() {
        return thumbimage;
    }

    public void setThumbimage(String thumbimage) {
        this.thumbimage = thumbimage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String thumbimage;
    public String title;

    //Changes For Native Ad's
    public transient boolean isAdPage;
    public transient NativeContentAd nativeContentAd;
}
