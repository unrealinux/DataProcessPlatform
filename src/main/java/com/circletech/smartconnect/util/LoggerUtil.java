package com.circletech.smartconnect.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xieyingfei on 2016/12/27.
 */
public class LoggerUtil {

    private LoggerUtil(){
    }

    private static class LoggerSingleTon{
        private static final Logger instance = LoggerFactory.getLogger("data_process_platform");
    }

    public static Logger getInstance(){
        return LoggerSingleTon.instance;
    }
}
