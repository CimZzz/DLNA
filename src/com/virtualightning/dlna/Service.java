package com.virtualightning.dlna;

import java.util.HashMap;

public class Service {
    DeviceInfo deviceInfo;//所属设备
    String serviceType;//服务类型
    String serviceId;//服务ID
    String controlURL;//SOAP控制地址
    String eventSubURL;//GENA订阅地址
    String SCPDURL;//服务描述文档地址

    int specMajorVersion;//主要版本
    int specMinorVersion;//次要版本

    SubscribeItem subscribeItem;//订阅项

    HashMap<String,Action> actionMap = null;//动作表
    HashMap<String,StateVariable> stateVariableMap = null;//变量表

    //判断是否需要重新订阅
    synchronized boolean isNeedSubscribe() {
        if(subscribeItem == null) {
            subscribeItem = new SubscribeItem();
            return true;
        }
        long timeDifference = System.currentTimeMillis() - subscribeItem.lastSubscribeTime;

        if(timeDifference > subscribeItem.timeOut) {
            subscribeItem.isAvaliable = false;
            return true;
        }

        return false;
    }

    synchronized boolean checkSubscribingState(boolean flag) {
        if(flag && subscribeItem.isCancelSubscrbing)
            return false;

        boolean returnVal = subscribeItem.isSubscrbing == flag;

        if(!returnVal)
            subscribeItem.isSubscrbing = flag;

        return returnVal;
    }

    synchronized boolean checkCancelSubscribeState(boolean flag) {
        boolean returnVal = subscribeItem.isCancelSubscrbing == flag;

        if(!returnVal)
            subscribeItem.isCancelSubscrbing = flag;

        return returnVal;
    }

    synchronized boolean checkCancelSubscribeState() {
        return subscribeItem.isCancelSubscrbing;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public HashMap<String, StateVariable> getStateVariableMap() {
        return stateVariableMap;
    }
}
