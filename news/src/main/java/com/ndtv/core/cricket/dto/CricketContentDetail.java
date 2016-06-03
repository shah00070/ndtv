package com.ndtv.core.cricket.dto;

import java.util.List;

/**
 * Created by laveen on 1/4/15.
 */
public class CricketContentDetail {

    public List<Disclaimer>disclaimer;

    public static class Disclaimer {
        public String type;
        public String name;
        public String message;
        public String position;
        public String subsection;

        public String getName(){
            return name;
        }

        public String getSubsection(){
            return subsection;
        }

        public String getMessage(){
            return  message;
        }

        public String getPosition(){
            return  position;
        }
    }
}
