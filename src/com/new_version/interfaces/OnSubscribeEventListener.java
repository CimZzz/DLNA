package com.new_version.interfaces;


import com.new_version.Service;
import com.new_version.SubscribeEvent;

public interface OnSubscribeEventListener {
    void onSubscribeEvent(Service service, SubscribeEvent event);
}
