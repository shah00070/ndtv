/**
 Project      : Awaaz
 Filename     : ShowItem.java
 Author       : nagaraj
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.shows.dto;

/**
 * @author Chandan kumar
 */
public class PrimeShowItem {

    public String id;
    public String image;
    public String link;
    public String name;
    public String slug;
    public boolean isHeader;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }


    public PrimeShowItem(String name) {
        this.name = name;
        this.isHeader = true;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PrimeShowItem) {
            return this.name.equalsIgnoreCase(((PrimeShowItem) o).name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.charAt(0);
    }

}
