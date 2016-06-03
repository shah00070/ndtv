/**
 Project      : Awaaz
 Filename     : Schedule.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.livetv.dto;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * @author anudeep
 */
@Root(strict = false)
public class Schedule {

    @Attribute(required = false)
    public String channel;

    @Element
    public String title;

    @ElementList(inline = true)
    public List<Program> programeList;

}
