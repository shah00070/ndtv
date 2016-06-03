package com.ndtv.core.radio.dto;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by sangeetha on 29/1/15.
 */
@Root(strict = false)
public class ScheduleTag {
    @Element
    public String title;

    @Attribute
    public String channel;

    @ElementList(inline = true)
    public List<ProgramItem> programList;
}
