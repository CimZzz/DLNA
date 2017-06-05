package com.new_version;

import com.new_version.tools.XmlAnalyzeStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlDDDHandler extends DefaultHandler {
    private Icon icon;
    private Service service;
    private final DeviceInfo deviceInfo;
    private final XmlAnalyzeStream.AnalyzerChain analyzerChain;

    XmlDDDHandler(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;

        XmlAnalyzeStream sepcVersion;
        XmlAnalyzeStream device;
        XmlAnalyzeStream iconList;
        XmlAnalyzeStream serviceList;
        XmlAnalyzeStream iconStream;
        XmlAnalyzeStream serviceStream;
        XmlAnalyzeStream root = new XmlAnalyzeStream("root",false)
                .addChildElement(sepcVersion = new XmlAnalyzeStream("specVersion",false))
                .addChildElement(device = new XmlAnalyzeStream("device",false));

        sepcVersion
                .addChildElement(new XmlAnalyzeStream("major",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.specMajorVersion = Integer.parseInt(value);
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("minor",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.specMajorVersion = Integer.parseInt(value);
                    }
                }));



        device
                .addChildElement(new XmlAnalyzeStream("friendlyName",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.friendlyName = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("modelNumber",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.modelNumber = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("modelName",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.modelName = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("modelDescription",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.modelDescription = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("manufacturer",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.manufacturer = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("manufacturerURL",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.manufacturerURL = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("modelURL",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.modelURL = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("serialNumber",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.serialNumber = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("UDN",false,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.UUID = value;
                    }
                }))
                .addChildElement(iconList = new XmlAnalyzeStream("iconList",false))
                .addChildElement(serviceList = new XmlAnalyzeStream("serviceList",false))
                .addChildElement(new XmlAnalyzeStream.DefaultElement(true,new XmlAnalyzeStream.OnElementCallback(true) {
                    @Override
                    public void onElementValue(String value) {
                        deviceInfo.otherDesc.put(getElementName(),value);
                    }
                }));


        iconList
                .addChildElement(iconStream = new XmlAnalyzeStream("icon",true,new XmlAnalyzeStream.OnElementCallback(){
                    @Override
                    public void onElementStart(Attributes attributes) {
                        icon = new Icon();
                    }

                    @Override
                    public void onElementEnd() {
                        deviceInfo.iconList.add(icon);
                    }
                }));

        iconStream
                .addChildElement(new XmlAnalyzeStream("mimetype",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        icon.mimetype = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("width",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        icon.width = Integer.parseInt(value);
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("height",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        icon.height = Integer.parseInt(value);
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("depth",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        icon.depth = Integer.parseInt(value);
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("url",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        icon.url = value;
                    }
                }));



        serviceList
                .addChildElement(serviceStream = new XmlAnalyzeStream("service",true,new XmlAnalyzeStream.OnElementCallback(){
                    @Override
                    public void onElementStart(Attributes attributes) {
                        service = new Service();
                        service.deviceInfo = deviceInfo;
                    }

                    @Override
                    public void onElementEnd() {
                        deviceInfo.serviceMap.put(service.serviceType,service);
                    }
                }));

        serviceStream
                .addChildElement(new XmlAnalyzeStream("serviceType",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        service.serviceType = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("serviceId",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        service.serviceId = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("controlURL",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        service.controlURL = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("eventSubURL",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        service.eventSubURL = value;
                    }
                }))
                .addChildElement(new XmlAnalyzeStream("SCPDURL",true,new XmlAnalyzeStream.OnElementCallback(true){
                    @Override
                    public void onElementValue(String value) {
                        service.SCPDURL = value;
                    }
                }));

        analyzerChain = new XmlAnalyzeStream.AnalyzerChain(root);
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        analyzerChain.startElement(qName,attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        analyzerChain.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        analyzerChain.endElement(qName);
    }
}
