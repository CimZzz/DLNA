package com.new_version;

import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;


public class XmlEventHandler extends DefaultHandler {
    private final SubscribeEvent subscribeEvent;

    XmlEventHandler(SubscribeEvent subscribeEvent) {
        this.subscribeEvent = subscribeEvent;
        this.subscribeEvent.feature = new HashMap<>();
    }
}
