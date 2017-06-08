package com.virtualightning.dlna.interfaces;

import java.io.InputStream;

public interface XmlDecoder<T> {
    boolean decoderXMLStream(T t, InputStream inputStream);
}
