package com.virtualightning.dlna;

import com.virtualightning.dlna.tools.BoundedInputStream;

import java.io.*;

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

    static DeviceInfo analyzeDeviceInfo(HTTPHeader httpHeader) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.deviceType = httpHeader.otherHeaders.get("NT");
        if(deviceInfo.deviceType == null)
            deviceInfo.deviceType = httpHeader.otherHeaders.get("ST");
        deviceInfo.USN = httpHeader.otherHeaders.get("USN");
        deviceInfo.location = httpHeader.otherHeaders.get("LOCATION");
        deviceInfo.server = httpHeader.otherHeaders.get("SERVER");
        deviceInfo.lastActiveTime = System.currentTimeMillis();

        String cacheControl = httpHeader.otherHeaders.get("CACHE-CONTROL");
        if(cacheControl != null)
            deviceInfo.cacheTime = Long.parseLong(cacheControl.substring(cacheControl.indexOf("=") + 1,cacheControl.length())) * 1000;

        int i = 3;
        int index = 0;
        while (i -- > 0)
            index = deviceInfo.location.indexOf('/',index == 0 ? index : index + 1);

        deviceInfo.host = deviceInfo.location.substring(0,index);

        String hostIP = deviceInfo.host.substring(deviceInfo.host.lastIndexOf('/') + 1,deviceInfo.host.length());
        String hostArr[] = hostIP.split(":");
        deviceInfo.IP = hostArr[0];
        deviceInfo.port = hostArr.length == 1 ? 80 : Integer.parseInt(hostArr[1]);
        return deviceInfo;
    }
}
