package com.new_version.factory;

import java.util.concurrent.ExecutorService;

public interface ThreadPoolFactory {
    ExecutorService createThreadPool();
}
