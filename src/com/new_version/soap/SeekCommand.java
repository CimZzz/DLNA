package com.new_version.soap;

import com.constant.DeviceType;
import com.new_version.Instance;
import com.new_version.SoapCommand;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

public class SeekCommand extends SoapCommand {
    private final Instance instance;

    protected SeekCommand(Instance instance) {
        super(DeviceType.AV_TRANSPORT, "Seek");
        this.instance = instance;
    }

    @Override
    protected void writeCommand(TransformerHandler handler) throws SAXException {

    }
}
