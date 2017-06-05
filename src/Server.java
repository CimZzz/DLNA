import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by xjw04 on 17/5/27.
 */
public class Server {
    public static void main(String[] args) throws Exception {
        server();
    }

    private static void server() throws Exception {
        MulticastSocket server = new MulticastSocket(1900);
        InetAddress inetAddress = Inet4Address.getByName("239.255.255.250");
        server.joinGroup(inetAddress);
        byte[] bytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(bytes,1024);




//        byte[] bytes2 = ("M-SEARCH * HTTP/1.1" + "\r\n"
//                + "MAN: \"ssdp:discover\"" + "\r\n"
//                + "MX: 5" + "\r\n"
//                + "HOST: 239.255.255.250:1900" + "\r\n"
//                + "ST: ssdp:all" + "\r\n"
//                + "\r\n").getBytes();
//        DatagramPacket packet2 = new DatagramPacket(bytes,bytes.length);
//        packet2.setPort(1900);
//        packet2.setAddress(InetAddress.getByName("239.255.255.250"));
//        server.send(packet2);


//        int length = 0;
//        File parentFile = new File("/Users/xjw04/Desktop/tmp/udp");
//        parentFile.mkdirs();
        while (true) {
            server.receive(packet);

            System.out.println(new String(packet.getData(),0,packet.getLength()));
//            File file = new File(parentFile,"file"+ length++);
//            if(file.exists())
//                file.delete();
//            file.createNewFile();
//            FileOutputStream outputStream = new FileOutputStream(file);
//            outputStream.write(packet.getData(),0,packet.getLength());
//            outputStream.flush();
//            outputStream.close();
            System.out.println("****************************************"+packet.getSocketAddress()+"********************************************");
        }
    }
}
