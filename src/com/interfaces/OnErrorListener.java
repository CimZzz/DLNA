package com.interfaces;

import com.DLNAClient;

/**
 * Created by xjw04 on 17/6/1.
 */
public interface OnErrorListener {
    void onError(DLNAClient client,int errorCode,Object... args);
}
