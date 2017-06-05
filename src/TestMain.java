import com.DLNAClient;
import com.DeviceInfo;
import com.SubscribeEvent;
import com.interfaces.*;

import java.io.File;
import java.util.Scanner;

public class TestMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DLNAClient client = new DLNAClient();
        client.setOnSubscribeEventListener(new OnSubscribeEventListener() {
            @Override
            public void onSubscribeEvent(DeviceInfo deviceInfo, SubscribeEvent event) {
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
                }
            }
        });
        client.setOnResourceRouteListener(new OnResourceRouteListener() {
            @Override
            public File onResourceRoute(String path) {
                System.out.println("haha.mp4");
                return new File("/Users/xjw04/Desktop/mini.mp4");
            }
        });
        client.setOnStartCompletedListener(new OnStartCompletedListener() {
            @Override
            public void onStartCompleted(DLNAClient client) {
                System.out.println("OnStartCompleted");
                client.search();
            }
        });
        client.setOnFindNewDeviceListener(new OnFindNewDeviceListener() {
            @Override
            public void onFindNewDevice(DeviceInfo deviceInfo) {
                System.out.println("OnFindNewDevice  Device Name : " + deviceInfo.getFriendlyName());
            }
        });
        client.setOnDeviceQuitListener(new OnDeviceQuitListener() {
            @Override
            public void onDeviceQuit(DeviceInfo deviceInfo) {
                System.out.println("OnDeviceQuit  Device Name : " + deviceInfo.getFriendlyName());
            }
        });
        client.setOnErrorListener(new OnErrorListener() {
            @Override
            public void onError(DLNAClient client, int errorCode, Object... args) {
                System.out.println("OnError  Error Code : " + errorCode);
            }
        });
        client.setOnInfoListener(new OnInfoListener() {
            @Override
            public void onInfo(int what, int other) {
                System.out.println("OnInfo  What : " + what);
            }
        });
        client.startDLNA();

        boolean isSelectIndex = false;
        DeviceInfo deviceInfo = null;

        while (true) {
            if(isSelectIndex) {
                int index = scanner.nextInt();
                deviceInfo = client.getCurrentInfo().get(index);
                isSelectIndex = false;
                System.out.println("已经选中设备 ： " + deviceInfo.getFriendlyName());
                continue;
            }
            switch (scanner.nextInt()) {
                case -1:
                    isSelectIndex = true;
                    break;
                case 0:
                    client.search();
                    break;
                case 1:
                    for(DeviceInfo info : client.getCurrentInfo())
//                        System.out.println("info >> Device Name : " + info.friendlyName + " , ctrlPath : " + info.avtControlPath + " , USN : " + info.USN);
                        System.out.println("info >> Device Name : " + info.getFriendlyName());
                    break;
                case 2:
                    client.setURI(deviceInfo,"haha.mp4");
                    break;
                case 3:
                    client.play(deviceInfo);
                    break;
                case 4:
                    client.pause(deviceInfo);
                    break;
                case 5:
                    client.closeDLNA();
                    break;
                case 6:
                    client.subscribe(deviceInfo);
                    break;
                case 7:
                    client.unsubscribe(deviceInfo);
                    break;
                case 8:
                    client.seek(deviceInfo,12000);
                    break;
                default:
                    return;
            }
        }
    }
}
