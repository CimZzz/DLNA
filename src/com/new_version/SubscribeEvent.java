package com.new_version;

import java.util.HashMap;

/**
 * Created by xjw04 on 17/6/1.
 */
public class SubscribeEvent {
    public static final int ACTION_SUBSCRIBE_SUCCESS = 0;
    public static final int ACTION_SUBSCRIBE_FAILED = 1;
    public static final int ACTION_SUBSCRIBE_CON_FAILED = 2;
    public static final int ACTION_RENEW_SUBSCRIBE_SUCCESS = 3;
    public static final int ACTION_RENEW_SUBSCRIBE_FAILED = 4;
    public static final int ACTION_RENEW_SUBSCRIBE_CON_FAILED = 5;
    public static final int ACTION_CANCEL_SUBSCRIBE_SUCCESS = 6;
    public static final int ACTION_CANCEL_SUBSCRIBE_FAILED = 7;
    public static final int ACTION_CANCEL_SUBSCRIBE_CON_FAILED = 8;
    public static final int ACTION_SUBSCRIBE_RESPONSE = 9;//第一次订阅响应信息（根据xml文件是否包含TransportStatus标签）
    public static final int ACTION_STATE_CHANGE = 10;//订阅设备状态变化

    int actionId;
    long seq;
    HashMap<String,String> feature;

    public int getAction() {

        return actionId;
    }
}
