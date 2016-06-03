/**
   Project      : Awaaz
   Filename     : Shows.java
   Author       : nagaraj
   Comments     : 
   Copyright    : © Copyright NDTV Convergence Limited 2011
					Developed under contract by Robosoft Technologies
   History      : NA
 */

package com.ndtv.core.shows.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Chandan kumar
 * 
 */
public class PrimeShows {
	@SerializedName("results")
	public List<PrimeShowItem> primeShowsList;

}
