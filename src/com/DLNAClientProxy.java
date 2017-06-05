package com;

import java.io.File;

public class DLNAClientProxy {
    private DLNAClient client;

    DLNAClientProxy(DLNAClient client) {
        this.client = client;
    }

    void subscibeEvent(DeviceInfo info,SubscribeEvent event) {
        if(client != null)
            client.onSubscribeEvent(info, event);
    }

    void error(int errorCode,Exception e,Object... args) {
        if(client != null)
            client.onError(errorCode,e,args);
    }

    void info(int what,int other) {
        if(client != null)
            client.onInfo(what,other);
    }

    DeviceInfo findDeviceInfoBySID(String sid) {
        if(client != null)
            return client.findDeviceInfoBySID(sid);
        else return null;
    }

    File getResourcesFile(String substring) throws Exception {
        if(client != null)
            return client.getResourcesFile(substring);
        else return null;
    }

    void findNewDevice(DeviceInfo info) {
        if(client != null)
            client.findNewDevice(info);
    }
    void deviceQuit(DeviceInfo info) {
        if(client != null)
            client.deviceQuit(info);
    }

    String getHostAddr() {
        if(client != null)
            return client.hostAddr;
        else return "";
    }
    void subscribe(DeviceInfo deviceInfo) {
        if(client != null)
            client.subscribe(deviceInfo);
    }

    void close() {
        client = null;
    }

}
