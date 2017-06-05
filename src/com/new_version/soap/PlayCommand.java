package com.new_version.soap;

import com.new_version.Instance;
import com.new_version.SoapCommand;
import com.new_version.constant.DeviceType;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

public class PlayCommand extends SoapCommand {
    private final Instance instance;
    private final int speed;

    public PlayCommand(Instance instance) {
        this(instance,1);
    }

    public PlayCommand(Instance instance, int speed) {
        super(DeviceType.AV_TRANSPORT, "Play");
        this.instance = instance;
        this.speed = speed;
    }

    @Override
    protected void writeCommand(TransformerHandler handler) throws SAXException {
        String instanceIdStr = String.valueOf(instance.getInstanceId());
        startElement("InstanceID");
        handler.characters(instanceIdStr.toCharArray(),0,instanceIdStr.length());
        endElement("InstanceID");

        String speedStr = String.valueOf(speed);
        startElement("Speed");
        handler.characters(speedStr.toCharArray(),0,speedStr.length());
        endElement("Speed");
    }
}
