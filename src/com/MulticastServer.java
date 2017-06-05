package com;

import com.constant.DeviceType;
import com.constant.ErrorCode;
import com.constant.SSDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastServer extends BaseServer {
    private MulticastSocket socket;

    private byte[] recvBuffer;
    private DatagramPacket receiver;

    MulticastServer(DLNAClientProxy client) {
        super(client);
    }

    @Override
    protected void onStart() throws IOException {
        socket = new MulticastSocket(1900);
        InetAddress inetAddress = Inet4Address.getByName("239.255.255.250");
        socket.joinGroup(inetAddress);
        recvBuffer = new byte[1024];
        receiver = new DatagramPacket(recvBuffer,recvBuffer.length);
    }

    @Override
    protected void onLoop() {
        try {
            socket.receive(receiver);
            DeviceInfo info = DLNAAnalyzer.analyzeDeviceInfoFromMulticast(receiver);
            if(info != null && info.deviceType.equals(DeviceType.AV_TRANSPORT)) {
                if (info.NTS.equals(SSDP.ALIVE))
                    client.findNewDevice(info);
                else
                    client.deviceQuit(info);
            }
        } catch (IOException e) {
            if(client != null)
                client.error(ErrorCode.MULTICAST_RECV_ERROR,e);
        }
    }

    @Override
    protected void onClose() {
        socket.close();
        recvBuffer = null;
        receiver = null;
    }
}
