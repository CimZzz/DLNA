package com.new_version;

import com.constant.ErrorCode;
import com.constant.SSDP;
import com.new_version.tools.BoundedInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UnicastServer extends BaseServer {
    private DatagramSocket socket;
    private int port;

    private byte[] recvBuffer;
    private DatagramPacket receiver;

    UnicastServer(DLNAContext dlnaContext, int port) {
        super(dlnaContext);
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
            BoundedInputStream inputStream = new BoundedInputStream(
                    new ByteArrayInputStream(receiver.getData()),receiver.getLength()
            );
            HTTPHeader httpHeader = HTTPHeader.analyzeParams(inputStream);

            if(httpHeader != null) {
                DeviceInfo deviceInfo = DLNAAnalyzer.analyzeDeviceInfo(httpHeader);
                dlnaContext.findNewDevice(deviceInfo,false);
            }
        } catch (IOException e) {
            dlnaContext.error(ErrorCode.UNICAST_RECV_ERROR,e);
        }
    }

    @Override
    protected void onClose() {
        socket.close();
        socket = null;
        recvBuffer = null;
        receiver = null;
    }

    void search(String deviceType) {
        if(!isRun())
            return;
        try {
            byte[] bytes = ("M-SEARCH * HTTP/1.1" + "\r\n"
                    + "MAN: \""+ SSDP.DISCOVER+"\"" + "\r\n"
                    + "MX: 5" + "\r\n"
                    + "HOST: 239.255.255.250:1900" + "\r\n"
                    + "ST: " + deviceType + "\r\n"
                    + "\r\n").getBytes();

            DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
            packet.setAddress(InetAddress.getByName("239.255.255.250"));
            packet.setPort(1900);
            socket.send(packet);
        } catch (IOException e) {
            dlnaContext.error(ErrorCode.UNICAST_SEARCH_ERROR,e);
        }
    }
}
