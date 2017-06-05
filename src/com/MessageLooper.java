package com;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageLooper<T> {
    private final Object locker;
    private final Object longRunLocker;
    private final boolean isLongRun;
    private final ConcurrentLinkedQueue<T> queue;

    private IMessageCallBack<T> messageCallBack;
    private volatile boolean runFlag;
    private volatile boolean longRunFlag;


    public MessageLooper() {
        this(false);
    }

    public MessageLooper (boolean isLongRun) {
        locker = new Object();
        longRunLocker = new Object();
        this.isLongRun = isLongRun;
        queue = new ConcurrentLinkedQueue<>();
        runFlag = false;
    }

    public void setMessageCallBack(IMessageCallBack<T> messageCallBack) {
        this.messageCallBack = messageCallBack;
    }

    public void sendMessage(T t) {
        queue.add(t);
        synchronized (locker) {
            if(runFlag)
                locker.notify();
        }
    }

    public void startLoopAsync() {
        if(runFlag)
            return;
        runFlag = true;

        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try{
                    while(runFlag) {
                        if(isLongRun)
                            longRun();
                        else shortRun();

                        synchronized (locker) {
                            if(runFlag)
                                locker.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    runFlag = false;
                }
            }
        }).start();
    }

    public void startLoop(){
        if(runFlag)
            return;
        runFlag = true;
        try{
            while(runFlag) {
                if(isLongRun)
                    longRun();
                else shortRun();

                synchronized (locker) {
                    if(runFlag)
                        locker.wait();
                }
            }
        } catch (InterruptedException e) {
            runFlag = false;
        }
    }

    public void stopLoop() {
        synchronized (locker) {
            queue.clear();
            runFlag = false;
            locker.notify();

            if(isLongRun)
                longRunCompleted();
        }
    }

    private void longRun() throws InterruptedException {
        while(!queue.isEmpty()) {
            longRunFlag = false;
            T msg = queue.poll();
            if(messageCallBack != null)
                messageCallBack.handleMessage(this,msg);
            synchronized (longRunLocker) {
                if(!longRunFlag)
                    longRunLocker.wait();
            }
        }
    }

    private void shortRun() {
        while(!queue.isEmpty()) {
            T msg = queue.poll();
            if(messageCallBack != null)
                messageCallBack.handleMessage(this,msg);
        }
    }

    public void longRunCompleted() {
        synchronized (longRunLocker) {
            longRunFlag = true;
            longRunLocker.notify();
        }
    }

    public void removeMessage(T t) {
        queue.remove(t);
    }

    public List<T> getRemainderMessage() {
        return new ArrayList<>(queue);
    }

    public void removeAll() {
        queue.clear();
    }

    public interface IMessageCallBack<T> {
        void handleMessage(MessageLooper<T> messageLooper,T t);
    }
}