package com.ndtv.core.config.model;


import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

@DatabaseTable
public class CommentsItem {
    @DatabaseField(id = true)
    public String cid;
    public String comment;
    public String content_id;
    public String created;
    @DatabaseField
    public String likes;
    public String name;
    public String parent_cid;
    public String uid;
    @DatabaseField
    public String unlikes;
    public String userimage;

    @SerializedName("reply")
    public List<Reply> replyItems;
    @DatabaseField
    public boolean isLiked;
    @DatabaseField
    public boolean isDisliked;
}
