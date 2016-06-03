package com.ndtv.core.config.model;

import java.util.List;

/**
 * Created by Srihari S Reddy on 04/01/15.
 */
public class NewsFeed {

    public List<NewsItems> results;
    public int total;

    public List<NewsItems> getResults() {
        return results;
    }
}
