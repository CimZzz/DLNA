package com.new_version.soap;

import com.constant.DeviceType;
import com.new_version.DLNAClient;
import com.new_version.Instance;
import com.new_version.SoapCommand;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

public class SetURICommand extends SoapCommand {
    private final Instance instance;
    private final String path;

    public SetURICommand(Instance instance,String path) {
        super(DeviceType.AV_TRANSPORT, "SetAVTransportURI");
        this.path = path;
        this.instance = instance;
    }

    public SetURICommand(DLNAClient client,Instance instance,String path) {
        this(instance,"http://" + client.getHostAddr() + ":" + client.getHTTPPort() + "/" + path);
    }



    @Override
    protected void writeCommand(TransformerHandler handler) throws SAXException {
        String instanceIdStr = String.valueOf(instance.getInstanceId());
        startElement("InstanceID");
        handler.characters(instanceIdStr.toCharArray(),0,instanceIdStr.length());
        endElement("InstanceID");

        startElement("CurrentURI");
        handler.characters(path.toCharArray(),0,path.length());
        endElement("CurrentURI");

        startElement("CurrentURIMetaData");
        endElement("CurrentURIMetaData");
    }
}
