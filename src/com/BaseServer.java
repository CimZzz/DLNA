package com;

import com.constant.ErrorCode;

import java.io.IOException;

public abstract class BaseServer {
    private boolean isRun;
    DLNAClientProxy client;

    BaseServer(DLNAClientProxy client) {
        isRun = false;
        this.client = client;
    }

    final void startServer(final TaskStatistic statist){
        isRun = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    onStart();
                    statist.complete();
                } catch (IOException e) {
                    if(isRun)
                        statist.failed();
                    if(isRun && client != null)
                        client.error(ErrorCode.SERVER_BOOTSTRAP_ERROR,e);
                    return;
                }
                while (isRun) {
                    onLoop();
                }
            }
        }).start();
    }

    boolean isRun() {
        return isRun;
    }

    final void closeServer() {
        isRun = false;
        client.close();
        onClose();
    }

    protected abstract void onStart() throws IOException;
    protected abstract void onLoop();
    protected abstract void onClose();

}
