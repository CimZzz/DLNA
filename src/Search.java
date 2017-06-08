import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Search {
    public static void main(String[] args) throws Exception {
        search();
    }



    public static void search() throws Exception {
        DatagramSocket socket = new DatagramSocket(21314);
        byte[] buffer = new byte[1024];
        byte[] bytes = ("M-SEARCH * HTTP/1.1" + "\r\n"
                + "MAN: \"ssdp:discover\"" + "\r\n"
                + "MX: 5" + "\r\n"
                + "HOST: 239.255.255.250:1900" + "\r\n"
//                + "ST: urn:schemas-upnp-org:service:AVTransport:1" + "\r\n"
//                + "ST: ssdp:all" + "\r\n"
                + "ST: upnp:rootdevice" + "\r\n"
                + "\r\n").getBytes();
        DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
        packet.setAddress(InetAddress.getByName("239.255.255.250"));
        packet.setPort(1900);
        DatagramPacket receiver = new DatagramPacket(buffer,buffer.length);
        socket.send(packet);
        System.out.print("Send Completed\n");
        while (true) {
            socket.receive(receiver);

            System.out.println(new String(receiver.getData(), 0, receiver.getLength()));
        }
    }
}