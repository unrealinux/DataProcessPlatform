package com.circletech.smartconnect.sender;

import com.circletech.smartconnect.CommDataProcessor;
import com.circletech.smartconnect.data.DeviceTransducerDataData;
import com.circletech.smartconnect.data.OutputTransducerBuffer;
import com.circletech.smartconnect.service.DeviceTransducerDataService;

/**
 * Created by xieyingfei on 2017/1/13.
 */
public class ScheduledTransducerSender implements Runnable {

    private DeviceTransducerDataService deviceTransducerDataService;
    private CommDataProcessor CommDataProcessor;
    private OutputTransducerBuffer outputTransducerBuffer;
    private DeviceTransducerDataData deviceTransducerData;

    public DeviceTransducerDataService getDeviceTransducerDataService() {
        return deviceTransducerDataService;
    }

    public void setDeviceTransducerDataService(DeviceTransducerDataService deviceTransducerDataService) {
        this.deviceTransducerDataService = deviceTransducerDataService;
    }

    public CommDataProcessor getCommDataProcessor() {
        return CommDataProcessor;
    }

    public void setCommDataProcessor(CommDataProcessor CommDataProcessor) {
        this.CommDataProcessor = CommDataProcessor;
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

    public ScheduledTransducerSender(DeviceTransducerDataService deviceTransducerDataService, CommDataProcessor CommDataProcessor, OutputTransducerBuffer outputTransducerBuffer, DeviceTransducerDataData deviceTransducerData) {
        this.deviceTransducerDataService = deviceTransducerDataService;
        this.CommDataProcessor = CommDataProcessor;
        this.outputTransducerBuffer = outputTransducerBuffer;
        this.deviceTransducerData = deviceTransducerData;
    }

    @Override
    public void run() {
        outputTransducerBuffer.scheduleOutput(deviceTransducerDataService, deviceTransducerData, CommDataProcessor);
    }
}
