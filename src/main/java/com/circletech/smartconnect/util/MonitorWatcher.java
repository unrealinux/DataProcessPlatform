package com.circletech.smartconnect.util;

/**
 * Created by xieyingfei on 2016/12/7.
 */
public class MonitorWatcher {

    private long lastCheckedTime = System.currentTimeMillis();
    private final long waitPeriod;

    public MonitorWatcher(long waitPeriod){
        this.waitPeriod=waitPeriod;
    }

    public void feed(){
        lastCheckedTime = System.currentTimeMillis();
    }

    public boolean checkAlive(){
        return (System.currentTimeMillis()-lastCheckedTime)<=waitPeriod;
    }
}
