import com.virtualightning.dlna.DLNAClient;
import com.virtualightning.dlna.DeviceInfo;
import com.virtualightning.dlna.Service;
import com.virtualightning.dlna.SubscribeEvent;
import com.virtualightning.dlna.constant.DeviceType;
import com.virtualightning.dlna.constant.SSDP;
import com.virtualightning.dlna.interfaces.*;
import com.virtualightning.dlna.soap.PauseCommand;
import com.virtualightning.dlna.soap.PlayCommand;
import com.virtualightning.dlna.soap.SetURICommand;
import com.virtualightning.dlna.soap.SetVolumeCommand;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

public class TestMain2 {
    static DeviceInfo deviceInfo = null;
    static Service service = null;
    static Service service2 = null;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DLNAClient client = new DLNAClient.Builder()
                .customInetAddressGetter(new InetAddressGetter() {
                    @Override
                    public String getInetAddress() {
                        return "192.168.18.10";
                    }
                })
                .customDeviceFilter(new DeviceFilter() {
                    @Override
                    public boolean isNeedDeviceType(DeviceInfo deviceInfo, boolean isResolved) {
                        if(isResolved) {
                            HashMap<String,Service> map = deviceInfo.getServiceMap();
                            if(map.containsKey(DeviceType.AV_TRANSPORT) && map.containsKey(DeviceType.RENDERING_CONTROL))
                                return true;
                        }
                        else return deviceInfo.getDeviceType().equals(DeviceType.ROOT_DEVICE);

                        return false;
                    }
                })
                .build();
        client.setOnBootstrapCompletedListener(new OnBootstrapCompletedListener() {
            @Override
            public void onBootstrapCompleted(DLNAClient client) {
                System.out.println("初始化成功");
            }
        });

        client.setOnFindDeviceListener(new OnFindDeviceListener() {
            @Override
            public void onFindDevice(DeviceInfo deviceInfo) {
                if(deviceInfo.getFriendlyName().contains("startdd")) {
                    TestMain2.deviceInfo = deviceInfo;
                    service = deviceInfo.getServiceMap().get(DeviceType.AV_TRANSPORT);
                    service2 = deviceInfo.getServiceMap().get(DeviceType.RENDERING_CONTROL);
                    System.out.println("选中设备");
                }
                System.out.println("发现新设备 : " + deviceInfo.getFriendlyName() + " , DeviceType : " + deviceInfo.getDeviceType());
            }
        });

        client.setOnDeviceQuitListener(new OnDeviceQuitListener() {
            @Override
            public void onDeviceQuit(DeviceInfo deviceInfo) {
                System.out.println("设备注销 : " + deviceInfo.getUSN() + " , DeviceType : " + deviceInfo.getDeviceType());
            }
        });

        client.setOnResourceRouteListener(new OnResourceRouteListener() {
            @Override
            public File onResourceRoute(String path) {
                System.out.println("正在申请文件，路径：" + path);
                return new File("/Users/xjw04/Desktop/mini.mp4");
            }
        });

        client.setOnServiceInfoListener(new OnServiceInfoListener() {
            @Override
            public void onServiceInfo(Service service) {
                System.out.println("获取服务成功 : Var Count : " + service.getStateVariableMap().size());
            }
        });


        client.setOnSubscribeEventListener(new OnSubscribeEventListener() {
            @Override
            public void onSubscribeEvent(Service service, SubscribeEvent event) {
                switch (event.getAction()) {
                    case SubscribeEvent.ACTION_SUBSCRIBE_SUCCESS:
                        System.out.println("订阅成功 ");
                        break;
                    case SubscribeEvent.ACTION_SUBSCRIBE_FAILED:
                        System.out.println("订阅失败");
                        break;
                    case SubscribeEvent.ACTION_SUBSCRIBE_CON_FAILED:
                        System.out.println("订阅连接失败");
                        break;
                    case SubscribeEvent.ACTION_RENEW_SUBSCRIBE_SUCCESS:
                        System.out.println("续订成功 ");
                        break;
                    case SubscribeEvent.ACTION_RENEW_SUBSCRIBE_FAILED:
                        System.out.println("续订失败");
                        break;
                    case SubscribeEvent.ACTION_RENEW_SUBSCRIBE_CON_FAILED:
                        System.out.println("续订连接失败");
                        break;
                    case SubscribeEvent.ACTION_CANCEL_SUBSCRIBE_SUCCESS:
                        System.out.println("取消订阅成功 ");
                        break;
                    case SubscribeEvent.ACTION_CANCEL_SUBSCRIBE_FAILED:
                        System.out.println("取消订阅失败");
                        break;
                    case SubscribeEvent.ACTION_CANCEL_SUBSCRIBE_CON_FAILED:
                        System.out.println("取消订阅连接失败");
                        break;
                    case SubscribeEvent.ACTION_SUBSCRIBE_RESPONSE:
                        System.out.println("订阅事件变化");
                        break;
                }
            }
        });

        client.startDLNA();

        while (true) {
            switch (scanner.nextInt()) {
//                case -1:
//                    isSelectIndex = true;
//                    break;
                case 0:
                    client.search(SSDP.ALL);
                    break;
                case 1:
                    client.executeCommand(service,new SetURICommand(0,"http://xue.startdd.com/plugins/nodebb-plugin-school/mirror/175/show.mp4"));
                    break;
                case 2:
                    client.executeCommand(service,new PlayCommand(0));
                    break;
                case 3:
                    client.executeCommand(service,new PauseCommand(0));
                    break;
                case 4:
                    client.subscribe(service);
                    break;
                case 5:
                    client.cancelSubscribe(service);
                    break;
                case 6:
                    client.subscribe(service2);
                    break;
                case 7:
                    client.cancelSubscribe(service2);
                    break;
                case 8:
                    client.executeCommand(service2,new SetVolumeCommand(0,100));
                    break;
                case 9:
                    client.executeCommand(service2,new SetVolumeCommand(0,0));
                    break;
                default:
                    client.release();
                    return;
            }
        }
    }
}
