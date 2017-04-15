package com.circletech.smartconnect.sender;

import com.circletech.smartconnect.data.DeviceSystemInfoData;
import com.circletech.smartconnect.data.OutputSystemInfoBuffer;
import com.circletech.smartconnect.service.DeviceSystemInfoService;
import com.circletech.smartconnect.util.ConstantUtil;
import javafx.util.Builder;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class SystemInfoSender implements Runnable {

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

    public static class SystemInfoSenderBuilder implements Builder<SystemInfoSender> {

        private DeviceSystemInfoService deviceSystemInfoService;
        private OutputSystemInfoBuffer outputSystemInfoBuffer;
        private DeviceSystemInfoData deviceSystemInfoData;

        public SystemInfoSenderBuilder deviceSystemInfoService(DeviceSystemInfoService deviceSystemInfoService){
            this.deviceSystemInfoService = deviceSystemInfoService;
            return this;
        }

        public SystemInfoSenderBuilder outputSystemInfoBuffer(OutputSystemInfoBuffer outputSystemInfoBuffer){
            this.outputSystemInfoBuffer = outputSystemInfoBuffer;
            return this;
        }

        public SystemInfoSenderBuilder deviceSystemInfoData(DeviceSystemInfoData systemInfoDataData){
            this.deviceSystemInfoData = systemInfoDataData;
            return this;
        }

        public SystemInfoSender build(){

            return new SystemInfoSender(this);
        }
    }

    private SystemInfoSender(SystemInfoSenderBuilder builder) {
        this.deviceSystemInfoService = builder.deviceSystemInfoService;
        this.outputSystemInfoBuffer = builder.outputSystemInfoBuffer;
        this.deviceSystemInfoData = builder.deviceSystemInfoData;
    }

    @Override
    public void run() {
        while (true){
            outputSystemInfoBuffer.output(deviceSystemInfoService, deviceSystemInfoData);
            try{
                Thread.sleep(ConstantUtil.THREAD_SLEEP_SPAN);
            }catch (InterruptedException e){
                e.printStackTrace();

            }
        }

    }

}
