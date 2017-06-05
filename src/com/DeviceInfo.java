package com;

/**
 * Created by xjw04 on 17/5/31.
 */
public class DeviceInfo {
    String IP;
    int port;
    String host;
    String avtControlPath;
    String avtEventSubPath;
    String location;

    String USN;
    String UUID;
    String server;
    String deviceType;
    String NTS;

    String friendlyName;

    Long lastActiveTime;
    Long cacheLong;

    String subscribeId;
    Long lastSubscribeTime;
    Long timeOut;//超时时间

    boolean isGetCompleted;//是否获取完整
    boolean isSubscibing;//正在订阅中状态

    DeviceInfo() {
        isSubscibing = false;
    }


    public String getFriendlyName() {
        return friendlyName;
    }

    public Long getCacheLong() {
        return cacheLong;
    }
}
