package com.circletech.smartconnect.data;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.model.DeviceTransducerData;
import com.circletech.smartconnect.service.DeviceTransducerDataService;
import com.circletech.smartconnect.util.LoggerUtil;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class OutputTransducerBuffer {

    private PriorityQueue<TransducerTimeStampData> outputTransducerBuffer;
    private ReentrantReadWriteLock rwl;

    public OutputTransducerBuffer(int maxsize){

        outputTransducerBuffer = new PriorityQueue<>(maxsize, new Comparator<TransducerTimeStampData>() {
            @Override
            public int compare(TransducerTimeStampData o1, TransducerTimeStampData o2) {
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

    public void add(TransducerTimeStampData transducerTimeStampData){

        rwl.writeLock().lock();
        try{
            outputTransducerBuffer.add(transducerTimeStampData);
        }finally {
            rwl.writeLock().unlock();
        }
    }

    public void output(DeviceTransducerDataService deviceTransducerDataService,
                       DeviceTransducerDataData deviceTransducerDataData,
                       CommDataProcessor CommDataProcessor){
        rwl.writeLock().lock();
        try {
            if (outputTransducerBuffer.size() > 0) {

                int size = outputTransducerBuffer.size();
                for (int i = 0; i < size; i++) {
                    TransducerTimeStampData data = outputTransducerBuffer.poll();

                    DeviceTransducerData temp = data.getDeviceTransducerData();
                    deviceTransducerDataService.save(temp);

                    deviceTransducerDataData.set(temp);
                    CommDataProcessor.setTransducerDataData(deviceTransducerDataData);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LoggerUtil.getInstance().info(e.getMessage());
        }finally {
            rwl.writeLock().unlock();
        }
    }

    public void scheduleOutput(DeviceTransducerDataService deviceTransducerDataService,
                       DeviceTransducerDataData deviceTransducerDataData,
                       CommDataProcessor CommDataProcessor){
        rwl.writeLock().lock();
        try {
            if (outputTransducerBuffer.size() > 0) {

                int size = outputTransducerBuffer.size();
                for (int i = 0; i < size; i++) {
                    TransducerTimeStampData data = outputTransducerBuffer.poll();

                    DeviceTransducerData temp = data.getDeviceTransducerData();
                    deviceTransducerDataService.save(temp);

                    deviceTransducerDataData.set(temp);
                    CommDataProcessor.setTransducerDataData(deviceTransducerDataData);
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
