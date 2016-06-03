package com.ndtv.core.config.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Comments {
	@SerializedName("items")
	public List<CommentsItem> commentsItemsList;
	public Pager pager;

}
