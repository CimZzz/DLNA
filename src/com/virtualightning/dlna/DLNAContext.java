package com.virtualightning.dlna;


import com.virtualightning.dlna.factory.ThreadPoolFactory;
import com.virtualightning.dlna.interfaces.InetAddressGetter;
import com.virtualightning.dlna.interfaces.XmlDecoder;
import com.virtualightning.dlna.tools.BoundedInputStream;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.virtualightning.dlna.constant.ErrorCode.SERVER_INFO_FAILED;

public class DLNAContext {
    private DLNAClient dlnaClient;//DLNA客户端
    private ExecutorService threadPool;//线程池
    private XmlDecoder<SubscribeEvent> subscribeEventXmlDecoder;//订阅事件XML Decoder

    boolean useDebugMode;//是否使用Debug模式
    String hostAddr;//主机地址
    int httpPort;//HTTP服务器监听端口



    private HashMap<String,DeviceInfo> devicesPool;//设备池
    private final Object deviceLocker = new Object();//设备池线程同步锁

    private ConcurrentHashMap<String,Service> subscribePool;//订阅池
    private Timer timeOutTimer;//订阅计时器
    private TimerTask timerTask;//订阅计时器任务

    DLNAContext(DLNAClient dlnaClient,boolean useDebugMode) {
        this.dlnaClient = dlnaClient;
        this.httpPort = dlnaClient.getHTTPPort();
        this.useDebugMode = useDebugMode;

        this.devicesPool = new HashMap<>();
        this.subscribePool = new ConcurrentHashMap<>();

        timeOutTimer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                for(String key : subscribePool.keySet()) {
                    Service service = subscribePool.get(key);
                    long timeDifference = System.currentTimeMillis() - service.subscribeItem.lastSubscribeTime;

                    if(service.isNeedSubscribe())
                        subscribePool.remove(key);
                    else if(timeDifference > service.subscribeItem.timeOut / 2)
                        subscribe(service);
                }
            }
        };
        /*3秒执行一次*/
        timeOutTimer.schedule(timerTask,0,3000);
    }

    /* 生命周期方法 */

    void init(final TaskStatistic taskStatistic, ThreadPoolFactory factory) {
        if(factory == null)
            threadPool = Executors.newCachedThreadPool();
        else threadPool = factory.createThreadPool();
        subscribeEventXmlDecoder = dlnaClient.subscribeEventXmlDecoder;
        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddressGetter getter = dlnaClient.inetAddressGetter;

                    if(getter != null)
                        hostAddr = getter.getInetAddress();
                    else hostAddr = InetAddress.getLocalHost().getHostAddress();

                    if(hostAddr != null)
                        taskStatistic.complete();
                    else taskStatistic.failed();
                } catch (UnknownHostException e) {
                    taskStatistic.failed();
                }
            }
        });
    }

    void clear() {
        if(threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }
        if(timeOutTimer != null) {
            timeOutTimer.cancel();
            timeOutTimer = null;
        }
        if(timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        dlnaClient = null;
        subscribeEventXmlDecoder = null;
        devicesPool.clear();
        subscribePool.clear();
    }

    /* 自身方法 */
    void execute(Runnable runnable) {
        if(threadPool != null)
            threadPool.execute(runnable);
    }

    void executeCommand(final Service service,final SoapCommand soapCommand) {
        execute(new Runnable() {
            @Override
            public void run() {
                SimpleHTTPConnection connection = null;
                try {
                    connection = new SimpleHTTPConnection(true);
                    connection.setMethod("POST");
                    connection.setSubUrl(service.controlURL);
                    connection.connect(service.deviceInfo.IP, service.deviceInfo.port);
                    connection.addRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
                    connection.addRequestProperty("SOAPACTION","\""+soapCommand.serviceType + "#" + soapCommand.actionName +"\"");
                    soapCommand.writeCommand(connection.getOutputStream());
                    connection.outputCompleted();

                    HTTPHeader httpHeader = HTTPHeader.analyzeParams(connection.getInputStream());

                    String msg = DLNAAnalyzer.analyzeXMLFromStream(connection.getInputStream(),httpHeader.contentLength);

                    if(httpHeader.methodPath.equals("200")) {
                        if (dlnaClient != null)
                            dlnaClient.commandExecResult(service,soapCommand,true, msg);
                    }
                    else if(dlnaClient != null)
                            dlnaClient.commandExecResult(service,soapCommand,false,msg);
                } catch (IOException e) {
                    if(dlnaClient != null)
                        dlnaClient.commandExecResult(service,soapCommand,false,null);
                }
            }
        });
    }

    void findServiceInfo(final Service service) {
        execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    String path = service.SCPDURL;
                    if(path.charAt(0) != '/')
                        path = '/' + path;

                    path = service.deviceInfo.host + path;
                    connection = (HttpURLConnection) new URL(path).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setDoOutput(false);
                    connection.setDoInput(true);

                    BoundedInputStream boundedInputStream = new BoundedInputStream(connection.getInputStream()
                            ,Long.parseLong(connection.getHeaderField("Content-Length")));

                    XmlDecoder<Service> decoder = new XmlSDDHandler(service);
                    if(!decoder.decoderXMLStream(null,boundedInputStream)) {
                        error(SERVER_INFO_FAILED,null,service);
                        return;
                    }

                    if(dlnaClient != null)
                        dlnaClient.findServiceInfoComplete(service);
                } catch (IOException e) {
                    error(SERVER_INFO_FAILED,e,service);
                } finally {
                    if(connection != null)
                        connection.disconnect();
                }
            }
        });
    }

    //发送订阅事件
    void subscribe(Service service) {
        boolean isNeedSubscribe = service.isNeedSubscribe();
        boolean checkState = service.checkSubscribingState(true);

        if(checkState)
            return;

        if(!isNeedSubscribe) {
            renewSubscribe(service);
            return;
        }

        SubscribeEvent subscribeEvent = new SubscribeEvent();

        SimpleHTTPConnection connection = null;
        try {
            connection = new SimpleHTTPConnection(false);
            connection.setMethod("SUBSCRIBE");
            connection.setSubUrl(service.eventSubURL);
            connection.connect(service.deviceInfo.IP,service.deviceInfo.port);
            if(dlnaClient == null)
                return;
            connection.addRequestProperty("CALLBACK", "<http://" + hostAddr + ":" + dlnaClient.getHTTPPort() + "/SUBSCRIBE/CallBack />");
            connection.addRequestProperty("NT", "upnp:event");
            connection.addRequestProperty("TIMEOUT","Second-3600");
            connection.outputCompleted();

            HTTPHeader httpHeader = HTTPHeader.analyzeParams(connection.getInputStream());
            if(httpHeader.methodPath.equals("200")) {
                String timeOutStr = httpHeader.otherHeaders.get("Timeout").trim();
                service.subscribeItem.subscribeId = httpHeader.otherHeaders.get("SID").trim();
                service.subscribeItem.lastSubscribeTime = System.currentTimeMillis();
                service.subscribeItem.timeOut = Long.parseLong(timeOutStr.substring(timeOutStr.indexOf('-') + 1 , timeOutStr.length())) * 1000L;

                if(!service.checkCancelSubscribeState()) {
                    subscribePool.put(service.subscribeItem.subscribeId, service);
                    subscribeEvent.actionId = SubscribeEvent.ACTION_SUBSCRIBE_SUCCESS;
                } else subscribeEvent.actionId = SubscribeEvent.ACTION_SUBSCRIBE_FAILED;
            } else
                subscribeEvent.actionId = SubscribeEvent.ACTION_SUBSCRIBE_FAILED;

        } catch (IOException e) {
            subscribeEvent.actionId = SubscribeEvent.ACTION_SUBSCRIBE_CON_FAILED;
        } finally {
            if(connection != null)
                connection.close();
            service.checkSubscribingState(false);
            subscribeEvent(service,subscribeEvent);
        }
    }

    //发送更新订阅事件
    private void renewSubscribe(Service service) {
        SubscribeEvent subscribeEvent = new SubscribeEvent();

        SimpleHTTPConnection connection = null;
        try {
            connection = new SimpleHTTPConnection(false);
            connection.setMethod("SUBSCRIBE");
            connection.setSubUrl(service.eventSubURL);
            connection.connect(service.deviceInfo.IP,service.deviceInfo.port);
            connection.addRequestProperty("TIMEOUT","Second-3600");
            connection.addRequestProperty("SID",service.subscribeItem.subscribeId);
            connection.outputCompleted();

            HTTPHeader httpHeader = HTTPHeader.analyzeParams(connection.getInputStream());
            if(httpHeader.methodPath.equals("200")) {
                String timeOutStr = httpHeader.otherHeaders.get("Timeout").trim();
                service.subscribeItem.subscribeId = httpHeader.otherHeaders.get("SID").trim();
                service.subscribeItem.lastSubscribeTime = System.currentTimeMillis();
                service.subscribeItem.timeOut = Long.parseLong(timeOutStr.substring(timeOutStr.indexOf('-') + 1 , timeOutStr.length())) * 1000L;

                if(!service.checkCancelSubscribeState()) {
                    subscribePool.put(service.subscribeItem.subscribeId, service);
                    subscribeEvent.actionId = SubscribeEvent.ACTION_RENEW_SUBSCRIBE_SUCCESS;
                } else subscribeEvent.actionId = SubscribeEvent.ACTION_RENEW_SUBSCRIBE_FAILED;
            } else
                subscribeEvent.actionId = SubscribeEvent.ACTION_RENEW_SUBSCRIBE_FAILED;

        } catch (IOException e) {
            subscribeEvent.actionId = SubscribeEvent.ACTION_RENEW_SUBSCRIBE_CON_FAILED;
        } finally {
            if(connection != null)
                connection.close();
            service.checkSubscribingState(false);
            subscribeEvent(service,subscribeEvent);
        }
    }

    //发送取消订阅事件
    void cancelSubscribe(Service service) {
        boolean checkState = service.checkCancelSubscribeState(true);

        if(checkState)
            return;

        SubscribeEvent subscribeEvent = new SubscribeEvent();
        SimpleHTTPConnection connection = null;
        try {
            connection = new SimpleHTTPConnection(false);
            connection.setMethod("UNSUBSCRIBE");
            connection.setSubUrl(service.eventSubURL);
            connection.connect(service.deviceInfo.IP,service.deviceInfo.port);
            connection.addRequestProperty("SID",service.subscribeItem.subscribeId);
            connection.outputCompleted();

            HTTPHeader httpHeader = HTTPHeader.analyzeParams(connection.getInputStream());
            if(httpHeader.methodPath.equals("200")) {
                subscribePool.remove(service.subscribeItem.subscribeId);
                service.subscribeItem = null;
                subscribeEvent.actionId = SubscribeEvent.ACTION_CANCEL_SUBSCRIBE_SUCCESS;
            } else
                subscribeEvent.actionId = SubscribeEvent.ACTION_CANCEL_SUBSCRIBE_FAILED;
        } catch (IOException e) {
            subscribeEvent.actionId = SubscribeEvent.ACTION_CANCEL_SUBSCRIBE_CON_FAILED;
        } finally {
            if(connection != null)
                connection.close();
            service.checkCancelSubscribeState(false);
            subscribeEvent(service,subscribeEvent);
        }
    }

    /* 代理方法 */

    //发送错误信息至回调
    void error(int errorCode, Exception e,Object... args) {
        if(dlnaClient != null)
            dlnaClient.error(errorCode,args);
    }

    //根据订阅ID找到服务
    Service findServiceInfoBySID(String sid) {
        return subscribePool.get(sid);
    }

    //发现新设备
    //比对设备池中是否存在相同USN并且尚未过期的设备，不存在则去获取DDD文档，视为新设备
    //如果消息来源是多播，则会刷新激活时间
    void findNewDevice(DeviceInfo deviceInfo,boolean fromMulticast) {
        synchronized (deviceLocker) {
            DeviceInfo existDevice = devicesPool.get(deviceInfo.USN);
            if(existDevice != null && System.currentTimeMillis() - existDevice.lastActiveTime < existDevice.cacheTime) {
                if(fromMulticast)
                    existDevice.lastActiveTime = System.currentTimeMillis();
                return;
            }

            devicesPool.put(deviceInfo.USN,deviceInfo);
        }

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

            XmlDecoder<DeviceInfo> decoder = new XmlDDDHandler(deviceInfo);
            if(!decoder.decoderXMLStream(null,boundedInputStream))
                return;


            if(dlnaClient != null)
                dlnaClient.findNewDevice(deviceInfo);
        } catch (IOException e) {
            //不做任何处理，视作未发现此设备
            synchronized (deviceLocker) {
                devicesPool.remove(deviceInfo.USN);
            }
        } finally {
            if(connection != null)
                connection.disconnect();
        }

    }

    //设备注销
    void deviceQuit(DeviceInfo deviceInfo) {
        synchronized (deviceLocker) {
            if(devicesPool.containsKey(deviceInfo.USN))
                deviceInfo = devicesPool.remove(deviceInfo.USN);
            else return;
        }

        if(dlnaClient != null)
            dlnaClient.deviceQuit(deviceInfo);
    }

    //发送订阅事件
    void subscribeEvent(Service service, SubscribeEvent event) {
        if(dlnaClient != null)
            dlnaClient.subscribeEvent(service,event);
    }

    //获取订阅事件XML Decoder
    XmlDecoder<SubscribeEvent> getSubscribeEventXmlDecoder() {
        return null;
    }

    //根据路径获取实际的资源文件
    //路由映射
    File getResourcesFile(String substring) {
        if(dlnaClient != null)
            return dlnaClient.getResourcesFile(substring);

        return null;
    }
}