package com.new_version;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DeviceInfo {
    String IP;//IP地址
    int port;//端口
    String host;//主机地址

    long lastActiveTime;//上一次激活时间，和生命周期相关
    long cacheTime;//存活时间，如果当前时间减去上一次激活时间大于存活时间，则认定此设备已注销

    int specMajorVersion;//主要版本
    int specMinorVersion;//次要版本

    String USN;//单一设备名
    String location;//DDD地址
    String UUID;//通用唯一识别码
    String friendlyName;//设备名
    String manufacturer;//制造商
    String manufacturerURL;//制造商地址
    String modelName;//模块名
    String modelNumber;//模块编号
    String modelDescription;//模块介绍
    String modelURL;//模块网址
    String serialNumber;//序列号
    String deviceType;//设备类型
    String server;//设备服务器
    HashMap<String,String> otherDesc = new HashMap<>();//其他非基本内容

    List<Icon> iconList = new LinkedList<>();//图片列表
    HashMap<String,Service> serviceMap = new HashMap<>();//服务列表

//    String NTS; NTS 通知消息子类型 去掉


    public String getUSN() {
        return USN;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public HashMap<String, Service> getServiceMap() {
        return serviceMap;
    }
}
