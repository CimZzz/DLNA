package com.interfaces;

import com.DeviceInfo;
import com.SubscribeEvent;

public interface OnSubscribeEventListener {
    void onSubscribeEvent(DeviceInfo deviceInfo,SubscribeEvent event);
}
