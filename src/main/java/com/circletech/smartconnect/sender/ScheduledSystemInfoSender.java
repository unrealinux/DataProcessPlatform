package com.circletech.smartconnect.sender;

import com.circletech.smartconnect.data.DeviceSystemInfoData;
import com.circletech.smartconnect.data.OutputSystemInfoBuffer;
import com.circletech.smartconnect.service.DeviceSystemInfoService;
import javafx.util.Builder;

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

    public static class ScheduledSystemInfoSenderBuilder implements Builder<ScheduledSystemInfoSender> {

        private DeviceSystemInfoService deviceSystemInfoService;
        private OutputSystemInfoBuffer outputSystemInfoBuffer;
        private DeviceSystemInfoData deviceSystemInfoData;

        public ScheduledSystemInfoSenderBuilder deviceSystemInfoService(DeviceSystemInfoService deviceSystemInfoService){
            this.deviceSystemInfoService = deviceSystemInfoService;
            return this;
        }

        public ScheduledSystemInfoSenderBuilder outputSystemInfoBuffer(OutputSystemInfoBuffer outputSystemInfoBuffer){
            this.outputSystemInfoBuffer = outputSystemInfoBuffer;
            return this;
        }

        public ScheduledSystemInfoSenderBuilder deviceSystemInfoData(DeviceSystemInfoData deviceSystemInfoData){
            this.deviceSystemInfoData = deviceSystemInfoData;
            return this;
        }

        public ScheduledSystemInfoSender build(){
            return new ScheduledSystemInfoSender(this);
        }
    }

    private ScheduledSystemInfoSender(ScheduledSystemInfoSenderBuilder builder){

        this.deviceSystemInfoData = builder.deviceSystemInfoData;
        this.deviceSystemInfoService = builder.deviceSystemInfoService;
        this.outputSystemInfoBuffer = builder.outputSystemInfoBuffer;
    }

    @Override
    public void run() {
        outputSystemInfoBuffer.scheduleOutput(deviceSystemInfoService, deviceSystemInfoData);
    }
}
