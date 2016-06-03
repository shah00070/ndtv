package com.ndtv.core.newswidget.dto;

import com.ndtv.core.common.util.TimeUtils;
import com.ndtv.core.util.StringUtils;

import java.util.List;

/**
 * Created by laveen on 10/12/14.
 *
 * @author Laveen
 * @modified Harisha B
 */
public class NewsWidget {

    public int recordcount;
    public List<NewsWidgetItem> item;

    public List<NewsWidgetItem> getItem() {
        return item;
    }

    public static class NewsWidgetItem implements Comparable<NewsWidgetItem>{
        public String contentType;
        public String title;
        public String time;
        public String link;
        public WidgetExtra extraData;
        public String description;
        public String image;

        public String getTitle() {
            return StringUtils.decodeStringWithoutCharset(title);
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NewsWidgetItem other = (NewsWidgetItem) obj;
            if (this.extraData.id == null) {
                if (other.extraData.id != null)
                    return false;
            } else if (!this.extraData.id.equals(other.extraData.id))
                return false;
            return true;

        }

        @Override
        public int compareTo(NewsWidgetItem another) {
             return TimeUtils.getNewsDate(another.time).compareTo(TimeUtils.getNewsDate(this.time));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((extraData.id == null) ? 0 : extraData.id.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "NewsWidgetItem{" +
                    "contentType='" + contentType + '\'' +
                    ", title='" + title + '\'' +
                    ", time='" + time + '\'' +
                    ", link='" + link + '\'' +
                    ", extraData=" + extraData.toString() +
                    ", description='" + description + '\'' +
                    ", image='" + image + '\'' +
                    '}';
        }
    }

    public static class WidgetExtra {
        public String type;
        public String image;
        public String description;
        public String applink;
        public int height;
        public String category;
        public String id;

        @Override
        public String toString() {
            return "WidgetExtra{" +
                    "type='" + type + '\'' +
                    ", image='" + image + '\'' +
                    ", description='" + description + '\'' +
                    ", applink='" + applink + '\'' +
                    ", height=" + height +
                    ", category='" + category + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }


}

