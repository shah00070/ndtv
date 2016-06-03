package com.ndtv.core.radio.dto;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by sangeetha on 29/1/15.
 */
@Root(name = "live", strict = false)
public class LiveRadioSchedules {

    @Element(name = "Schedule")
    public ScheduleTag schedule;
}
