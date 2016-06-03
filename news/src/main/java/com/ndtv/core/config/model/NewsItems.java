package com.ndtv.core.config.model;

import android.text.TextUtils;

import com.ndtv.core.common.util.TimeUtils;
import com.ndtv.core.util.FlavourUtils.TitleAbstraction;
import com.ndtv.core.util.StringUtils;

import java.util.Date;

/**
 * Created by Srihari S Reddy on 04/01/15.
 */


//    {
//        "id":"237613",
//        "nodes":{
//        "type":"story",
//        "cache":1,
//        "stype":{
//        "t":"631 reading",
//        "tc":"#70ac33"
//        }
//        },
//        "title":"IPL Auction: Yuvraj Make Jackpot; Karthik Among Big Buys Again",
//        "link":"http:\/\/sports.ndtv.com\/indian-premier-league-2015\/news\/237613-ipl-2015-player-auctions-live",
//        "updatedAt":"February 16, 2015 10:24 PM",
//        "pubDate":"February 16, 2015 10:24 PM",
//        "thumb_image":"http:\/\/s.ndtvimg.com\/images\/content\/2014\/nov\/120\/yuvraj-singh-ipl.jpg",
//        "type":"story",
//        "keywords":"Aaron Finch,Hashim Amla,Yuvraj Singh,IPL 8,Cricket",
//        "identifier":"story-sports-237613",
//        "category":"sports",
//        "device":"http:\/\/www.ndtv.com\/article\/view\/sports\/237613\/site=classic\/?device=androidv2&showads=no&site=classic"
//        }

public class NewsItems extends TitleAbstraction {
    public String id;
    public String thumb_image;
    public String pubDate;
    public String updatedAt;
    public String link;
    public String story_image;
    public String by_line;
    public String device;
    public Node nodes;
    public String category;
    public String identifier;
    public String applink;
    public String type;

    public Date getSearchPublishDate() {
        if (!TextUtils.isEmpty(pubDate)) {
            return TimeUtils.getSearchNewsDate(pubDate);
        } else {
            return null;
        }
    }

    public String getApplink(){
        return StringUtils.decodeString(applink);
    }
}
