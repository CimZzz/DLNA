package com.virtualightning.dlna.soap;

import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

import com.virtualightning.dlna.SoapCommand;
import com.virtualightning.dlna.constant.DeviceType;
import com.virtualightning.dlna.tools.TimeUtils;

public class SeekCommand extends SoapCommand {
    private final int instanceId;
    private final long position;

    public SeekCommand(int instanceId, long position) {
        super(DeviceType.AV_TRANSPORT, "Seek");
        this.instanceId = instanceId;
        this.position = position;
    }

    @Override
    protected void writeCommand(TransformerHandler handler) throws SAXException {
        String instanceIdStr = String.valueOf(instanceId);
        startElement("InstanceID");
        handler.characters(instanceIdStr.toCharArray(),0,instanceIdStr.length());
        endElement("InstanceID");

        String unitStr = "REL_TIME";
        startElement("Unit");
        handler.characters(unitStr.toCharArray(),0,unitStr.length());
        endElement("Unit");

        String timeStr = TimeUtils.millis2Str(position);
        System.out.println("Time Str : " + timeStr);
        startElement("Target");
        handler.characters(timeStr.toCharArray(),0,timeStr.length());
        endElement("Target");

    }
}
