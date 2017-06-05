import java.io.*;
import java.net.Socket;

/**
 * Created by xjw04 on 17/5/31.
 */
public class SendHTTPRequest {
    public static void main(String[] args) throws Exception{
        Socket socket = new Socket("192.168.18.34",2869);
        OutputStream stream = socket.getOutputStream();
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeBytes("POST /upnphost/udhisapi.dll?control=uuid:d03299a6-6ca2-4068-8d66-92d96d651989+urn:upnp-org:serviceId:AVTransport HTTP/1.1\r\n");
        outputStream.writeBytes("Host: 192.168.18.34:2869\r\n");
        outputStream.writeBytes("Content-Type: text/xml; charset=\"utf-8\"\r\n");
        outputStream.writeBytes("SOAPACTION: \"urn:upnp-org:serviceId:AVTransport#SetAVTransportURI\"\r\n");
        outputStream.writeBytes("Content-Length: 480\r\n");
        outputStream.writeBytes("\r\n");

        File file = new File("/Users/xjw04/Desktop/setURI.xml");
        FileInputStream inputStream = new FileInputStream(file);
        byte buffer[] = new byte[1024];
        int length;

        outputStream.flush();

        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
            outputStream.flush();
        }

        System.out.println("发送完文件");

        inputStream.close();
        outputStream.writeBytes("\r\n");

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line;
        boolean first = true;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if(line.equals(""))
                if(first)
                    first = false;
                else break;
        }
    }
}


