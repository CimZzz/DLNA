package com;

import java.io.File;
import java.io.InputStream;

/**
 * Created by CimZzz on 17/5/31.<br>
 * Project Name : Hunban.com Education<br>
 * Since : Education_0.0.1<br>
 * Description:<br>
 * Description
 */
public interface IDLNACallback {
    File getResourcesFile(String path) throws Exception;
    void onError(Exception e);
    void onFindNewDevice(DeviceInfo info);
}
