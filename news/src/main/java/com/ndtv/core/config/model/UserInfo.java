/**
   Project      : Awaaz
   Filename     : UserInfo.java
   Author       : adithya
   Comments     : 
   Copyright    : © Copyright NDTV Convergence Limited 2011
					Developed under contract by Robosoft Technologies
   History      : NA
 */

package com.ndtv.core.config.model;

/**
 * @author adithya
 * 
 */
public class UserInfo {
	public String uid;
	public String first_name;
	public String last_name;
	public String profile_image;
	public String site_name;
	public String access_token;

	public String toString() {
		String uinfo = "user_id=" + uid + "&first_name=" + first_name + "&last_name=" + last_name + "&profile_image="
				+ uid + "&sitename=" + site_name;
		return uinfo;
	}
}