package com.rasppi;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jerome
 * Date: 10/02/2014
 * Time: 9:15 AM
 * To change this template use File | Settings | File Templates.
 */
public enum BuildStatus {
    STABLE ("<color>blue</color>"),
    BROKEN ("<color>red</color>"),
    BUILDING ("<color>blue_anime</color>"),
    UNKNOWN ("UNKNOWN");

    private final String buildStatus;

    private static final Map<String, BuildStatus> lookup = new HashMap<String, BuildStatus>();

    static {
        for (BuildStatus s : EnumSet.allOf(BuildStatus.class)){
            lookup.put(s.getStatus(), s);
        }
    }

    BuildStatus(String status) {
        this.buildStatus = status;
    }

    public String getStatus(){
        return this.buildStatus;
    }

    public static BuildStatus getBuildStatus(String status){
        try {
            return lookup.get(status);
        }
        catch (Exception e){
            return UNKNOWN;
        }
    }
}
