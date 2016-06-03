/**
   Project      : Awaaz
   Filename     : PrimeVideoItem.java
   Author       : nagaraj
   Comments     : 
   Copyright    : © Copyright NDTV Convergence Limited 2011
					Developed under contract by Robosoft Technologies
   History      : NA
 */

package com.ndtv.core.shows.dto;

import com.google.gson.annotations.SerializedName;

/**
 * @author Chandan kumar
 * 
 */
public class PrimeVideoItem {

	public String id;
	public String title;
	public String link;
	public String pubDate;

	@SerializedName("media:thumbnail")
	public String thumbnail;
	@SerializedName("media:duration")
	public String duration;
	public String description;
	@SerializedName("media:filepath")
	public String url;
	@SerializedName("media:keywords")
	public String keyword;

	@SerializedName("media:fullimage")
	public String fullImage;

}
