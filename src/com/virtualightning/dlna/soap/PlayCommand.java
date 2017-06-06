package com.virtualightning.dlna.soap;

import com.virtualightning.dlna.SoapCommand;
import com.virtualightning.dlna.constant.DeviceType;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

public class PlayCommand extends SoapCommand {
    private final int instanceId;
    private final int speed;

    public PlayCommand(int instanceId) {
        this(instanceId,1);
    }

    public PlayCommand(int instanceId, int speed) {
        super(DeviceType.AV_TRANSPORT, "Play");
        this.instanceId = instanceId;
        this.speed = speed;
    }

    @Override
    protected void writeCommand(TransformerHandler handler) throws SAXException {
        String instanceIdStr = String.valueOf(instanceId);
        startElement("InstanceID");
        handler.characters(instanceIdStr.toCharArray(),0,instanceIdStr.length());
        endElement("InstanceID");

        String speedStr = String.valueOf(speed);
        startElement("Speed");
        handler.characters(speedStr.toCharArray(),0,speedStr.length());
        endElement("Speed");
    }
}
