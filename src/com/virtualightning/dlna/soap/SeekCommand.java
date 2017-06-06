package com.virtualightning.dlna.soap;

import com.virtualightning.dlna.SoapCommand;
import com.virtualightning.dlna.constant.DeviceType;

import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

public class SeekCommand extends SoapCommand {
    private final int instanceId;

    protected SeekCommand(int instanceId) {
        super(DeviceType.AV_TRANSPORT, "Seek");
        this.instanceId = instanceId;
    }

    @Override
    protected void writeCommand(TransformerHandler handler) throws SAXException {

    }
}
