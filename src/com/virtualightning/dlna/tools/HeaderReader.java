package com.virtualightning.dlna.tools;

import java.io.IOException;
import java.io.InputStream;

public class HeaderReader {
    private final InputStream stream;

    public HeaderReader(InputStream stream) {
        this.stream = stream;
    }

    public String readLine() throws IOException {
        StringBuffer buffer = new StringBuffer(30);
        boolean isR = false;
        boolean isN = false;
        while (true) {
            int readChar = stream.read();
            if (readChar == '\r')
                isR = true;
            else if(readChar == '\n')
                isN = true;

            if(isR) {
                isR = false;
                continue;
            }
            else if(isN) {
                break;
            }
            buffer.append((char)readChar);
        }

        return buffer.toString();
    }
}
