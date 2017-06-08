package com.virtualightning.dlna.soap;

import org.xml.sax.SAXException;

import javax.xml.transform.sax.TransformerHandler;

import com.virtualightning.dlna.DLNAClient;
import com.virtualightning.dlna.SoapCommand;
import com.virtualightning.dlna.constant.DeviceType;

public class SetURICommand extends SoapCommand {
    private final int instanceId;
    private final String path;

    public SetURICommand(int instanceId,String path) {
        super(DeviceType.AV_TRANSPORT, "SetAVTransportURI");
        this.path = path;
        this.instanceId = instanceId;
        System.out.println("SetURI : " + path);
    }

    public SetURICommand(DLNAClient client, int instanceId, String path) {
        this(instanceId,"http://" + client.getHostAddr() + ":" + client.getHTTPPort() + "/" + path);
    }



    @Override
    protected void writeCommand(TransformerHandler handler) throws SAXException {
        String instanceIdStr = String.valueOf(instanceId);
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
