package com.circletech.smartconnect.sender;

import com.circletech.smartconnect.data.DeviceSystemInfoData;
import com.circletech.smartconnect.data.OutputSystemInfoBuffer;
import com.circletech.smartconnect.service.DeviceSystemInfoService;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class ScheduledSystemInfoSender implements Runnable {

    private DeviceSystemInfoService deviceSystemInfoService;
    private OutputSystemInfoBuffer outputSystemInfoBuffer;
    private DeviceSystemInfoData deviceSystemInfoData;

    public OutputSystemInfoBuffer getOutputSystemInfoBuffer() {
        return outputSystemInfoBuffer;
    }

    public void setOutputSystemInfoBuffer(OutputSystemInfoBuffer outputSystemInfoBuffer) {
        this.outputSystemInfoBuffer = outputSystemInfoBuffer;
    }

    public DeviceSystemInfoService getDeviceSystemInfoService() {
        return deviceSystemInfoService;
    }

    public void setDeviceSystemInfoService(DeviceSystemInfoService deviceSystemInfoService) {
        this.deviceSystemInfoService = deviceSystemInfoService;
    }

    public DeviceSystemInfoData getDeviceSystemInfoData() {
        return deviceSystemInfoData;
    }

    public void setDeviceSystemInfoData(DeviceSystemInfoData deviceSystemInfoData) {
        this.deviceSystemInfoData = deviceSystemInfoData;
    }

    public ScheduledSystemInfoSender(DeviceSystemInfoService deviceSystemInfoService, OutputSystemInfoBuffer outputSystemInfoBuffer, DeviceSystemInfoData deviceSystemInfoData) {
        this.deviceSystemInfoService = deviceSystemInfoService;
        this.outputSystemInfoBuffer = outputSystemInfoBuffer;
        this.deviceSystemInfoData = deviceSystemInfoData;
    }

    @Override
    public void run() {
        outputSystemInfoBuffer.scheduleOutput(deviceSystemInfoService, deviceSystemInfoData);
    }
}
