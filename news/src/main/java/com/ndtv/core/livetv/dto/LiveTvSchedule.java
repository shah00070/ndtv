/**
 Project      : Awaaz
 Filename     : Live.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.livetv.dto;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author laveen
 */
@Root(name = "live", strict = false)
public class LiveTvSchedule {
    @Element(name = "Schedule")
    public Schedule schedule;

}
