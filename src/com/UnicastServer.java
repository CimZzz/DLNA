package com;

import com.constant.ErrorCode;
import com.constant.SSDP;

import java.io.IOException;
import java.net.*;

public class UnicastServer extends BaseServer{
    private DatagramSocket socket;
    private int port;

    private byte[] recvBuffer;
    private DatagramPacket receiver;

    UnicastServer(DLNAClientProxy client,int port) {
        super(client);

        this.port = port;
    }

    @Override
    protected void onStart() throws IOException {
        socket = new DatagramSocket(port);
        recvBuffer = new byte[512];
        receiver = new DatagramPacket(recvBuffer,recvBuffer.length);
    }

    @Override
    protected void onLoop() {
        try {
            socket.receive(receiver);
            DeviceInfo deviceInfo = DLNAAnalyzer.analyzeDeviceInfoFromSearch(receiver);
            client.findNewDevice(deviceInfo);
        } catch (IOException e) {
            client.error(ErrorCode.UNICAST_RECV_ERROR,e);
        }
    }

    @Override
    protected void onClose() {
        socket.close();
        socket = null;
        recvBuffer = null;
        receiver = null;
    }




    void search() {
        if(!isRun())
            return;
        try {
            byte[] bytes = ("M-SEARCH * HTTP/1.1" + "\r\n"
                    + "MAN: \""+ SSDP.DISCOVER+"\"" + "\r\n"
                    + "MX: 5" + "\r\n"
                    + "HOST: 239.255.255.250:1900" + "\r\n"
                    + "ST: urn:schemas-upnp-org:service:AVTransport:1" + "\r\n"
                    + "\r\n").getBytes();

            DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
            packet.setAddress(InetAddress.getByName("239.255.255.250"));
            packet.setPort(1900);
            socket.send(packet);
        } catch (IOException e) {
            client.error(ErrorCode.UNICAST_SEARCH_ERROR,e);
        }
    }
}
