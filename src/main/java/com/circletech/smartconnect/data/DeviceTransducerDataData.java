package com.circletech.smartconnect.data;

import com.circletech.smartconnect.model.DeviceTransducerData;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xieyingfei on 2016/12/15.observe entity class
 */
public class DeviceTransducerDataData{
    private DeviceTransducerData deviceTransducerData;
    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    public void set(DeviceTransducerData deviceTransducerData){
        rwl.writeLock().lock();

        try{
            this.deviceTransducerData = deviceTransducerData;
        }finally {
            rwl.writeLock().unlock();
        }
    }

    public DeviceTransducerData get(){
        rwl.readLock().lock();

        try{
            return deviceTransducerData;
        }finally {
            rwl.readLock().unlock();
        }
    }
}
