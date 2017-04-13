package com.circletech.smartconnect.repository;

import com.circletech.smartconnect.model.DeviceSystemInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by xieyingfei on 2016/12/12.
 */
public interface DeviceSystemInfoRepository extends CrudRepository<DeviceSystemInfo, Long> {

    List<DeviceSystemInfo> findByDeviceId(String deviceId);
}

