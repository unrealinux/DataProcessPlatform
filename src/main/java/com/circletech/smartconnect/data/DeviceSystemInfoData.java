package com.circletech.smartconnect.data;

import com.circletech.smartconnect.model.DeviceSystemInfo;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xieyingfei on 2016/12/23.
 */
public class DeviceSystemInfoData {

    private DeviceSystemInfo deviceSystemInfo;

    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    public void set(DeviceSystemInfo deviceSystemInfo){

        rwl.writeLock().lock();

        try{
            this.deviceSystemInfo = deviceSystemInfo;
        }finally {
            rwl.writeLock().unlock();
        }
    }

    public DeviceSystemInfo get(){

        rwl.readLock().lock();

        try{
            return deviceSystemInfo;
        }finally {
            rwl.readLock().unlock();
        }
    }
}
