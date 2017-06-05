package com.new_version;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class XMLAnalyzer {

    static boolean analyzeDDD(DeviceInfo info, InputStream inputStream) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(inputStream,new XmlDDDHandler(info));
        } catch (SAXException e) {
            return false;
        } catch (IOException e) {
            return false;
        } catch (ParserConfigurationException e) {
            return false;
        } finally {
            if(inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {}
        }
        return true;
    }

    static boolean analyzeSDD(Service service, InputStream inputStream) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(inputStream,new XmlSDDHandler(service));
        } catch (SAXException e) {
            return false;
        } catch (IOException e) {
            return false;
        } catch (ParserConfigurationException e) {
            return false;
        } finally {
            if(inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {}
        }
        return true;
    }


    static boolean analyzeSubscibeEvent(Map<Integer,Instance> instanceHashMap, InputStream inputStream) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(inputStream,new XmlEventHandler(instanceHashMap));
        } catch (SAXException e) {
            return false;
        } catch (IOException e) {
            return false;
        } catch (ParserConfigurationException e) {
            return false;
        } finally {
            if(inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {}
        }
        return true;
    }
}
