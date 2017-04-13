package com.circletech.smartconnect.data;

import com.circletech.smartconnect.model.DeviceTransducerData;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class TransducerTimeStampData {

    private DeviceTransducerData deviceTransducerData;
    private Long timestamp;

    public DeviceTransducerData getDeviceTransducerData() {
        return deviceTransducerData;
    }

    public void setDeviceTransducerData(DeviceTransducerData deviceTransducerData) {
        this.deviceTransducerData = deviceTransducerData;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public TransducerTimeStampData(DeviceTransducerData deviceTransducerData, Long timestamp) {
        this.deviceTransducerData = deviceTransducerData;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TransducerTimeStampData{" +
                "deviceTransducerData=" + deviceTransducerData +
                ", timestamp=" + timestamp +
                '}';
    }
}
