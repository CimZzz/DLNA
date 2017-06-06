package com.virtualightning.dlna.soap;

import com.virtualightning.dlna.SoapCommand;
import com.virtualightning.dlna.constant.DeviceType;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

public class StopCommand extends SoapCommand {
    private final int instanceId;

    public StopCommand(int instanceId) {
        super(DeviceType.AV_TRANSPORT, "Stop");
        this.instanceId = instanceId;
    }

    @Override
    protected void writeCommand(TransformerHandler handler) throws SAXException {
        String instanceIdStr = String.valueOf(instanceId);
        startElement("InstanceID");
        handler.characters(instanceIdStr.toCharArray(),0,instanceIdStr.length());
        endElement("InstanceID");
    }
}
