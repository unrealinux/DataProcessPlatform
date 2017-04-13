package com.circletech.smartconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by xieyingfei on 2016/12/30.Custom Configuration
 */
@Configuration
public class CustomConfig {

    @Value("${custom.dataqueuesize}")
    private int dataqueuesize;

    @Value("${custom.consumerthreadsize}")
    private int consumerthreadsize;

    @Value("${custom.monitorperiod}")
    private int monitorperiod;

    @Value("${custom.reinvokeprocess}")
    private int reinvokeprocess;

    @Value("${custom.senderthreadsize}")
    private int senderthreadsize;

    @Value("${custom.datasourcenum}")
    private int datasourcenum;

    public int getDatasourcenum() {
        return datasourcenum;
    }

    public int getSenderthreadsize() {
        return senderthreadsize;
    }

    public int getDataQueueSize() {
        return dataqueuesize;
    }

    public int getConsumerThreadSize() {
        return consumerthreadsize;
    }

    public int getMonitorperiod() {
        return monitorperiod;
    }

    public int getReinvokeprocess() {
        return reinvokeprocess;
    }
}
