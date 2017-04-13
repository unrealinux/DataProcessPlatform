package com.circletech.smartconnect.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xieyingfei on 2016/12/27.
 */
public class ThreadPoolUtil {

    private ThreadPoolUtil(){
    }

    private static class ThreadPoolSingleTon {
        private static final ExecutorService intance = Executors.newCachedThreadPool();
    }

    public static ExecutorService getInstance(){
        return ThreadPoolSingleTon.intance;
    }
}
