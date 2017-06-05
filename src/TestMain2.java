import com.constant.DeviceType;
import com.new_version.DLNAClient;
import com.new_version.DeviceInfo;
import com.new_version.Service;
import com.new_version.interfaces.*;
import com.new_version.soap.PlayCommand;
import com.new_version.soap.SetURICommand;

import java.io.File;
import java.util.Scanner;

public class TestMain2 {
    static DeviceInfo deviceInfo = null;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DLNAClient client = new DLNAClient.Builder().build();
        client.setOnBootstrapCompletedListener(new OnBootstrapCompletedListener() {
            @Override
            public void onBootstrapCompleted(DLNAClient client) {
                System.out.println("初始化成功");
            }
        });

        client.setOnFindDeviceListener(new OnFindDeviceListener() {
            @Override
            public void onFindDevice(DeviceInfo deviceInfo) {
                if(deviceInfo.getDeviceType().equals(DeviceType.AV_TRANSPORT))
                    TestMain2.deviceInfo = deviceInfo;
                System.out.println("发现新设备 : " + deviceInfo.getUSN() + " , DeviceType : " + deviceInfo.getDeviceType());
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

        client.startDLNA();

        while (true) {
//            if(isSelectIndex) {
//                int index = scanner.nextInt();
//                deviceInfo = client.getCurrentInfo().get(index);
//                isSelectIndex = false;
//                System.out.println("已经选中设备 ： " + deviceInfo.getFriendlyName());
//                continue;
//            }
            switch (scanner.nextInt()) {
//                case -1:
//                    isSelectIndex = true;
//                    break;
                case 0:
                    client.search(DeviceType.AV_TRANSPORT);
                    break;
                case 1:
                    client.executeCommand(deviceInfo.getServiceMap().get(DeviceType.AV_TRANSPORT),new SetURICommand(client,null,"mp4222"));
                    break;
                case 2:
                    client.executeCommand(deviceInfo.getServiceMap().get(DeviceType.AV_TRANSPORT),new PlayCommand(null));
                    break;
                case 3:
                    client.findServiceInfo(deviceInfo.getServiceMap().get(DeviceType.AV_TRANSPORT));
                    break;
                default:
                    return;
            }
        }
    }
}
