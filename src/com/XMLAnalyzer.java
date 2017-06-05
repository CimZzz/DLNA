package com;

import com.constant.Event;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.sql.Time;

public class XMLAnalyzer {

    static boolean analyzeDDD(DeviceInfo info, InputStream inputStream) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(inputStream,new DefaultHandler(){
                boolean isFindFriendlyName;
                boolean isFindUUID;
                boolean isFindServiceType;
                boolean isFindServiceTypeSuccess;
                boolean isFindControlUrl;
                boolean isFindEventUrl;
                boolean isFindOver;

                @Override
                public void startDocument() throws SAXException {
                    super.startDocument();
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if(isFindOver)
                        return;

                    if(qName.equals("friendlyName"))
                        isFindFriendlyName = true;
                    else if(qName.equals("UDN"))
                        isFindUUID = true;
                    else if(qName.equals("serviceType"))
                        isFindServiceType = true;
                    else if(isFindServiceTypeSuccess && qName.equals("controlURL"))
                        isFindControlUrl = true;
                    else if(isFindServiceTypeSuccess && qName.equals("eventSubURL"))
                        isFindEventUrl = true;
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    super.characters(ch, start, length);
                    if(isFindOver)
                        return;

                    if(isFindFriendlyName) {
                        info.friendlyName = new String(ch, start, length);
                        isFindFriendlyName = false;
                    }
                    else if(isFindUUID) {
                        info.UUID = new String(ch, start, length);
                        isFindUUID = false;
                    } else if(isFindServiceType) {
                        String type = new String(ch, start, length);
                        if(type.equals("urn:schemas-upnp-org:service:AVTransport:1"))
                            isFindServiceTypeSuccess = true;
                        isFindServiceType = false;
                    } else if (isFindControlUrl) {
                        info.avtControlPath = new String(ch, start, length);
                        isFindControlUrl = false;
                    } else if (isFindEventUrl) {
                        info.avtEventSubPath = new String(ch, start, length);
                        isFindControlUrl = false;
                        isFindOver = true;
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    super.endElement(uri, localName, qName);
                }

                @Override
                public void endDocument() throws SAXException {
                    super.endDocument();
                }
            });
            inputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    static boolean analyzeSubscibeEvent(SubscribeEvent event, InputStream inputStream) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(inputStream,new DefaultHandler(){

                @Override
                public void startDocument() throws SAXException {
                    super.startDocument();
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    super.startElement(uri, localName, qName, attributes);
                    if(qName.equals("Event"))
                    if(qName.equals("Event")) {
                        switch (attributes.getValue("val")) {
                            case "STATUS_OK":
                                event.transportStatus = Event.STATUS_OK;
                                break;
                            case "ERROR_OCCURRED":
                                event.transportStatus = Event.STATUS_OCCUR_ERROR;
                                break;
                            default:
                                event.transportStatus = Event.DEFAULT;
                                break;
                        }
                    } else if (qName.equals("TransportState")) {
                        switch (attributes.getValue("val")) {
                            case "PLAYING":
                                event.transportState = Event.STATE_PLAYING;
                                break;
                            case "PAUSED_PLAYBACK":
                                event.transportState = Event.STATE_PAUSED_PLAYBACK;
                                break;
                            case "STOPPED":
                                event.transportState = Event.STATE_STOPPED;
                                break;
                            case "TRANSITIONING":
                                event.transportState = Event.STATE_TRANSPORTING;
                                break;
                            case "NO_MEDIA_PRESENT":
                                event.transportState = Event.STATE_NO_MEDIA_PRESENT;
                                break;
                            default:
                                event.transportState = Event.DEFAULT;
                                break;
                        }
                    } else if (qName.equals("RelativeTimePosition"))
                        event.realTimePosition = TimeUtils.str2Millis(attributes.getValue("val"));
                    else if(qName.equals("CurrentMediaDuration"))
                        event.currentMediaDuration = TimeUtils.str2Millis(attributes.getValue("val"));
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    super.characters(ch, start, length);
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    super.endElement(uri, localName, qName);
                }

                @Override
                public void endDocument() throws SAXException {
                    super.endDocument();
                }
            });
            inputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
