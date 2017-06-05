package com;

import com.interfaces.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by CimZzz on 17/5/31.<br>
 * Project Name : Hunban.com Education<br>
 * Since : Education_0.0.1<br>
 * Description:<br>
 * Description
 */
public class DLNAClient implements TaskStatistic.ICompletedListener{
    static final int HTTP_PORT = 9090;//DLNA HTTP Server Port
    static final int UNICAST_PORT = 1234;//DLNA Unicast Port;

    private static final int FLAG_START_COMPLETED = 0;
    private static final int FLAG_START_FAILED = 1;

    private static final int STATE_CLOSE = 0;//关闭状态
    private static final int STATE_START = 1;//开启状态，尚未启动成功
    private static final int STATE_START_COMPLETED = 2;//开启状态，启动成功

    private HTTPServer httpServer;
    private UnicastServer unicastServer;
    private MulticastServer multicastServer;
    private DLNAContext context;


    private ExecutorService threadPool;
    private int state;

    String hostAddr;

    /*接口*/

    private OnStartCompletedListener onStartCompletedListener;
    private OnDeviceQuitListener onDeviceQuitListener;
    private OnFindNewDeviceListener onFindNewDeviceListener;
    private OnInfoListener onInfoListener;
    private OnErrorListener onErrorListener;
    private OnResourceRouteListener onResourceRouteListener;
    private OnSubscribeEventListener onSubscribeEventListener;

    public DLNAClient() {
        this.state = STATE_CLOSE;
    }

    public void startDLNA() {
        synchronized (this) {
            if(state != STATE_CLOSE)
                return;
            state = STATE_START;
        }
        threadPool = Executors.newCachedThreadPool();
        final TaskStatistic statist = new TaskStatistic(this,4,FLAG_START_COMPLETED,FLAG_START_FAILED);
        httpServer = new HTTPServer(new DLNAClientProxy(this),threadPool,HTTP_PORT);
        unicastServer = new UnicastServer(new DLNAClientProxy(this),UNICAST_PORT);
        multicastServer = new MulticastServer(new DLNAClientProxy(this));
        context = new DLNAContext(new DLNAClientProxy(this));
        httpServer.startServer(statist);
        unicastServer.startServer(statist);
        multicastServer.startServer(statist);
        new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
//                    hostAddr = InetAddress.getLocalHost().getHostAddress();
//                    statist.complete();
//                } catch (UnknownHostException e) {
//                    callback.onError(e);
//                    statist.failed();
//                }

                hostAddr = "192.168.18.10";
                statist.complete();
            }
        }).start();
    }

    public void search() {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }
        unicastServer.search();
    }

    public void setURI(DeviceInfo info, String routePath) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                context.sendSetURICommand(info,routePath);
            }
        });
    }

    public void play(DeviceInfo info) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                context.sendPlayCommand(info);
            }
        });
    }

    public void pause(DeviceInfo info) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                context.sendPauseCommand(info);
            }
        });
    }

    public void stop(DeviceInfo info) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                context.sendStopCommand(info);
            }
        });
    }

    public void seek(DeviceInfo info,long target) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                context.sendSeekCommand(info,target);
            }
        });
    }

    public void subscribe(DeviceInfo info) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                context.sendSubscribeCommand(info);
            }
        });
    }

    public void unsubscribe(DeviceInfo info) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                context.sendCancelSubscribeCommand(info);
            }
        });
    }


    public List<DeviceInfo> getCurrentInfo() {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return null;
        }
        return new ArrayList<>(context.getExistDevices());
    }

    public void closeDLNA() {
        synchronized (this) {
            state = STATE_CLOSE;
            if(unicastServer != null) {
                unicastServer.closeServer();
                unicastServer = null;
            }
            if(httpServer != null) {
                httpServer.closeServer();
                httpServer = null;
            }
            if(multicastServer != null) {
                multicastServer.closeServer();
                multicastServer = null;
            }
            if(threadPool != null) {
                threadPool.shutdownNow();
                threadPool = null;
            }
            if(context != null) {
                context.clear();
                context = null;
            }
        }
    }

    /*任务过程回调*/
    @Override
    public void onTaskCompleted(int flag) {
        switch (flag) {
            case FLAG_START_COMPLETED:
                synchronized (this) {
                    state = STATE_START_COMPLETED;
                }
                if(onStartCompletedListener != null)
                    onStartCompletedListener.onStartCompleted(this);
                System.out.println("初始化完成");
                break;
            case FLAG_START_FAILED:
                closeDLNA();
                System.out.println("初始化失败");
                break;
        }
    }


    /*设置接口方法*/

    public void setOnDeviceQuitListener(OnDeviceQuitListener onDeviceQuitListener) {
        this.onDeviceQuitListener = onDeviceQuitListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setOnFindNewDeviceListener(OnFindNewDeviceListener onFindNewDeviceListener) {
        this.onFindNewDeviceListener = onFindNewDeviceListener;
    }

    public void setOnInfoListener(OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
    }

    public void setOnStartCompletedListener(OnStartCompletedListener onStartCompletedListener) {
        this.onStartCompletedListener = onStartCompletedListener;
    }

    public void setOnResourceRouteListener(OnResourceRouteListener onResourceRouteListener) {
        this.onResourceRouteListener = onResourceRouteListener;
    }

    public void setOnSubscribeEventListener(OnSubscribeEventListener onSubscribeEventListener) {
        this.onSubscribeEventListener = onSubscribeEventListener;
    }
    /*内部回调方法*/

    void findNewDevice(DeviceInfo deviceInfo) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }
        DeviceInfo info = context.findNewDeviceInfo(deviceInfo);
        if(info != null && onFindNewDeviceListener != null)
            onFindNewDeviceListener.onFindNewDevice(info);
        //onDeviceFind
    }

    void deviceQuit(DeviceInfo deviceInfo) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }

        context.deviceQuit(deviceInfo);
        if(onDeviceQuitListener != null)
            onDeviceQuitListener.onDeviceQuit(deviceInfo);
        //onDeviceQuit
    }

    DeviceInfo findDeviceInfoBySID(String sid) {
        return context.findDeviceInfoBySID(sid);
    }

    void onSubscribeEvent(DeviceInfo info,SubscribeEvent event) {
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return;
        }
        if(onSubscribeEventListener != null)
            onSubscribeEventListener.onSubscribeEvent(info,event);
    }

    void onError(int errorCode,Exception e,Object... args) {
        synchronized (this) {
            if(state == STATE_CLOSE)
                return;
        }

        if(onErrorListener != null)
            onErrorListener.onError(this,errorCode,args);
    }

    void onInfo(int infoCode,int other) {
        synchronized (this) {
            if(state == STATE_CLOSE)
                return;
        }

        if(onInfoListener != null)
            onInfoListener.onInfo(infoCode,other);
    }

    File getResourcesFile(String path) throws Exception{
        synchronized (this) {
            if(state < STATE_START_COMPLETED)
                return null;
        }

        if(onResourceRouteListener != null)
            return onResourceRouteListener.onResourceRoute(path);
        return null;
    }
}
