package com.ndtv.core.radio.dto;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by sangeetha on 29/1/15.
 */
@Root(strict = false, name = "program")
public class ProgramItem {
    @Element(required = false)
    public String progid;

    @Element(data = true, required = false)
    public String name;

    @Element(data = true, required = false)
    public String desc;

    @Element(required = false)
    public String timestamp;

    @Element(data = true, required = false)
    public String image;

    @Element(data = true, required = false)
    public String thumb;


}

