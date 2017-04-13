package com.circletech.smartconnect.data;

import com.circletech.smartconnect.model.DeviceSystemInfo;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class SystemInfoTimeStampData {

    private DeviceSystemInfo deviceSystemInfo;
    private Long timestamp;

    public DeviceSystemInfo getDeviceSystemInfo() {
        return deviceSystemInfo;
    }

    public void setDeviceSystemInfo(DeviceSystemInfo deviceSystemInfo) {
        this.deviceSystemInfo = deviceSystemInfo;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public SystemInfoTimeStampData(DeviceSystemInfo deviceSystemInfo, Long timestamp) {
        this.deviceSystemInfo = deviceSystemInfo;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SystemInfoTimeStampData{" +
                "deviceSystemInfo=" + deviceSystemInfo +
                ", timestamp=" + timestamp +
                '}';
    }
}
