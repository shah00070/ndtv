package com.ndtv.core.config.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Reply {
    @DatabaseField(id = true)
    public String cid;
    private String comment;
    private String content_id;
    private String created;
    @DatabaseField
    public String likes;
    private String name;
    private String parent_cid;
    private String uid;

    @DatabaseField
    public String unlikes;
    private String userimage;

    @DatabaseField
    public boolean isLiked;
    @DatabaseField
    public boolean isDisliked;

    public String getCid() {
        return this.cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getContent_id() {
        return this.content_id;
    }

    public void setContent_id(String content_id) {
        this.content_id = content_id;
    }

    public String getCreated() {
        return this.created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLikes() {
        return this.likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent_cid() {
        return this.parent_cid;
    }

    public void setParent_cid(String parent_cid) {
        this.parent_cid = parent_cid;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUnlikes() {
        return this.unlikes;
    }

    public void setUnlikes(String unlikes) {
        this.unlikes = unlikes;
    }

    public String getUserimage() {
        return this.userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }
}