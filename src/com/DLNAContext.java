package com;

import com.constant.ErrorCode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DLNAContext {
    private static final int COMMAND_SET_URI = 0;
    private static final int COMMAND_PLAY = 1;
    private static final int COMMAND_STOP = 2;
    private static final int COMMAND_PAUSE = 3;
    private static final int COMMAND_SEEK = 4;

    private ConcurrentHashMap<String,DeviceInfo> devicesPool;
    private Vector<DeviceInfo> subscribeList;
    private DLNAClientProxy client;

    private Timer timeOutTimer;
    private TimerTask timerTask;

    DLNAContext(DLNAClientProxy client) {
        devicesPool = new ConcurrentHashMap<>();
        subscribeList = new Vector<>();
        timeOutTimer = new Timer();
        this.client = client;

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Iterator<DeviceInfo> infoIterator = subscribeList.iterator();
                while (infoIterator.hasNext()) {
                    DeviceInfo deviceInfo = infoIterator.next();

                    if(deviceInfo.subscribeId == null) {
                        infoIterator.remove();
                        continue;
                    }

                    long timeDifference = System.currentTimeMillis() - deviceInfo.lastSubscribeTime;

                    if(timeDifference > deviceInfo.timeOut)
                        infoIterator.remove();
                    else if(timeDifference > deviceInfo.timeOut / 2)
                        client.subscribe(deviceInfo);
                }
            }
        };
        /*3秒执行一次*/
        timeOutTimer.schedule(timerTask,0,3000);
    }

    Collection<DeviceInfo> getExistDevices() {
        return devicesPool.values();
    }

    DeviceInfo findDeviceInfoBySID(String sid) {
        for(DeviceInfo info : subscribeList)
            if(info.subscribeId.equals(sid))
                return info;
        return null;
    }

    DeviceInfo findNewDeviceInfo(DeviceInfo deviceInfo) {
        DeviceInfo existInfo = devicesPool.get(deviceInfo.USN);
        //如果哈希表内存在此设备信息，则返回空
        if(existInfo != null && existInfo.location.equals(deviceInfo.location))
            return null;
        else devicesPool.put(deviceInfo.USN,deviceInfo);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(deviceInfo.location).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(false);
            connection.setDoInput(true);

            BoundedInputStream boundedInputStream = new BoundedInputStream(connection.getInputStream()
                    ,Long.parseLong(connection.getHeaderField("Content-Length")));

            if(!XMLAnalyzer.analyzeDDD(deviceInfo,boundedInputStream))
                throw new IOException();

            deviceInfo.isGetCompleted = true;
            return deviceInfo;
        } catch (IOException e) {
            //不做任何处理，视作未发现此设备
            devicesPool.remove(deviceInfo.USN);
            return null;
        } finally {
            if(connection != null)
                connection.disconnect();
        }
    }

    void deviceQuit(DeviceInfo deviceInfo) {
        devicesPool.remove(deviceInfo.USN);
    }

    void sendSetURICommand(DeviceInfo deviceInfo, String routePath) {
        if(!devicesPool.contains(deviceInfo))
            return;

        try {
            sendAction(deviceInfo,COMMAND_SET_URI,routePath);
        } catch (Exception e) {
            client.error(ErrorCode.CON_SET_URI_ERROR,e,deviceInfo);
        }
    }

    void sendPlayCommand(DeviceInfo deviceInfo) {
        if(!devicesPool.contains(deviceInfo))
            return;

        try {
            sendAction(deviceInfo,COMMAND_PLAY,null);
        } catch (Exception e) {
            client.error(ErrorCode.CON_PLAY_ERROR,e,deviceInfo);
        }
    }

    void sendPauseCommand(DeviceInfo deviceInfo) {
        if(!devicesPool.contains(deviceInfo))
            return;

        try {
            sendAction(deviceInfo,COMMAND_PAUSE,null);
        } catch (Exception e) {
            client.error(ErrorCode.CON_PAUSE_ERROR,e,deviceInfo);
        }
    }

    void sendStopCommand(DeviceInfo deviceInfo) {
        if(!devicesPool.contains(deviceInfo))
            return;

        try {
            sendAction(deviceInfo,COMMAND_STOP,null);
        } catch (Exception e) {
            client.error(ErrorCode.CON_STOP_ERROR,e,deviceInfo);
        }
    }

    void sendSeekCommand(DeviceInfo deviceInfo,long target) {
        if(!devicesPool.contains(deviceInfo))
            return;

        try {
            String timeStr = TimeUtils.millis2Str(target);
            sendAction(deviceInfo,COMMAND_SEEK,timeStr);
        } catch (Exception e) {
            client.error(ErrorCode.CON_SEEK_ERROR,e,deviceInfo);
        }
    }

    void sendSubscribeCommand(DeviceInfo deviceInfo) {
        if(!devicesPool.contains(deviceInfo))
            return;

        if(deviceInfo.isSubscibing)
            return;
        deviceInfo.isSubscibing = false;
        /*如果订阅id不为空并且上次订阅时间距今不大于1小时，则续订*/
        if(deviceInfo.subscribeId != null && System.currentTimeMillis() - deviceInfo.lastSubscribeTime < deviceInfo.timeOut) {
            sendRenewSubscribeCommand(deviceInfo);
            return;
        }
        SubscribeEvent subscribeEvent = new SubscribeEvent();
        SimpleHTTPConnection connection = null;
        try {
            connection = new SimpleHTTPConnection(false);
            connection.setMethod("SUBSCRIBE");
            connection.setSubUrl(deviceInfo.avtEventSubPath);
            connection.connect(deviceInfo.IP,deviceInfo.port);
            connection.addRequestProperty("CALLBACK", "<http://" + client.getHostAddr() + ":" + DLNAClient.HTTP_PORT + "/>");
            connection.addRequestProperty("NT", "upnp:event");
            connection.addRequestProperty("TIMEOUT","Second-3600");
            connection.outputCompleted();

            HTTPHeader httpHeader = HTTPHeader.analyzeParams(connection.getInputStream());
            if(httpHeader.methodPath.equals("200")) {
                String timeOutStr = httpHeader.otherHeaders.get("Timeout").trim();
                deviceInfo.subscribeId = httpHeader.otherHeaders.get("SID").trim();
                deviceInfo.lastSubscribeTime = System.currentTimeMillis();
                deviceInfo.timeOut = Long.parseLong(timeOutStr.substring(timeOutStr.indexOf('-') + 1 , timeOutStr.length())) * 1000L;

                subscribeList.add(deviceInfo);
                subscribeEvent.actionId = SubscribeEvent.ACTION_SUBSCRIBE_SUCCESS;
            } else
                subscribeEvent.actionId = SubscribeEvent.ACTION_SUBSCRIBE_FAILED;

        } catch (IOException e) {
            subscribeEvent.actionId = SubscribeEvent.ACTION_SUBSCRIBE_CON_FAILED;
        } finally {
            if(connection != null)
                connection.close();
            deviceInfo.isSubscibing = false;
            client.subscibeEvent(deviceInfo,subscribeEvent);
        }
    }

    private void sendRenewSubscribeCommand(DeviceInfo deviceInfo) {
        SubscribeEvent subscribeEvent = new SubscribeEvent();
        SimpleHTTPConnection connection = null;
        try {
            connection = new SimpleHTTPConnection(false);
            connection.setMethod("SUBSCRIBE");
            connection.setSubUrl(deviceInfo.avtEventSubPath);
            connection.connect(deviceInfo.IP,deviceInfo.port);
            connection.addRequestProperty("SID",deviceInfo.subscribeId);
            connection.addRequestProperty("TIMEOUT","Second-3600");
            connection.outputCompleted();

            HTTPHeader httpHeader = HTTPHeader.analyzeParams(connection.getInputStream());
            if(httpHeader.methodPath.equals("200")) {
                String timeOutStr = httpHeader.otherHeaders.get("Timeout").trim();
                deviceInfo.subscribeId = httpHeader.otherHeaders.get("SID").trim();
                deviceInfo.lastSubscribeTime = System.currentTimeMillis();
                deviceInfo.timeOut = Long.parseLong(timeOutStr.substring(timeOutStr.indexOf('-') + 1 , timeOutStr.length())) * 1000L;

                subscribeEvent.actionId = SubscribeEvent.ACTION_RENEW_SUBSCRIBE_SUCCESS;
            } else
                subscribeEvent.actionId = SubscribeEvent.ACTION_RENEW_SUBSCRIBE_FAILED;
        } catch (IOException e) {
            subscribeEvent.actionId = SubscribeEvent.ACTION_RENEW_SUBSCRIBE_CON_FAILED;
        } finally {
            if(connection != null)
                connection.close();
            deviceInfo.isSubscibing = false;
            client.subscibeEvent(deviceInfo,subscribeEvent);
        }
    }

    void sendCancelSubscribeCommand(DeviceInfo deviceInfo) {
        SubscribeEvent subscribeEvent = new SubscribeEvent();
        SimpleHTTPConnection connection = null;
        try {
            connection = new SimpleHTTPConnection(false);
            connection.setMethod("UNSUBSCRIBE");
            connection.setSubUrl(deviceInfo.avtEventSubPath);
            connection.connect(deviceInfo.IP,deviceInfo.port);
            connection.addRequestProperty("SID",deviceInfo.subscribeId);
            connection.outputCompleted();

            HTTPHeader httpHeader = HTTPHeader.analyzeParams(connection.getInputStream());
            if(httpHeader.methodPath.equals("200")) {
                subscribeList.remove(deviceInfo);
                deviceInfo.subscribeId = null;
                deviceInfo.lastSubscribeTime = null;
                deviceInfo.timeOut = null;

                subscribeEvent.actionId = SubscribeEvent.ACTION_CANCEL_SUBSCRIBE_SUCCESS;
            } else
                subscribeEvent.actionId = SubscribeEvent.ACTION_CANCEL_SUBSCRIBE_FAILED;
        } catch (IOException e) {
            subscribeEvent.actionId = SubscribeEvent.ACTION_CANCEL_SUBSCRIBE_CON_FAILED;
        } finally {
            if(connection != null)
                connection.close();
            client.subscibeEvent(deviceInfo,subscribeEvent);
        }
    }




    private void sendAction(DeviceInfo deviceInfo,int commandType,Object params) throws Exception {
        SimpleHTTPConnection connection = null;
        try {
            connection = new SimpleHTTPConnection(true);
            connection.setMethod("POST");
            connection.setSubUrl(deviceInfo.avtControlPath);
            connection.connect(deviceInfo.IP,deviceInfo.port);
            connection.addRequestProperty("Content-Type","text/xml; charset=\"utf-8\"");

            switch (commandType) {
                case COMMAND_SET_URI:
                    connection.addRequestProperty("SOAPACTION","\"urn:upnp-org:serviceId:AVTransport#SetAVTransportURI\"");
                    DLNAAnalyzer.writeSetURICommand(connection.getOutputStream(),"http://" + client.getHostAddr() + ":" + DLNAClient.HTTP_PORT + "/" + params);
                    break;
                case COMMAND_PLAY:
                    connection.addRequestProperty("SOAPACTION","\"urn:upnp-org:serviceId:AVTransport#Play\"");
                    DLNAAnalyzer.writePlayCommand(connection.getOutputStream());
                    break;
                case COMMAND_PAUSE:
                    connection.addRequestProperty("SOAPACTION","\"urn:upnp-org:serviceId:AVTransport#Pause\"");
                    DLNAAnalyzer.writePauseCommand(connection.getOutputStream());
                    break;
                case COMMAND_STOP:
                    connection.addRequestProperty("SOAPACTION","\"urn:upnp-org:serviceId:AVTransport#Stop\"");
                    DLNAAnalyzer.writeStopCommand(connection.getOutputStream());
                    break;
                case COMMAND_SEEK:
                    connection.addRequestProperty("SOAPACTION","\"urn:upnp-org:serviceId:AVTransport#Seek\"");
                    DLNAAnalyzer.writeSeekCommand(connection.getOutputStream(),(String)params);
                    break;
            }
            connection.outputCompleted();

            HTTPHeader httpHeader = HTTPHeader.analyzeParams(connection.getInputStream());


            if(httpHeader.methodPath.equals("200"))
                System.out.println(commandType + " Command Success");
            else System.out.println(DLNAAnalyzer.analyzeXMLFromStream(connection.getInputStream(),httpHeader.contentLength));
        } finally {
            if(connection != null)
                connection.close();
        }
    }

    void clear() {
        client.close();
        devicesPool.clear();
        if(timeOutTimer != null) {
            timeOutTimer.cancel();
            timeOutTimer = null;
        }
        if(timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        client = null;
    }
}
