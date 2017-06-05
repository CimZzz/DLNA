package com.new_version;

import java.util.HashMap;

public class Instance {
    private int instanceId;//instanceId
    private HashMap<String,String> featureMap;


    Instance(int instanceId) {
        this.instanceId = instanceId;
        this.featureMap = new HashMap<>();
    }


    public int getInstanceId() {
        return instanceId;
    }
}
