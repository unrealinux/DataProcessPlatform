package com.circletech.smartconnect.service;

import com.circletech.smartconnect.model.DeviceSystemInfo;
import com.circletech.smartconnect.repository.DeviceSystemInfoRepository;
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
public class DeviceSystemInfoService {

    @Autowired
    private DeviceSystemInfoRepository deviceSystemInfoRepository;

    private ReadWriteLock rwl = new ReentrantReadWriteLock();
    private List<DeviceSystemInfo> deviceSystemInfoList = new ArrayList<DeviceSystemInfo>();

    @Transactional
    public void savebylength(){
        rwl.writeLock().lock();

        try{
            if(deviceSystemInfoList.size() > ConstantUtil.DATABASE_WRITE_LENGTH){
                deviceSystemInfoRepository.save(deviceSystemInfoList);
                deviceSystemInfoList.clear();
            }
        }finally {
            rwl.writeLock().unlock();
        }
    }

    public void save(DeviceSystemInfo systemInfo){

        rwl.writeLock().lock();

        try {
            deviceSystemInfoList.add(systemInfo);
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
            if(deviceSystemInfoList.size() > 0){
                deviceSystemInfoRepository.save(deviceSystemInfoList);
                deviceSystemInfoList.clear();
            }
        }finally {
            rwl.writeLock().unlock();
        }
    }
}
