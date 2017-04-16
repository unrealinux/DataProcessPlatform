package com.circletech.smartconnect.sender;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.data.OutputTransducerBuffer;
import com.circletech.smartconnect.service.DeviceTransducerDataService;
import javafx.util.Builder;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class ScheduledTransducerSender implements Runnable {

    private DeviceTransducerDataService deviceTransducerDataService;
    private CommDataProcessor commDataProcessor;
    private OutputTransducerBuffer outputTransducerBuffer;
    private DeviceTransducerDataData deviceTransducerData;

    public DeviceTransducerDataService getDeviceTransducerDataService() {
        return deviceTransducerDataService;
    }

    public void setDeviceTransducerDataService(DeviceTransducerDataService deviceTransducerDataService) {
        this.deviceTransducerDataService = deviceTransducerDataService;
    }

    public CommDataProcessor getCommDataProcessor() {
        return commDataProcessor;
    }

    public void setCommDataProcessor(CommDataProcessor commDataProcessor) {
        this.commDataProcessor = commDataProcessor;
    }

    public OutputTransducerBuffer getOutputTransducerBuffer() {
        return outputTransducerBuffer;
    }

    public void setOutputTransducerBuffer(OutputTransducerBuffer outputTransducerBuffer) {
        this.outputTransducerBuffer = outputTransducerBuffer;
    }

    public DeviceTransducerDataData getDeviceTransducerData() {
        return deviceTransducerData;
    }

    public void setDeviceTransducerData(DeviceTransducerDataData deviceTransducerData) {
        this.deviceTransducerData = deviceTransducerData;
    }

    public static class ScheduledTransducerSenderBuilder implements Builder<ScheduledTransducerSender> {

        private DeviceTransducerDataService deviceTransducerDataService;
        private CommDataProcessor commDataProcessor;
        private OutputTransducerBuffer outputTransducerBuffer;
        private DeviceTransducerDataData deviceTransducerData;

        public ScheduledTransducerSenderBuilder deviceTransducerDataService(DeviceTransducerDataService deviceTransducerDataService){
            this.deviceTransducerDataService = deviceTransducerDataService;
            return this;
        }

        public ScheduledTransducerSenderBuilder commDataProcessor(CommDataProcessor commDataProcessor){
            this.commDataProcessor = commDataProcessor;
            return this;
        }

        public ScheduledTransducerSenderBuilder outputTransducerBuffer(OutputTransducerBuffer outputTransducerBuffer){
            this.outputTransducerBuffer = outputTransducerBuffer;
            return this;
        }

        public ScheduledTransducerSenderBuilder deviceTransducerData(DeviceTransducerDataData deviceTransducerDataData){
            this.deviceTransducerData = deviceTransducerDataData;
            return this;
        }

        public ScheduledTransducerSender build(){
            return new ScheduledTransducerSender(this);
        }
    }

    private ScheduledTransducerSender(ScheduledTransducerSenderBuilder builder){

        this.deviceTransducerData = builder.deviceTransducerData;
        this.deviceTransducerDataService = builder.deviceTransducerDataService;
        this.outputTransducerBuffer = builder.outputTransducerBuffer;
        this.commDataProcessor = builder.commDataProcessor;
    }

    public ScheduledTransducerSender(DeviceTransducerDataService deviceTransducerDataService, CommDataProcessor commDataProcessor, OutputTransducerBuffer outputTransducerBuffer, DeviceTransducerDataData deviceTransducerData) {
        this.deviceTransducerDataService = deviceTransducerDataService;
        this.commDataProcessor = commDataProcessor;
        this.outputTransducerBuffer = outputTransducerBuffer;
        this.deviceTransducerData = deviceTransducerData;
    }

    @Override
    public void run() {
        outputTransducerBuffer.scheduleOutput(deviceTransducerDataService, deviceTransducerData, commDataProcessor);
    }
}
