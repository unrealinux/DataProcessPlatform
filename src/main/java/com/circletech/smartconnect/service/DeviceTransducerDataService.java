package com.circletech.smartconnect.service;

import com.circletech.smartconnect.model.DeviceTransducerData;
import com.circletech.smartconnect.repository.DeviceTransducerDataRepository;
import com.circletech.smartconnect.util.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xieyingfei on 2016/12/12.
 */
@Service
public class DeviceTransducerDataService {

    @Autowired
    private DeviceTransducerDataRepository deviceTransducerDataRepository;

    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    private List<DeviceTransducerData> deviceTransducerDataList = new ArrayList<DeviceTransducerData>();

    @Transactional
    public void savebylength(){
        rwl.writeLock().lock();

        try{
            if(deviceTransducerDataList.size() > ConstantUtil.DATABASE_WRITE_LENGTH){
                deviceTransducerDataRepository.save(deviceTransducerDataList);
                deviceTransducerDataList.clear();
            }
        }finally {
            rwl.writeLock().unlock();
        }
    }

    public void save(DeviceTransducerData data){

       rwl.writeLock().lock();

       try{
           deviceTransducerDataList.add(data);
           savebylength();
       }finally {
           rwl.writeLock().unlock();
       }

    }

    @Scheduled(fixedRate = ConstantUtil.DATABASE_WRITE_SCHEDULE)
    @Transactional
    public void saveBatch(){

        rwl.writeLock().lock();

        try {
            if (deviceTransducerDataList.size() > 0) {
                deviceTransducerDataRepository.save(deviceTransducerDataList);
                deviceTransducerDataList.clear();
            }
        }finally {
            rwl.writeLock().unlock();
        }
    }
}
