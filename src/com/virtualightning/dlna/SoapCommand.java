package com.virtualightning.dlna;

import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.DataOutputStream;

public abstract class SoapCommand {
    protected final String serviceType;
    protected final String actionName;
    private TransformerHandler handler;
    private AttributesImpl atts;
    protected SoapCommand(String serviceType, String actionName) {
        this.serviceType = serviceType;
        this.actionName = actionName;
    }

    public boolean writeCommand(DataOutputStream outputStream) {
        try {
            SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            handler = factory.newTransformerHandler();
            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");// 设置输出采用的编码方式
            transformer.setOutputProperty(OutputKeys.INDENT, "false");// 是否自动添加额外的空白
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"no");// 是否忽略xml声明
            handler.setResult(new StreamResult(outputStream));
            handler.startDocument();

            newAttributes();
            newAttributeValue("s:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/");
            newAttributeValue("xmlns:s","http://schemas.xmlsoap.org/soap/envelope/");
            startElement("s:Envelope", generateAttrs());
            startElement("s:Body");
            newAttributes();
            newAttributeValue("xmlns:u",serviceType);
            newAttributeValue("xmlns:u",serviceType);
            startElement("u:"+actionName,generateAttrs());

            writeCommand(handler);

            endElement("u:"+actionName);
            endElement("s:Body");
            endElement("s:Envelope");
            handler.endDocument();
        } catch (TransformerConfigurationException e) {
            return false;
        } catch (SAXException e) {
            return false;
        }

        return true;
    }

    protected final void startElement(String elementName) throws SAXException {
        startElement(elementName,null);
    }

    protected final void startElement(String elementName,AttributesImpl attrs) throws SAXException {
        handler.startElement("","",elementName,attrs);
    }

    protected final void endElement(String elementName) throws SAXException {
        handler.endElement("","",elementName);
    }

    protected final void newAttributes() {
        atts = new AttributesImpl();
    }

    protected final void newAttributeValue(String key,String value) {
        atts.addAttribute("","",key,"",value);
    }

    protected final AttributesImpl generateAttrs() {
        return atts;
    }

    protected abstract void writeCommand(TransformerHandler handler) throws SAXException;
}
