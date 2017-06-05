package com.new_version;

import com.*;
import com.constant.ErrorCode;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Created by CimZzz on 17/5/31.<br>
 * Project Name : Hunban.com Education<br>
 * Since : Education_0.0.1<br>
 * Description:<br>
 * Description
 */
public class HTTPServer extends BaseServer {
    private ServerSocket socket;
    private int port;

    HTTPServer(DLNAContext dlnaContext, int port) {
        super(dlnaContext);
        this.port = port;
    }

    @Override
    protected void onStart() throws IOException {
        socket = new ServerSocket(port);
    }

    @Override
    protected void onLoop(){
        final Socket connSocket;
        try {
            connSocket = socket.accept();
            dlnaContext.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        handleSocket(connSocket);
                    } catch (Exception e) {
                        dlnaContext.error(ErrorCode.HTTP_RES_ERROR,e);
                    }
                    finally {
                        try {if(!connSocket.isClosed())connSocket.close();} catch (IOException e) {}
                    }
                }
            });
        } catch (IOException e) {
            dlnaContext.error(ErrorCode.HTTP_RECV_CON_ERROR,e);
        }
    }

    @Override
    protected void onClose() {
        try {
            socket.close();
        } catch (IOException e) {}
        socket = null;
    }


    private void handleSocket(Socket socket) throws Exception {
        InputStream socketInput = socket.getInputStream();
        HTTPHeader params = HTTPHeader.analyzeParams(socketInput);
        /*
         * 如果请求报文中包含Content-Length字段，则判断为订阅信息
         */
        if(params.contentLength != null) {
            String sid = params.otherHeaders.get("SID").trim();
            long seq = Long.parseLong(params.otherHeaders.get("SEQ").trim());
            String xml = null;
            Service service = dlnaContext.findServiceInfoBySID(sid);

            //如果订阅号有效，解析XML文档，否则略过
            if(service != null)
                xml = DLNAAnalyzer.analyzeXMLFromStream(socketInput,params.contentLength);

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeBytes("HTTP/1.1 200 STATUS_OK\r\n\r\n");
            dataOutputStream.flush();
            socket.close();

            //解析xml文件
            if(xml != null) {
                SubscribeEvent event = new SubscribeEvent();
                event.seq = seq;
                if(XMLAnalyzer.analyzeSubscibeEvent(dlnaContext.instancePool,new ByteArrayInputStream(xml.getBytes())))
                    //解析xml成功，发送订阅事件
                    dlnaContext.subscribeEvent(service,event);
            }
        }
        /*
         * 否则，则认为是访问本地资源
         */
        else {
            File file = dlnaContext.getResourcesFile(params.methodPath.substring(1,params.methodPath.length()));

            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeBytes("HTTP/1.0 200 STATUS_OK\r\n");
            if(file != null) {
                outputStream.writeBytes("Content-Length: "+file.length()+"\r\n");
                outputStream.writeBytes("\r\n");
                byte[] buffer = new byte[1024];
                int readLength;
                FileInputStream inputStream = new FileInputStream(file);

                while ((readLength = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer,0,readLength);
                    outputStream.flush();
                }

                inputStream.close();

                outputStream.writeBytes("\r\n");
            } else {
                outputStream.writeBytes("\r\n");
            }

            System.out.println("响应完毕");
        }
    }
}
