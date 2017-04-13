package com.circletech.smartconnect.repository;

import com.circletech.smartconnect.model.DeviceTransducerData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by xieyingfei on 2016/12/9.
 */
public interface DeviceTransducerDataRepository extends CrudRepository<DeviceTransducerData, Long> {

    List<DeviceTransducerData> findByDeviceId(String deviceId);
}
