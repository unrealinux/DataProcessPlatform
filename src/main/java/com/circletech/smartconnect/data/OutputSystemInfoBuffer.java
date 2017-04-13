package com.circletech.smartconnect.data;


import com.circletech.smartconnect.model.DeviceSystemInfo;
import com.circletech.smartconnect.service.DeviceSystemInfoService;
import com.circletech.smartconnect.util.LoggerUtil;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class OutputSystemInfoBuffer {

    private PriorityQueue<SystemInfoTimeStampData> outputSystemInfoBuffer;
    private ReentrantReadWriteLock rwl;

    public OutputSystemInfoBuffer(int maxsize) {

        this.outputSystemInfoBuffer = new PriorityQueue<SystemInfoTimeStampData>(maxsize, new Comparator<SystemInfoTimeStampData>() {
            @Override
            public int compare(SystemInfoTimeStampData o1, SystemInfoTimeStampData o2) {
                if(o1.getTimestamp() < o2.getTimestamp()){
                    return -1;
                }else if(o1.getTimestamp() > o2.getTimestamp()){
                    return 1;
                }else{
                    return 0;
                }
            }
        });

        rwl = new ReentrantReadWriteLock();
    }

    public void add(SystemInfoTimeStampData systemInfoTimeStampData){

        rwl.writeLock().lock();
        try {
            outputSystemInfoBuffer.add(systemInfoTimeStampData);
        }finally {
            rwl.writeLock().unlock();
        }
    }

    public void output(DeviceSystemInfoService deviceSystemInfoService, DeviceSystemInfoData deviceSystemInfoData){
        rwl.writeLock().lock();
        try {
            if (outputSystemInfoBuffer.size() > 0) {

                int size = outputSystemInfoBuffer.size();
                for (int i = 0; i < size; i++) {
                    SystemInfoTimeStampData data = outputSystemInfoBuffer.poll();

                    DeviceSystemInfo tempSystemInfo = data.getDeviceSystemInfo();

                    deviceSystemInfoService.save(tempSystemInfo);

                    deviceSystemInfoData.set(tempSystemInfo);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LoggerUtil.getInstance().info(e.getMessage());
        }finally {
            rwl.writeLock().unlock();
        }
    }

    public void scheduleOutput(DeviceSystemInfoService deviceSystemInfoService, DeviceSystemInfoData deviceSystemInfoData){
        rwl.writeLock().lock();
        try {
            if (outputSystemInfoBuffer.size() > 0) {

                int size = outputSystemInfoBuffer.size();
                for (int i = 0; i < size; i++) {
                    SystemInfoTimeStampData data = outputSystemInfoBuffer.poll();

                    DeviceSystemInfo tempSystemInfo = data.getDeviceSystemInfo();

                    deviceSystemInfoService.save(tempSystemInfo);

                    deviceSystemInfoData.set(tempSystemInfo);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LoggerUtil.getInstance().info(e.getMessage());
        }finally {
            rwl.writeLock().unlock();
        }
    }
}
