package com;

import java.io.*;
import java.net.DatagramPacket;

public class DLNAAnalyzer {
    static String analyzeXMLFromStream(InputStream stream, long length) throws IOException {
        BoundedInputStream boundedInputStream = new BoundedInputStream(stream,length);
        StringBuilder stringBuilder = new StringBuilder();
        byte[] buffer = new byte[1024];
        int readLength;
        while ((readLength = boundedInputStream.read(buffer)) != -1)
            stringBuilder.append(new String(buffer, 0, readLength));

        return stringBuilder.toString().replace("&gt;",">").replace("&lt;","<");
    }


    static DeviceInfo analyzeDeviceInfoFromMulticast(DatagramPacket packet) throws IOException {
        BoundedInputStream inputStream = new BoundedInputStream(new ByteArrayInputStream(packet.getData()),packet.getLength());
        HTTPHeader httpHeader = HTTPHeader.analyzeParams(inputStream,"NOTIFY");
        DeviceInfo deviceInfo = null;
        if(httpHeader != null) {
            deviceInfo = new DeviceInfo();
            deviceInfo.deviceType = httpHeader.otherHeaders.get("NT");
            deviceInfo.USN = httpHeader.otherHeaders.get("USN");
            deviceInfo.location = httpHeader.otherHeaders.get("Location");
            deviceInfo.server = httpHeader.otherHeaders.get("Server");
            deviceInfo.NTS = httpHeader.otherHeaders.get("NTS").trim();
            deviceInfo.lastActiveTime = System.currentTimeMillis();
            String cacheControl = httpHeader.otherHeaders.get("Cache-Control");
            if(cacheControl != null)
                deviceInfo.cacheLong = Long.parseLong(cacheControl.substring(cacheControl.indexOf("=") + 1,cacheControl.length()));
            int i = 3;
            int index = 0;
            try {
                while (i -- > 0)
                    index = deviceInfo.location.indexOf('/',index == 0 ? index : index + 1);
            } catch (NullPointerException e) {
                System.out.println(new String(packet.getData(),0,packet.getLength()));
                throw e;
            }

            deviceInfo.host = deviceInfo.location.substring(0,index);
            String hostIP = deviceInfo.host.substring(deviceInfo.host.lastIndexOf('/') + 1,deviceInfo.host.length());
            String hostArr[] = hostIP.split(":");
            deviceInfo.IP = hostArr[0];
            deviceInfo.port = hostArr.length == 1 ? 80 : Integer.parseInt(hostArr[1]);
        }
        inputStream.close();
        return deviceInfo;
    }

    static DeviceInfo analyzeDeviceInfoFromSearch(DatagramPacket packet) throws IOException {
        BoundedInputStream inputStream = new BoundedInputStream(new ByteArrayInputStream(packet.getData()),packet.getLength());
        HTTPHeader httpHeader = HTTPHeader.analyzeParams(inputStream);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.deviceType = httpHeader.otherHeaders.get("ST");
        deviceInfo.USN = httpHeader.otherHeaders.get("USN");
        deviceInfo.location = httpHeader.otherHeaders.get("Location");
        deviceInfo.server = httpHeader.otherHeaders.get("Server");
        deviceInfo.lastActiveTime = System.currentTimeMillis();
        String cacheControl = httpHeader.otherHeaders.get("Cache-Control");
        if(cacheControl != null)
            deviceInfo.cacheLong = Long.parseLong(cacheControl.substring(cacheControl.indexOf("=") + 1,cacheControl.length()));
        int i = 3;
        int index = 0;
        while (i -- > 0)
            index = deviceInfo.location.indexOf('/',index == 0 ? index : index + 1);

        deviceInfo.host = deviceInfo.location.substring(0,index);
        String hostIP = deviceInfo.host.substring(deviceInfo.host.lastIndexOf('/') + 1,deviceInfo.host.length());
        String hostArr[] = hostIP.split(":");
        deviceInfo.IP = hostArr[0];
        deviceInfo.port = hostArr.length == 1 ? 80 : Integer.parseInt(hostArr[1]);
        inputStream.close();
        return deviceInfo;
    }

    static void writeSetURICommand(OutputStream output, String path) throws IOException {
        DataOutputStream outputStream = output instanceof DataOutputStream ? (DataOutputStream) output : new DataOutputStream(output);
        outputStream.writeBytes("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
        outputStream.writeBytes("<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        outputStream.writeBytes("<s:Body>");
        outputStream.writeBytes("<u:SetAVTransportURI xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">");
        outputStream.writeBytes("<InstanceID>0</InstanceID>");
        outputStream.writeBytes("<CurrentURI>" + path + "</CurrentURI>");
        outputStream.writeBytes("<CurrentURIMetaData />");
        outputStream.writeBytes("</u:SetAVTransportURI>");
        outputStream.writeBytes("</s:Body>");
        outputStream.writeBytes("</s:Envelope>");
        outputStream.flush();
    }


    static void writePlayCommand(OutputStream output) throws IOException {
        DataOutputStream outputStream = output instanceof DataOutputStream ? (DataOutputStream) output : new DataOutputStream(output);
        outputStream.writeBytes("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
        outputStream.writeBytes("<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        outputStream.writeBytes("<s:Body>");
        outputStream.writeBytes("<u:Play xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">");
        outputStream.writeBytes("<InstanceID>0</InstanceID>");
        outputStream.writeBytes("<Speed>1</Speed>");
        outputStream.writeBytes("</u:Play>");
        outputStream.writeBytes("</s:Body>");
        outputStream.writeBytes("</s:Envelope>");

    }


    static void writePauseCommand(OutputStream output) throws IOException {
        DataOutputStream outputStream = output instanceof DataOutputStream ? (DataOutputStream) output : new DataOutputStream(output);
        outputStream.writeBytes("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
        outputStream.writeBytes("<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        outputStream.writeBytes("<s:Body>");
        outputStream.writeBytes("<u:Pause xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">");
        outputStream.writeBytes("<InstanceID>0</InstanceID>");
        outputStream.writeBytes("</u:Pause>");
        outputStream.writeBytes("</s:Body>");
        outputStream.writeBytes("</s:Envelope>");
    }

    static void writeStopCommand(OutputStream output) throws IOException {
        DataOutputStream outputStream = output instanceof DataOutputStream ? (DataOutputStream) output : new DataOutputStream(output);
        outputStream.writeBytes("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
        outputStream.writeBytes("<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        outputStream.writeBytes("<s:Body>");
        outputStream.writeBytes("<u:Stop xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">");
        outputStream.writeBytes("<InstanceID>0</InstanceID>");
        outputStream.writeBytes("</u:Stop>");
        outputStream.writeBytes("</s:Body>");
        outputStream.writeBytes("</s:Envelope>");
    }

    static void writeSeekCommand(OutputStream output,String timeStr) throws IOException {
        DataOutputStream outputStream = output instanceof DataOutputStream ? (DataOutputStream) output : new DataOutputStream(output);
        outputStream.writeBytes("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
        outputStream.writeBytes("<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        outputStream.writeBytes("<s:Body>");
        outputStream.writeBytes("<u:Seek xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">");
        outputStream.writeBytes("<InstanceID>0</InstanceID>");
        outputStream.writeBytes("<Unit>REL_TIME</Unit>");
        outputStream.writeBytes("<Target>"+timeStr+"</Target>");
        outputStream.writeBytes("</u:Seek>");
        outputStream.writeBytes("</s:Body>");
        outputStream.writeBytes("</s:Envelope>");
    }
}
